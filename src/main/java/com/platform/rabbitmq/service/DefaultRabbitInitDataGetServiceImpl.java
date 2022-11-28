package com.platform.rabbitmq.service;

import com.platform.repo.pg.model.base.BasePlpgsqlModel;
import com.platform.repo.pg.model.mq.SysMqExchange;
import com.platform.repo.pg.model.mq.SysMqQueue;
import com.platform.repo.pg.model.mq.SysMqRoute;
import com.platform.repo.pg.repo.sys.rabbitmq.SysRabbitMqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.platform.repo.pg.util.BeanUtil.basePlpgsqlModel2Clz;

/**
 * @author Advance
 * @date 2022年07月17日 12:29
 * @since V1.0.0
 */
@Component
public class DefaultRabbitInitDataGetServiceImpl implements RabbitInitDataGetService{

    @Autowired
    private SysRabbitMqRepository sysRabbitMqRepository;

    @Override
    public List<SysMqQueue> findAllSysMqQueues() {
        BasePlpgsqlModel allSysMqQueues = sysRabbitMqRepository.findAllSysMqQueues(3, 0, Integer.MAX_VALUE);
        SysMqQueue[] sysMqQueues = basePlpgsqlModel2Clz(allSysMqQueues, SysMqQueue[].class);
        if(!ObjectUtils.isEmpty(sysMqQueues)){
            List<SysMqQueue> sysMqQueueList = (List<SysMqQueue>) CollectionUtils.arrayToList(sysMqQueues);
            return sysMqQueueList;
        }
        return null;
    }

    @Override
    public List<SysMqExchange> findAllSysMqExchange() {
        BasePlpgsqlModel allSysMqExchange = sysRabbitMqRepository.findAllSysMqExchange(3, 0, Integer.MAX_VALUE);
        SysMqExchange[] sysMqExchanges = basePlpgsqlModel2Clz(allSysMqExchange, SysMqExchange[].class);
        if(!ObjectUtils.isEmpty(sysMqExchanges)){
            List<SysMqExchange> sysMqExchangeList = (List<SysMqExchange>) CollectionUtils.arrayToList(sysMqExchanges);
            return sysMqExchangeList;
        }
        return null;
    }

    @Override
    public List<SysMqRoute> findAllSysMqRoute() {
        BasePlpgsqlModel allSysMqRoute = sysRabbitMqRepository.findAllSysMqRoute(3, 0, Integer.MAX_VALUE);
        SysMqRoute[] sysMqRoutes = basePlpgsqlModel2Clz(allSysMqRoute, SysMqRoute[].class);
        if(!ObjectUtils.isEmpty(sysMqRoutes)){
            List<SysMqRoute> sysMqRouteList = (List<SysMqRoute>) CollectionUtils.arrayToList(sysMqRoutes);
            return sysMqRouteList;
        }
        return null;
    }
}
