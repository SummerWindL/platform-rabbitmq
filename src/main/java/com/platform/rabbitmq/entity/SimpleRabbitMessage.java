package com.platform.rabbitmq.entity;

import com.platform.core.rabbitmq.entity.IRabbitMessage;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年07月17日 11:53
 * @since V1.0.0
 */
public class SimpleRabbitMessage implements IRabbitMessage {
    private String id;

    private Serializable message;

    //private Serializable resultMessage;
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setMessage(Serializable message) {
        this.message = message;
    }

    @Override
    public Serializable getMessage() {
        return message;
    }

    /*@Override
    public void setResultMessage(Serializable result) {
        this.resultMessage = result;
    }

    @Override
    public Serializable getResultMessage() {
        return resultMessage;
    }*/
}
