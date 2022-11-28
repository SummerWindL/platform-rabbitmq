package com.platform.rabbitmq.sender;

import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * @author Advance
 * @date 2022年07月20日 16:44
 * @since V1.0.0
 */
public interface RabbitReceiveService {
    /**
     * 消息投递后返回信息
     * @param correlationData
     * @param ack
     * @param cause
     */
    void invokeReturnMessage(CorrelationData correlationData, boolean ack, String cause);
}
