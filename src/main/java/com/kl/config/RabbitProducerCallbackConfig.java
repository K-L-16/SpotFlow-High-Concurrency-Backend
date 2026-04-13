package com.kl.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitProducerCallbackConfig {

    private final RabbitTemplate rabbitTemplate;

    public RabbitProducerCallbackConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {

        // 1. confirm callback：消息有没有到 broker / exchange
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                String messageId = correlationData != null ? correlationData.getId() : null;

                if (ack) {
                    log.info("MQ confirm success, messageId={}", messageId);
                } else {
                    log.error("MQ confirm failed, messageId={}, cause={}", messageId, cause);
                }
            }
        });

        // 2. return callback：消息到 exchange 了，但没进 queue
        rabbitTemplate.setReturnsCallback(returned -> {
            String body = new String(returned.getMessage().getBody());
            log.error(
                    "MQ return failed, exchange={}, routingKey={}, replyCode={}, replyText={}, body={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    body
            );
        });
    }
}