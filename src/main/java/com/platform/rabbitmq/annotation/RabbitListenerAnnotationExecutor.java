package com.platform.rabbitmq.annotation;

import com.platform.core.rabbitmq.entity.IRabbitMessage;
import com.platform.rabbitmq.constant.RabbitConstants;
import com.platform.rabbitmq.entity.DefaultCorrelationData;
import com.platform.rabbitmq.service.RabbitLogService;
import com.platform.rabbitmq.util.ConfigUtils;
import com.platform.rabbitmq.util.RabbitUtils;
import com.platform.repo.pg.model.mq.SysMqReceiveLog;
import com.rabbitmq.client.Channel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Advance
 * @date 2022年07月17日 11:33
 * @since V1.0.0
 */
@Lazy(false)
@Order(-1)
@Aspect
@Component
public class RabbitListenerAnnotationExecutor {

    private Logger logger = LoggerFactory.getLogger(RabbitListenerAnnotationExecutor.class);

    private RabbitLogService rabbitLogService;

    private static final ThreadLocal<RabblistenerParameter> rabbitlistenerParameter = new NamedThreadLocal<>("rabbitListener parameter");

    @Pointcut("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public void aspect() {

    }

    public RabbitListenerAnnotationExecutor(RabbitLogService rabbitLogService) {
        this.rabbitLogService = rabbitLogService;
    }

    @Around("aspect()")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        IRabbitMessage message = checkAndGetArguments(pjp);;
        RabbitListener listener = getListener(pjp);
        if(ConfigUtils.check(listener)) {
            try {
                result = pjp.proceed();

                ackMessage(pjp, message, true);
            } catch (Throwable throwable) {
                ackMessage(pjp, message, false);
                throw throwable;
            }
        }
        else {
            throw new IllegalArgumentException("不符合配置规则的交换机，忽略此消息");
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
//        // TODO
//        System.out.println("afterThrowing method ...");
//    }

    private void ackMessage(ProceedingJoinPoint joinPoint, IRabbitMessage rabbitMessage, boolean success) {
        RabblistenerParameter parameter = rabbitlistenerParameter.get();
        if(null != parameter) {
            SysMqReceiveLog mqReceiveLog = rabbitLogService.buildReceiveLog(new DefaultCorrelationData(RabbitUtils.getBeanName(joinPoint), null, rabbitMessage));
            mqReceiveLog.setReceivestatus(success ? RabbitConstants.STATUS_SUCCESS : RabbitConstants.STATUS_FAILED);
            rabbitLogService.insertMqReceiveLog(mqReceiveLog);
            try {
                if(success) {
                    // 只有消费成功时才进行ack操作
                    RabbitUtils.ack(parameter.getChannel(), parameter.getMessage());
                }
            } catch (IOException e) {
                logger.error("消息确认失败", e);
            }
            finally {
                rabbitlistenerParameter.remove();
            }
        }
    }

    private IRabbitMessage checkAndGetArguments(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        IRabbitMessage rabbitMessage = null;
        for(Object arg : args) {
            if(arg instanceof IRabbitMessage) {
                rabbitMessage = (IRabbitMessage)arg;
            }
        }
        if(null == rabbitMessage) {
            throw new IllegalArgumentException("方法参数不正确");
        }
        return rabbitMessage;
    }

    protected RabbitListener getListener(JoinPoint joinPoint) {
        return ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(RabbitListener.class);
    }

    public static void setRabbitlistenerParameter(Channel channel, Message message) {
        rabbitlistenerParameter.set(new RabblistenerParameter(channel, message));
    }

    /**
     *
     */
    static class RabblistenerParameter {
        private Channel channel;
        private Message message;

        RabblistenerParameter(Channel channel, Message message) {
            this.channel = channel;
            this.message = message;
        }

        public Channel getChannel() {
            return channel;
        }

        public Message getMessage() {
            return message;
        }
    }
}
