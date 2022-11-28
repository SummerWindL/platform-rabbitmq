package com.platform.rabbitmq.util;

import com.rabbitmq.client.Channel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Advance
 * @date 2022年07月17日 11:57
 * @since V1.0.0
 */
public class RabbitUtils {
    private static String SPLITOR = "#";

//    private static Map<String, DefaultCorrelationData> SEND_MESSAGE_CACHE = new HashMap<>();

    public static void ack(Channel channel, Message messge) throws IOException {
        channel.basicAck(messge.getMessageProperties().getDeliveryTag(), false);
    }

    public static void ackMulit(Channel channel, Message messge) throws IOException {
        channel.basicAck(messge.getMessageProperties().getDeliveryTag(), true);
    }

//    public static DefaultCorrelationData getAndRemoveCorrelationData(CorrelationData correlationData) {
//        String msgId = correlationData.getId();
//        if(SEND_MESSAGE_CACHE.containsKey(msgId)) {
//            DefaultCorrelationData data = SEND_MESSAGE_CACHE.get(msgId);
//            SEND_MESSAGE_CACHE.remove(msgId);
//            return data;
//        }
//        return null;
//    }
//
//    public static boolean recordSendCorrelationData(String msgId, DefaultCorrelationData correlationData) {
//        if(!SEND_MESSAGE_CACHE.containsKey(msgId)) {
//            SEND_MESSAGE_CACHE.put(msgId, correlationData);
//            return true;
//        }
//        // 重复发送
//        return false;
//    }

    public static String getBeanName(JoinPoint joinPoint) {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        return method.getDeclaringClass().getName() + SPLITOR + method.getName();
    }
}
