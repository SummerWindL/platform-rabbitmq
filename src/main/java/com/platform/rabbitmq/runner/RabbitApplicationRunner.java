package com.platform.rabbitmq.runner;

import com.platform.rabbitmq.constant.RabbitConstants;
import com.platform.rabbitmq.service.RabbitInitDataGetService;
import com.platform.rabbitmq.util.ConfigUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.platform.repo.pg.model.mq.SysMqExchange;
import com.platform.repo.pg.model.mq.SysMqQueue;
import com.platform.repo.pg.model.mq.SysMqRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Advance
 * @date 2022年07月17日 12:22
 * @since V1.0.0
 */
@Component
@Lazy(value = false)
public class RabbitApplicationRunner implements CommandLineRunner, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(RabbitApplicationRunner.class);

    @Autowired
    private AmqpAdmin rabbitAdmin;

    @Autowired(required = false)
    private RabbitInitDataGetService rabbitInitDataGetService;

    @Value("${spring.application.name:all}")
    private String applicationName;

    @Override
    public void run(String... strings) throws Exception {
        // todo 如果有需要,再此方法中将发送失败的消息重新丢入消息队列
        logger.info("Rabbit Client启动完成......");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("初始化MQ基础数据");

        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("application", applicationName);
        // 设置死信队列
        arguments.put("x-dead-letter-exchange", RabbitConstants.DLX_EXCHANGE);
        // 死信队列的声明
        Queue dlxQueue = QueueBuilder.durable(RabbitConstants.DLX_QUEUE).build();
        rabbitAdmin.declareQueue(dlxQueue);
        TopicExchange dlxExchange = (TopicExchange) ExchangeBuilder.topicExchange(RabbitConstants.DLX_EXCHANGE).durable(true).build();
        rabbitAdmin.declareExchange(dlxExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(dlxQueue).to(dlxExchange).with(RabbitConstants.DLX_ROUTING));

        // 根据RabbitInitDataGetService获取的初始化数据，创建队列、路由和绑定关系
        Map<String, Queue> queues = buildQueue(arguments);
        queues.values().forEach(queue -> rabbitAdmin.declareQueue(queue));

        Map<String, Exchange> exchanges = buildExchange(arguments);
        exchanges.values().forEach(exchange -> rabbitAdmin.declareExchange(exchange));

        List<SysMqRoute> routes = rabbitInitDataGetService.findAllSysMqRoute();
        routes.forEach(sysMqRoute -> {
            Exchange exchange = exchanges.get(sysMqRoute.getExchangename());
            Binding binding = null;
            if(exchange instanceof DirectExchange) {
                binding = BindingBuilder.bind(queues.get(sysMqRoute.getQueuename())).to((DirectExchange)exchange).with(sysMqRoute.getRoutename());
            }
            else if(exchange instanceof FanoutExchange) {
                binding = BindingBuilder.bind(queues.get(sysMqRoute.getQueuename())).to((FanoutExchange)exchange);
            }
            else if(exchange instanceof TopicExchange) {
                binding = BindingBuilder.bind(queues.get(sysMqRoute.getQueuename())).to((TopicExchange)exchange).with(sysMqRoute.getRoutename());
            }
            else {
                // TODO
//                binding = BindingBuilder.bind(queues.get(sysMqRoute.getQueueName())).to((HeadersExchange) exchange).where(sysMqRoute.getRouteName()).matches(null);
            }
            rabbitAdmin.declareBinding(binding);
        });
        ConfigUtils.cache(queues, routes);
        logger.info("MQ基础数据初始化完成");
    }

    private Map<String, Queue> buildQueue(Map<String, Object> arguments) {
        List<SysMqQueue> sysMqQueues = rabbitInitDataGetService.findAllSysMqQueues();
        return sysMqQueues.stream().map(queue -> {
            if(logger.isDebugEnabled()){
                logger.debug("queueName:{}, durable:{}, autoDelete:{}, exclusive:{}", queue.getQueuename(), queue.getIsdurable(), queue.getIsautodelete(), queue.getIsexclusive());
            }
            QueueBuilder builder = null;
            if(RabbitConstants.是.equals(queue.getIsdurable())) {
                builder = QueueBuilder.durable(queue.getQueuename());
            }
            else {
                builder = QueueBuilder.nonDurable(queue.getQueuename());
            }
            if(RabbitConstants.是.equals(queue.getIsautodelete())) {
                builder.autoDelete();
            }
            if(RabbitConstants.是.equals(queue.getIsexclusive())) {
                builder.exclusive();
            }
            return builder.withArguments(arguments).build();
        }).collect(Collectors.toMap(Queue::getName, Function.identity()));
    }

    private Map<String, Exchange> buildExchange(Map<String, Object> arguments) {
        List<SysMqExchange> sysMqExchanges = rabbitInitDataGetService.findAllSysMqExchange();
        return sysMqExchanges.stream().map(exchange -> {
            if(logger.isDebugEnabled()){
                logger.debug("type:{}, name:{}, durable:{}, autoDelete:{}", exchange.getExchangetype(), exchange.getExchangename(), true, false);
            }
            ExchangeBuilder builder= null;
            if(RabbitConstants.交换机类型_Headers.equals(exchange.getExchangetype())) {
                builder = ExchangeBuilder.headersExchange(exchange.getExchangename());
            }
            else if(RabbitConstants.交换机类型_Topic.equals(exchange.getExchangetype())) {
                builder = ExchangeBuilder.topicExchange(exchange.getExchangename());
            }
            else if(RabbitConstants.交换机类型_Fanout.equals(exchange.getExchangetype())) {
                builder = ExchangeBuilder.fanoutExchange(exchange.getExchangename());
            }
            else {
                builder = ExchangeBuilder.directExchange(exchange.getExchangename());
            }
            Exchange build = builder.durable(RabbitConstants.是.equals(exchange.getIsdurable())).withArguments(arguments).build();
            return build;
        }).collect(Collectors.toMap(Exchange::getName, Function.identity()));
    }
}
