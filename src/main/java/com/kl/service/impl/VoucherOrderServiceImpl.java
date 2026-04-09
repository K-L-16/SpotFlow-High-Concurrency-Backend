package com.kl.service.impl;


import com.kl.dto.Result;
import com.kl.entity.VoucherOrder;
import com.kl.repository.VoucherOrderRepository;
import com.kl.service.ISeckillVoucherService;
import com.kl.service.IVoucherOrderService;
import com.kl.utils.MQConstants;
import com.kl.utils.RedisIDWorker;
import com.kl.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@Slf4j
public class VoucherOrderServiceImpl  implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIDWorker redisIDWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private VoucherOrderRepository voucherOrderRepository;

    //加载脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPTL;
    static {
        SECKILL_SCRIPTL = new DefaultRedisScript<>();
        SECKILL_SCRIPTL.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPTL.setResultType(Long.class);
    }


    @Override
    public Result seckillVoucher(Long voucherId) {
        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPTL,
                Collections.emptyList(),
                voucherId.toString(),
                UserHolder.getUser().getId().toString()
        );
        //2.判断结果是否为0
        int r = result.intValue();
        if(r != 0){
            //2.1部位0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        //2.2为0.有购买资格，把下单的信息保存到阻塞队列
        //todo 保存阻塞队列
        long orderId = redisIDWorker.nextId("order");
        //2.3创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //2.4订单id
        voucherOrder.setId(orderId);
        //2.5用户id
        Long id = UserHolder.getUser().getId();
        voucherOrder.setUserId(id);
        //2.6优惠券id
        voucherOrder.setVoucherId(voucherId);
        //放入mq
        try{
            rabbitTemplate.convertAndSend(
                    MQConstants.SECKILL_EXCHANGE,
                    MQConstants.SECKILL_ROUTING_KEY,
                    voucherOrder
            );
        }catch (Exception e) {
            log.error("发送 RabbitMQ 消息失败，订单ID: {}", orderId, e);
            throw new RuntimeException("发送消息失败");
        };

        //3.返回订单id
        return Result.ok(orderId);
    }

    @Transactional
    public  void createVoucherOrder(VoucherOrder voucherOrder){
        //5 一人一单，不能多买
        Long userId = voucherOrder.getUserId();
            //5.1 查询订单
            int count = voucherOrderRepository.countByUserIdAndVoucherId(
                    userId,
                    voucherOrder.getVoucherId()
            );
            //5.2判断是否存在
            if (count > 0){
                log.error("用户已经购买过一次");
                return;
            }

            //6.扣减库存
        int updated = seckillVoucherService.deductStock(voucherOrder.getVoucherId());
        if (updated == 0) {
            log.error("库存不足");
            return;
        }
            //7.创建订单
        voucherOrderRepository.save(voucherOrder);
        log.info("订单创建成功");
        }
}
