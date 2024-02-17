package com.yumo.moojbackendjudgeservice.rabbitmq;

import cn.hutool.core.util.StrUtil;
import com.rabbitmq.client.Channel;
import com.yumo.moojbackendcommon.common.ErrorCode;
import com.yumo.moojbackendcommon.config.MessageSendConfig;
import com.yumo.moojbackendcommon.exception.BusinessException;
import com.yumo.moojbackendjudgeservice.judge.JudgeService;
import com.yumo.moojbackendserviceclient.service.QuestionFeignClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MyMessageConsumer {

    private static final String MYMESSAGE_COMSUMER_KEY = "judgeservice:consumer:";

    @Resource
    private JudgeService judgeService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private QuestionFeignClient questionFeignClient;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = MessageSendConfig.SEND_CODE_QUEUE_NAME)
    public void receiveMessage(Message message, Channel channel) {
        Long questionSubmitId = (Long) message.getPayload();
        log.info("receiveMessage message = {}", questionSubmitId);

        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        if (redisTemplate.opsForHash().hasKey(MYMESSAGE_COMSUMER_KEY + questionSubmitId,
                String.valueOf(questionSubmitId))) {
            //存在说明这个消息已经被处理了，手动ack
            //直接丢掉消息
            channel.basicNack(deliveryTag, false, false);
        } else {
            // 收到参数，修改题目提交数 +1
            boolean b = questionFeignClient.addSubmitNum(questionSubmitId);
            if (!b) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改题目提交数失败");
            }
            // 判题
            log.info("开始判题...");
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false);
            //将数据存入redis
            redisTemplate.opsForHash().put(MYMESSAGE_COMSUMER_KEY + questionSubmitId, String.valueOf(questionSubmitId), String.valueOf(questionSubmitId));
            redisTemplate.expire(MYMESSAGE_COMSUMER_KEY + questionSubmitId, 1, TimeUnit.MINUTES);
        }

    }

}