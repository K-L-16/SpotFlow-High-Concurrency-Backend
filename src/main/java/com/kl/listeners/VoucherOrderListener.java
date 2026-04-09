package com.kl.listeners;

import com.kl.entity.VoucherOrder;
import com.kl.service.IVoucherOrderService;
import com.kl.utils.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VoucherOrderListener {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @RabbitListener(queues = MQConstants.SECKILL_QUEUE)
    public void listenSeckillOrder(VoucherOrder voucherOrder) {
        handleVoucherOrder(voucherOrder);
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        RLock lock = redissonClient.getLock("lock:order:" + userId + ":" + voucherId);
        boolean isLock = lock.tryLock();

        if (!isLock) {
            log.error("不允许重复下单");
            return;
        }

        try {
            //这里已经拿到了代理对象，所以不需要担心这个事务
            voucherOrderService.createVoucherOrder(voucherOrder);
        } finally {
            lock.unlock();
        }
    }
}