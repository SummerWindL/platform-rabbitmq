package com.platform.rabbitmq.util;

import com.platform.core.rabbitmq.annotation.MessageSender;
import com.platform.core.constant.GlobalConstant;
import com.platform.core.util.StringUtil;
import com.platform.repo.pg.model.mq.SysMqRoute;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Advance
 * @date 2022年07月17日 11:55
 * @since V1.0.0
 */
public class ConfigUtils {
    private static List<SysMqRoute> ROUTE_CACHE = new ArrayList<>();

    private static Map<String, Queue> QUEUE_CACHE = new HashMap<>();

    private static Map<String, Boolean> CACHED = new HashMap<>();

    /**
     * 验证是否为数据库中的配置，如果不是，则返回false
     * @param sender
     * @return
     */
    public static boolean check(MessageSender sender) {
        String routingKey = sender.routingKey();
        String exchange = sender.exchange();
        String tempKey = routingKey + GlobalConstant.UNDER_LINE + exchange;
        Boolean exist = CACHED.get(tempKey);
        if(null == exist) {
            for(SysMqRoute route : ROUTE_CACHE) {
                exist = StringUtil.equals(StringUtil.defaultString(routingKey, "none"), route.getRoutename()) && StringUtil.equals(exchange, route.getExchangename());
                if(exist) {
                    break;
                }
            }
            CACHED.put(tempKey, exist);
        }
        return exist.booleanValue();
    }

    /**
     * 验证是否为数据库中的配置，如果不是，则返回false
     * @param receiver
     * @return
     */
    public static boolean check(RabbitListener receiver) {
        String[] queues = receiver.queues();
        for(String queueName : queues) {
            if(QUEUE_CACHE.containsKey(queueName)) {
                return true;
            }
        }
        return false;
    }

    public static void cache(Map<String, Queue> queueMap, List<SysMqRoute> sysMqRouteList) {
        ROUTE_CACHE.addAll(sysMqRouteList);
        QUEUE_CACHE.putAll(queueMap);
    }
}
