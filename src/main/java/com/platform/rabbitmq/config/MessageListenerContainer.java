package com.platform.rabbitmq.config;

import com.platform.rabbitmq.annotation.RabbitListenerAnnotationExecutor;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/**
 * @author Advance
 * @date 2022年07月17日 11:33
 * @since V1.0.0
 */
public class MessageListenerContainer extends SimpleMessageListenerContainer {

    @Override
    protected void doInvokeListener(ChannelAwareMessageListener listener, Channel channel, Object data) {
        RabbitListenerAnnotationExecutor.setRabbitlistenerParameter(channel, (Message) data);
        super.doInvokeListener(listener, channel, data);
    }
}