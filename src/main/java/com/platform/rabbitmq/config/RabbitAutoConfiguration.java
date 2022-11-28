package com.platform.rabbitmq.config;

import com.platform.rabbitmq.annotation.MessageAnnotationBeanPostProcessor;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author Advance
 * @date 2022年07月17日 11:13
 * @since V1.0.0
 */


@Configuration
public class RabbitAutoConfiguration {

//    @Value("${spring.rabbitmq.addresses}")
//    private String addresses;
//    @Value("${spring.rabbitmq.username}")
//    private String userName;
//    @Value("${spring.rabbitmq.password}")
//    private String passWord;
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setAddresses(addresses);
//        connectionFactory.setUsername(userName);
//        connectionFactory.setPassword(passWord);
//        connectionFactory.setVirtualHost("vhost_nei");
//        connectionFactory.setPublisherConfirms(true);//消息确认
//        connectionFactory.setPublisherReturns(true);
//        return connectionFactory;
//    }

    @Bean("rabbitListenerContainerFactory")
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                         ConnectionFactory connectionFactory, RabbitProperties rabbitProperties) {
        PlatformRabbitListenerContainerFactory factory = new PlatformRabbitListenerContainerFactory(rabbitProperties);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean("messageSenderAnnotationBeanPostProcessor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MessageAnnotationBeanPostProcessor messageSenderAnnotationBeanPostProcessor() {
        return new MessageAnnotationBeanPostProcessor();
    }

    @Bean
    public MessageListener exampleListener(){
        return new MessageListener() {
            @Override
            public void onMessage(Message message) {
                System.out.println("received: "+ message);
            }
        };
    }

    @Bean
    public ChannelAwareMessageListener channelAwareMessageListener(){
        return new ChannelAwareMessageListener() {

            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                System.out.println("received: "+ message+" channel: "+channel);
            }
        };
    }


}

