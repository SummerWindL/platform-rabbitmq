package com.platform.rabbitmq.entity;

import com.platform.core.rabbitmq.entity.IRabbitMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年07月17日 11:48
 * @since V1.0.0
 */
public class DefaultCorrelationData extends CorrelationData implements Serializable {

    private String beanName;

    private String routeName;

    private IRabbitMessage message;

    public DefaultCorrelationData(String beanName, String routeName, IRabbitMessage message) {
        super(message.getId());
        this.beanName = beanName;
        this.routeName = routeName;
        this.message = message;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getRouteName() {
        return routeName;
    }

    public IRabbitMessage getMessage() {
        return message;
    }
}