package com.platform.rabbitmq.sender;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

/**
 * @author Advance
 * @date 2022年07月20日 16:54
 * @since V1.0.0
 */
@Component
public class RabbitReceiveHandler {
    public void handler(RabbitReceiveService rabbitReceiveService , CorrelationData correlationData, boolean ack, String cause){
        rabbitReceiveService.invokeReturnMessage(correlationData,ack,cause);
    }
}
