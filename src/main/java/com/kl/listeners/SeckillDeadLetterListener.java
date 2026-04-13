package com.kl.listeners;

import com.kl.entity.VoucherOrder;
import com.kl.utils.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillDeadLetterListener {

    @RabbitListener(queues = MQConstants.SECKILL_DLQ)
    public void listenDeadLetter(VoucherOrder voucherOrder) {
        log.error("Dead letter:，orderId={}, userId={}, voucherId={}",
                voucherOrder.getId(),
                voucherOrder.getUserId(),
                voucherOrder.getVoucherId());
    }
}