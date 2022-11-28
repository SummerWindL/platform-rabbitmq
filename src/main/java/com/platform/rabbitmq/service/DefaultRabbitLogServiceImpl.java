package com.platform.rabbitmq.service;

import com.platform.common.util.StringUtil;
import com.platform.core.util.DateUtil;
import com.platform.core.util.IDGenerator;
import com.platform.core.util.JsonUtils;
import com.platform.rabbitmq.constant.RabbitConstants;
import com.platform.rabbitmq.entity.DefaultCorrelationData;
import com.platform.repo.pg.model.mq.SysMqReceiveLog;
import com.platform.repo.pg.model.mq.SysMqSendLog;
import com.platform.repo.pg.repo.sys.rabbitmq.SysMqReceiveLogRepository;
import com.platform.repo.pg.repo.sys.rabbitmq.SysMqSendLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Advance
 * @date 2022年07月17日 12:29
 * @since V1.0.0
 */
@Component
public class DefaultRabbitLogServiceImpl implements RabbitLogService{
    @Autowired
    private SysMqReceiveLogRepository receiveLogRepository;
    @Autowired
    private SysMqSendLogRepository sendLogRepository;
    @Override
    public void updateSendStatus(String id, String status) {
        SysMqSendLog sysMqSendLog = sendLogRepository.findById(id).orElse(null);
        //保存
        Optional.ofNullable(sysMqSendLog).ifPresent(u->{
            sysMqSendLog.setId(id);
            sysMqSendLog.setSendstatus(status);
            sendLogRepository.save(u);
        });
    }

    @Override
    public void insertMqReceiveLog(SysMqReceiveLog message) {
        receiveLogRepository.save(message);
    }

    @Override
    public void insertMqSendLog(SysMqSendLog message) {
        if(StringUtil.isEmpty(message.getRoutename())) {
            message.setRoutename(RabbitConstants.DEFAULT_ROUTE);
        }
        sendLogRepository.save(message);
    }

    @Override
    public SysMqReceiveLog buildReceiveLog(DefaultCorrelationData correlationData) {
        SysMqReceiveLog log = new SysMqReceiveLog();
        log.setId(IDGenerator.generate(32));
        log.setMsgid(correlationData.getId());
        log.setReceivebeanname(correlationData.getBeanName());
        log.setReceivetime(DateUtil.now());
        log.setCreatetime(DateUtil.now());
        log.setUpdatetime(DateUtil.now());
        log.setCreateuser("system");
        log.setUpdateuser("system");
        return log;
    }

    @Override
    public SysMqSendLog buildSendLog(DefaultCorrelationData correlationData, String status) {
        SysMqSendLog log = new SysMqSendLog();
        log.setId(correlationData.getId());
        log.setSendtime(DateUtil.now());
        log.setMsg(JsonUtils.object2Json(correlationData.getMessage()));
        log.setRoutename(correlationData.getRouteName());
        log.setSendstatus(status);
        log.setSendbeanname(correlationData.getBeanName());
        log.setCreateuser("system");
        log.setCreatetime(DateUtil.now());
        log.setUpdatetime(DateUtil.now());
        log.setUpdateuser("system");
        return log;
    }
}
