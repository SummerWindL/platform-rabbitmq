package com.platform.rabbitmq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

/**
 * @author Advance
 * @date 2022年07月17日 11:33
 * @since V1.0.0
 */
public class PlatformRabbitListenerContainerFactory extends SimpleRabbitListenerContainerFactory {
    public PlatformRabbitListenerContainerFactory(RabbitProperties properties) {
        RabbitProperties.Listener listener = properties.getListener();
        if(null != listener) {
            //setAcknowledgeMode(listener.getSimple().getAcknowledgeMode());
            setAcknowledgeMode(AcknowledgeMode.MANUAL);
            //setMessageConverter(new Jackson2JsonMessageConverter());
        }
    }


    @Override
    protected SimpleMessageListenerContainer createContainerInstance() {
        return new MessageListenerContainer();
    }
}
