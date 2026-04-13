package com.kl.listeners;

import com.kl.entity.VoucherOrder;
import com.kl.service.IVoucherOrderService;
import com.kl.utils.MQConstants;
import com.kl.utils.VoucherOrderProcessResult;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
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
    public void listenSeckillOrder(VoucherOrder voucherOrder, Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();

        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        RLock lock = redissonClient.getLock("lock:order:" + userId + ":" + voucherId);
        boolean isLock = lock.tryLock();

        if (!isLock) {
            log.warn("Failure to get lock is considered a duplicate concurrent message, and an ACK is sent directly，orderId={}", voucherOrder.getId());
            channel.basicAck(tag, false);
            return;
        }

        try {
            VoucherOrderProcessResult result = voucherOrderService.createVoucherOrder(voucherOrder);

            switch (result) {
                case SUCCESS, DUPLICATE_MESSAGE, DUPLICATE_USER_ORDER, OUT_OF_STOCK -> {
                    channel.basicAck(tag, false);
                    log.info("VoucherOrder warn，orderId={}, result={}", voucherOrder.getId(), result);
                }
                default -> {
                    channel.basicNack(tag, false, false);
                    log.error("Unknow reason，Enter DLE，orderId={}, result={}", voucherOrder.getId(), result);
                }
            }
        } catch (Exception e) {
            log.error("consumer error，orderId={}", voucherOrder.getId(), e);
            channel.basicNack(tag, false, false);
        } finally {
            lock.unlock();
        }
    }
}