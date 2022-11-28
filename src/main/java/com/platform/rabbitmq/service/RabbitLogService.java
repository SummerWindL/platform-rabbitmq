package com.platform.rabbitmq.service;

import com.platform.rabbitmq.entity.DefaultCorrelationData;
import com.platform.repo.pg.model.mq.SysMqReceiveLog;
import com.platform.repo.pg.model.mq.SysMqSendLog;

/**
 * @author Advance
 * @date 2022年07月17日 11:35
 * @since V1.0.0
 */
public interface RabbitLogService {
    /**
     * 记录需要补偿处理的消息(消费异常的消息)
     * @param id 消息ID
     * @param status 状态
     */
    void updateSendStatus(String id, String status);
    /**
     * 消费日志记录
     * @param message SysMqReceiveLog
     */
    void insertMqReceiveLog(SysMqReceiveLog message);

    /**
     * 记录消息发送日志
     * @param message
     */
    void insertMqSendLog(SysMqSendLog message);

    /**
     *
     * @param correlationData
     * @return
     */
    SysMqReceiveLog buildReceiveLog(DefaultCorrelationData correlationData);

    /**
     *
     * @param correlationData
     * @param status
     * @return
     */
    SysMqSendLog buildSendLog(DefaultCorrelationData correlationData, String status);
}
