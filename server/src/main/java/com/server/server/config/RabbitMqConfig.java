package com.server.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 订单超时取消配置
 * <p>
 * 延迟实现方式为「队列级 TTL + 死信队列」，不依赖任何 RabbitMQ 插件：
 * <ol>
 *     <li>下单成功后向 {@link #ORDER_DELAY_EXCHANGE} 投递订单号；</li>
 *     <li>消息进入 {@link #ORDER_DELAY_QUEUE} 后不会被立即消费，而是等待 30 分钟；</li>
 *     <li>消息在队列中过期后，由 RabbitMQ 自动转发到死信交换机 {@link #ORDER_DLX_EXCHANGE}；</li>
 *     <li>死信最终落入 {@link #ORDER_CANCEL_QUEUE}，由监听器执行真正的取消逻辑。</li>
 * </ol>
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 延迟消息投递交换机
     */
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";

    /**
     * 延迟队列：消息在其中停留到超时时间，自身不作为消费队列
     */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    /**
     * 延迟队列的绑定路由键
     */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    /**
     * 死信交换机：接收延迟队列中过期的消息
     */
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    /**
     * 订单取消队列：真正被消费者监听的队列
     */
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    /**
     * 订单取消队列的绑定路由键
     */
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /**
     * 订单未支付超时时长：30 分钟
     */
    public static final int ORDER_TIMEOUT_MILLIS = 30 * 60 * 1000;

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE, true, false);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    /**
     * 延迟队列：声明消息存活上限，并指定过期后的死信去向
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .ttl(ORDER_TIMEOUT_MILLIS)
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(ORDER_CANCEL_ROUTING_KEY)
                .build();
    }

    /**
     * 订单取消队列
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    /**
     * 延迟交换机与延迟队列的绑定
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 死信交换机与订单取消队列的绑定
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderDlxExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }
}
