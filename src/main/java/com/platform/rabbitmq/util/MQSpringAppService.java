package com.platform.rabbitmq.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class MQSpringAppService implements ApplicationContextAware {

    private final static Log log = LogFactory.getLog(MQSpringAppService.class);

    private static ApplicationContext context;

    private MQSpringAppService() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String beanName) {
        if (context == null) {
            return null;
        }
        Object obj = null;
        try {
//			 log.info("load spring bean [" + service + "]");
            obj = context.getBean(beanName);
        } catch (Exception e) {
            log.error("load spring bean [" + beanName + "] failed");
            return null;
        }
        return (T) obj;
    }

    public static String getBeanName(Class<?> clz) {
        if (context == null) {
            return null;
        }
        String beanName = null;
        try {
            beanName = context.getBeanNamesForType(clz)[0];
        } catch (Exception e) {
            return null;
        }
        return beanName;
    }

    public static <T> T getBean(Class<T> tClass) throws BeansException {
        return context.getBean(tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MQSpringAppService.context = applicationContext;
        //启动时就获取 直接获取可能存在问题
    }

    /**
     * @return ApplicationContext返回给其它模块使用
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 暴露的静态方法
     * @param applicationContext
     * @throws BeansException
     */
    public static void setApplicationContextStatic(ApplicationContext applicationContext) throws BeansException {
        MQSpringAppService.context = applicationContext;
    }

}
