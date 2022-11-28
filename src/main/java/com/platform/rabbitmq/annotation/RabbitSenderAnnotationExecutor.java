package com.platform.rabbitmq.annotation;

import com.platform.core.rabbitmq.annotation.MessageSender;
import com.platform.core.rabbitmq.entity.IRabbitMessage;
import com.platform.core.util.JsonUtils;
import com.platform.rabbitmq.sender.RabbitSender;
import com.platform.rabbitmq.util.ConfigUtils;
import com.platform.rabbitmq.util.RabbitUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年07月17日 11:58
 * @since V1.0.0
 */
@Lazy(false)
@Order(-1)
@Aspect
@Component
public class RabbitSenderAnnotationExecutor {
    private RabbitSender rabbitSender;

    public RabbitSenderAnnotationExecutor(RabbitSender rabbitSender) {
        if(null == rabbitSender) {
            throw new IllegalArgumentException("rabbitSender未定义");
        }
        this.rabbitSender = rabbitSender;
    }

    private Logger logger = LoggerFactory.getLogger(RabbitSenderAnnotationExecutor.class);

    @Pointcut("@annotation(com.platform.core.rabbitmq.annotation.MessageSender)")
    public void aspect() {

    }

    @Around("aspect()")
    public Object interceptorForSender(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        MessageSender sender = getSender(pjp);
        if(ConfigUtils.check(sender)) {
            result = pjp.proceed();
            // 正确结束则发送MQ
            sendMessage(pjp, result);
        }
        else {
            throw new IllegalArgumentException("基础数据中未配置该消息发送规则");
        }
        return result;
    }
//
//    @After("aspect()")
//    public void after(JoinPoint joinPoint) {
//
//    }
//
//    @AfterThrowing(value = "aspect()", throwing = "throwable")
//    public void afterThrowing(Throwable throwable) {
//        logger.error("业务逻辑异常,消息未发送", throwable);
//    }
//
//    @AfterReturning(value = "aspect()", returning = "retVal")
//    public void afterReturn(JoinPoint joinPoint, Object retVal) {
//
//    }

    private void sendMessage(JoinPoint joinPoint, Object retVal) {
        if(logger.isDebugEnabled()) {
            logger.debug("业务代码执行结束,发送MQ消息");
        }
        MessageSender sender = getSender(joinPoint);
        String beanName = RabbitUtils.getBeanName(joinPoint);
        Serializable sendMessage;
        if(retVal instanceof Serializable) {
            sendMessage = (Serializable)retVal;
        }
        else {
            sendMessage = JsonUtils.object2Json(retVal);
        }
        IRabbitMessage message = rabbitSender.send(sender.exchange(), sender.routingKey(), sendMessage, beanName);
        if(logger.isDebugEnabled()) {
            logger.debug("MQ消息发送完成:" + message.getId());
        }
    }

    protected MessageSender getSender(JoinPoint joinPoint) {
        return ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(MessageSender.class);
    }
}
