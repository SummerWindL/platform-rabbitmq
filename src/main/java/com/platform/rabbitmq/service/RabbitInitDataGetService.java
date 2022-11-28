package com.platform.rabbitmq.service;

import com.platform.repo.pg.model.mq.SysMqExchange;
import com.platform.repo.pg.model.mq.SysMqQueue;
import com.platform.repo.pg.model.mq.SysMqRoute;

import java.util.List;

/**
 * @author Advance
 * @date 2022年07月17日 12:28
 * @since V1.0.0
 */
public interface RabbitInitDataGetService {
    /**
     * 获取系统定义的Queue列表
     * @return SysMqQueue列表
     */
    List<SysMqQueue> findAllSysMqQueues();

    /**
     * 获取系统定义的Exchange列表
     * @return SysMqExchange列表
     */
    List<SysMqExchange> findAllSysMqExchange();

    /**
     * 获取系统定义的路由规则Route
     * @return SysMqRoute列表
     */
    List<SysMqRoute> findAllSysMqRoute();
}
