package com.kl.config;

import com.kl.utils.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(MQConstants.SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MQConstants.SECKILL_DLX);
        args.put("x-dead-letter-routing-key", MQConstants.SECKILL_DLQ_ROUTING_KEY);
        return new Queue(MQConstants.SECKILL_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(MQConstants.SECKILL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange seckillDlx() {
        return new DirectExchange(MQConstants.SECKILL_DLX, true, false);
    }

    @Bean
    public Queue seckillDlq() {
        return new Queue(MQConstants.SECKILL_DLQ, true);
    }

    @Bean
    public Binding seckillDlqBinding() {
        return BindingBuilder.bind(seckillDlq())
                .to(seckillDlx())
                .with(MQConstants.SECKILL_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}