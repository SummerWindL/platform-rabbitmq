package com.platform.rabbitmq.annotation;

import com.platform.core.rabbitmq.annotation.MessageSender;
import com.platform.core.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Advance
 * @date 2022年07月17日 10:57
 * @since V1.0.0
 */
public class MessageAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {
    private Logger logger = LoggerFactory.getLogger(MessageAnnotationBeanPostProcessor.class);
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            MessageSender methodLevelSender = AnnotationUtils.findAnnotation(method, MessageSender.class);
            if (null != methodLevelSender) {
                logger.debug("注册消息生产者： {exchange:" + methodLevelSender.exchange() + ", routingKey:" + methodLevelSender.routingKey() + "}");
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        ReflectionUtils.doWithMethods(targetClass,method -> {
            RabbitListener rabbitListener = AnnotationUtils.findAnnotation(method, RabbitListener.class);
            if (null != rabbitListener) {
                logger.debug("注册消息消费者： {" + StringUtil.join(rabbitListener.queues()) + "}");
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
