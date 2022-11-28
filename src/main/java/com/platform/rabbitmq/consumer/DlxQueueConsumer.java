package com.platform.rabbitmq.consumer;

import com.platform.core.rabbitmq.entity.IRabbitMessage;
import com.platform.rabbitmq.constant.RabbitConstants;
import com.platform.rabbitmq.util.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Advance
 * @date 2022年07月17日 12:00
 * @since V1.0.0
 */
@Lazy(false)
@Component
@RabbitListener(queues = RabbitConstants.DLX_QUEUE)
public class DlxQueueConsumer {
    @RabbitHandler
    public void process(IRabbitMessage context, Channel channel, Message message) throws IOException {

        System.err.println("dlx queue");

        RabbitUtils.ack(channel, message);
    }
}
