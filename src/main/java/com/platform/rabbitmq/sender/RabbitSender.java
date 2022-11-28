package com.platform.rabbitmq.sender;

import com.platform.core.rabbitmq.entity.IRabbitMessage;
import com.platform.core.util.IDGenerator;
import com.platform.rabbitmq.constant.RabbitConstants;
import com.platform.rabbitmq.entity.DefaultCorrelationData;
import com.platform.rabbitmq.entity.SimpleRabbitMessage;
import com.platform.rabbitmq.service.RabbitLogService;
import com.platform.rabbitmq.util.MQSpringAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年07月17日 11:59
 * @since V1.0.0
 */
@Component
public class RabbitSender {
    private final Logger logger = LoggerFactory.getLogger(RabbitSender.class);

    private RabbitTemplate rabbitTemplate;
    private AmqpTemplate amqpTemplate;
    private RabbitLogService logService;

    private static RabbitReceiveService rabbitReceiveService = null;

    private static RabbitReceiveHandler rabbitReceiveHandler = null;

    public RabbitSender(RabbitTemplate rabbitTemplate, RabbitLogService logService) {
        this.rabbitTemplate = rabbitTemplate;
        this.logService = logService;
        //设置投递回调
        this.rabbitTemplate.setConfirmCallback(confirmCallback);
        this.rabbitTemplate.setReturnCallback(returnCallback);
    }

    /**
     * ConfirmCallback用于监听 Broker端给我们返回的确认请求。
     * 无论消息可不可达，都会执行ConfirmCallback这个方法。
     */
    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {

        /**
         *
         * @param correlationData 唯一标识，有了这个唯一标识，我们就知道可以确认（失败）哪一条消息了
         * @param ack  是否投递成功
         * @param cause 失败原因
         */
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            /*if(rabbitReceiveService == null){
                //这个地方其他模块实现
                rabbitReceiveService =  MQSpringAppService.getBean("rabbitReceiveMonitor");
            }
            if(rabbitReceiveHandler == null){
                rabbitReceiveHandler = MQSpringAppService.getBean("rabbitReceiveHandler");
            }
            rabbitReceiveHandler.handler(rabbitReceiveService,correlationData,ack,cause);*/
            //返回成功，表示消息被正常投递
            if (ack) {
                if(logger.isDebugEnabled()) {
                    logger.debug("信息投递成功，messageId:{}", correlationData.getId());
                }
                logService.updateSendStatus(correlationData.getId(), RabbitConstants.STATUS_SUCCESS);
            } else {
                logger.error("信息投递失败，messageId:{} 原因:{}", correlationData.getId(), cause);
                logService.updateSendStatus(correlationData.getId(), RabbitConstants.STATUS_FAILED);
            }
        }
    };

    /**
     * 如果消息不可达，routingkey不匹配，那么需要做二次处理，如补偿处理。
     * 如果消息可达就不会到这一步。只有在消息不可达，才会执行这个方法。
     */
    final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
        @Override
        public void returnedMessage(org.springframework.amqp.core.Message message, int replyCode, String replyText, String exchange, String routingKey) {
            Object object = rabbitTemplate.getMessageConverter().fromMessage(message);
            if(object instanceof IRabbitMessage) {
                IRabbitMessage rabbitMessage = (IRabbitMessage)object;
                // 记录发送失败的消息
                logService.updateSendStatus(rabbitMessage.getId(), RabbitConstants.STATUS_FAILED);
            }
            throw new RuntimeException("消息不可达，routingkey不匹配 [exchange:" + exchange + ",routingKey:" + routingKey + ",replyCode" + replyCode + ",replyText：" + replyText + "]");
        }
    };

    /**
     * 信息投递的方法
     *
     * @param exchange
     * @param routingKey
     * @param message
     * @throws Exception
     */
    public IRabbitMessage send(String exchange, String routingKey, Serializable message, String beanName) {
        return doSend(exchange, routingKey, message, beanName);
    }

    /**
     * 信息投递的方法
     *
     * @param exchange
     * @param routingKey
     * @param message
     * @throws Exception
     */
    public IRabbitMessage send(String exchange, String routingKey, Serializable message) {
        return doSend(exchange, routingKey, message, "none");
    }

    protected IRabbitMessage doSend(String exchange, String routingKey, Serializable message, String beanName) {
        IRabbitMessage rabbitMessage;
        if(!(message instanceof IRabbitMessage)) {
            rabbitMessage = new SimpleRabbitMessage();
            rabbitMessage.setId(IDGenerator.generate());
            rabbitMessage.setMessage(message);
        }
        else {
            rabbitMessage = (IRabbitMessage)message;
        }
        DefaultCorrelationData correlationData = new DefaultCorrelationData(beanName, routingKey, rabbitMessage);
        if(logger.isDebugEnabled()){
            logger.debug("投递消息，messageId:{}", rabbitMessage.getId());
        }
        logService.insertMqSendLog(logService.buildSendLog(correlationData, RabbitConstants.STATUS_UNKNOWN));
        rabbitTemplate.send(exchange, routingKey, toMessage(rabbitMessage), new CorrelationData(rabbitMessage.getId()));
//        Object o = rabbitTemplate.convertSendAndReceive(exchange, routingKey, toMessage(rabbitMessage), new CorrelationData(rabbitMessage.getId()));
//        rabbitMessage.setResultMessage((Serializable) o);
        return rabbitMessage;
    }

    protected Message toMessage(IRabbitMessage message) {
        MessageProperties properties = new MessageProperties();
        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        // 让消息持久化，否则rabbitmq服务重启后消息将会丢失
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return messageConverter.toMessage(message, properties);
    }

    /**
     * 异步信息投递的方法
     * @param exchange
     * @param routingKey
     * @param message
     * @throws Exception
     */
    @Async
    public void sendAsync(String exchange, String routingKey, IRabbitMessage message) {
        doSend(exchange, routingKey, message, "none");
    }

    /**
     * 信息投递的方法
     * @param exchange
     * @param routingKey
     * @param message
     * @throws Exception
     */
    @Async
    public void sendAsync(String exchange, String routingKey, IRabbitMessage message, String beanName) {
        doSend(exchange, routingKey, message, beanName);
    }
}
