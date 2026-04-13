package com.kl.utils;

public class MQConstants {
    public static final String SECKILL_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ROUTING_KEY = "seckill.order";

    public static final String SECKILL_DLX = "seckill.order.dlx";
    public static final String SECKILL_DLQ = "seckill.order.dlq";
    public static final String SECKILL_DLQ_ROUTING_KEY = "seckill.order.dead";
}