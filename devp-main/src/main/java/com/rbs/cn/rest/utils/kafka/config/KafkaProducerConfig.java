/*
 * File: KafkaProducerConfig.java
 * Created By: fengtao.xue@gausscode.com
 * Date: 2018-08-28
 */

package com.rbs.cn.rest.utils.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author fengtao.xue
 */
@Configuration
@EnableKafka
public class KafkaProducerConfig {
    static Logger logger = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${bootstrap.servers}")
    private String servers;

    //配置基础属性
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        /**
         * Server完成 producer request 前需要确认的数量。 acks=0时，producer不会等待确认，直接添加到socket等待发送；
         * acks=1时，等待leader写到local log就行； acks=all或acks=-1时，等待isr中所有副本确认 （注意：确认都是 broker
         * 接收到消息放入内存就直接返回确认，不是需要等待数据写入磁盘后才返回确认，这也是kafka快的原因）
         */
        // props.put("acks", "all");

        /**
         * Producer可以将发往同一个Partition的数据做成一个Produce
         * Request发送请求，即Batch批处理，以减少请求次数，该值即为每次批处理的大小。
         * 另外每个Request请求包含多个Batch，每个Batch对应一个Partition，且一个Request发送的目的Broker均为这些partition的leader副本。
         * 若将该值设为0，则不会进行批处理
         */
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        /**
         * 默认缓冲可立即发送，即遍缓冲空间还没有满，但是，如果你想减少请求的数量，可以设置linger.ms大于0。
         * 这将指示生产者发送请求之前等待一段时间，希望更多的消息填补到未满的批中。这类似于TCP的算法，例如上面的代码段，
         * 可能100条消息在一个请求发送，因为我们设置了linger(逗留)时间为1毫秒，然后，如果我们没有填满缓冲区，
         * 这个设置将增加1毫秒的延迟请求以等待更多的消息。 需要注意的是，在高负载下，相近的时间一般也会组成批，即使是
         * linger.ms=0。在不处于高负载的情况下，如果设置比0大，以少量的延迟代价换取更少的，更有效的请求。
         */
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        /**
         * 控制生产者可用的缓存总量，如果消息发送速度比其传输到服务器的快，将会耗尽这个缓存空间。
         * 当缓存空间耗尽，其他发送调用将被阻塞，阻塞时间的阈值通过max.block.ms设定， 之后它将抛出一个TimeoutException。
         */
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    //工厂类配置
    private ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    //执行发送消息的模版类
    @Bean
    public KafkaTemplate kafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory(), true);
        return kafkaTemplate;
    }

}
