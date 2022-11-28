package com.platform.rabbitmq.config;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * @author Advance
 * @date 2022年07月17日 12:21
 * @since V1.0.0
 */
public class RabbitErrorHandler extends ConditionalRejectingErrorHandler {
    @Override
    public void handleError(Throwable t) {
        super.handleError(t);
    }
}