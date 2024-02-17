package com.yumo.moojbackendcommon.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @description: 发送代码
 *
 * @author: yumo
 * @create: 2024-01-03 13:38
 **/
@Configuration
public class MessageSendConfig {

    //发送代码的队列
    public static final String SEND_CODE_QUEUE_NAME = "send_code_queue_name";

    //发送代码的交换机
    public static final String SEND_CODE_EXCHANGE_NAME = "send_code_exchange_name";

    @Bean
    Queue codeSendQueue(){
        return new Queue(SEND_CODE_QUEUE_NAME,true,false,false);
    }

    @Bean
    DirectExchange codeSendExchange(){
        return new DirectExchange(SEND_CODE_EXCHANGE_NAME,true,false);
    }

    @Bean
    Binding addFriendSendBinding(){
        return BindingBuilder.bind(codeSendQueue())
                .to(codeSendExchange())
                .with(SEND_CODE_QUEUE_NAME);
    }
}
