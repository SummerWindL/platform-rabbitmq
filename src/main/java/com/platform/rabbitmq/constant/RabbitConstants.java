package com.platform.rabbitmq.constant;

/**
 * @author Advance
 * @date 2022年07月17日 11:58
 * @since V1.0.0
 */
public interface RabbitConstants {
    String DLX_QUEUE = "queue.dlx";
    String DLX_EXCHANGE = "exchange.dlx";
    String DLX_ROUTING = "#";

    String STATUS_FAILED = "failed";

    String STATUS_SUCCESS = "success";

    String STATUS_UNKNOWN = "unknown";

    String DEFAULT_ROUTE = "default";

    //交换机类型(01 : Direct, 02 : Fanout, 03 : Topic, 04 : Headers)
    String 交换机类型_Direct = "01";
    String 交换机类型_Fanout = "02";
    String 交换机类型_Topic = "03";
    String 交换机类型_Headers = "04";


    String 是 = "01";
    String 否 = "02";
}
