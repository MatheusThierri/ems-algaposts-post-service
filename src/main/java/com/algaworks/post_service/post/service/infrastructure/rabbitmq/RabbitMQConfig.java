package com.algaworks.post_service.post.service.infrastructure.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_POST_PROCESSING = "text-processor-service.post-processing.v1.q";
    public static final String QUEUE_POST_PROCESSING_RESULT = "post-service.post-processing-result.v1.q";
    public static final String FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED = "text-processor-service.post-processing-received.v1.e";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public FanoutExchange exchange() {
        return ExchangeBuilder.fanoutExchange(FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED).build();
    }

    @Bean
    public Queue queuePostProcess() {
        return QueueBuilder.durable(QUEUE_POST_PROCESSING).build();
    }

    @Bean
    public Binding bindingTextProcessor() {
        return BindingBuilder.bind(queuePostProcess()).to(exchange());
    }
}
