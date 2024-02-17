package com.yumo.moojbackendquestionservice.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yumo.moojbackendmodel.model.entity.MassageSendLog;
import com.yumo.moojbackendquestionservice.service.MassageSendLogService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-10 20:47
 **/
public class SendCodeJob {


    @Resource
    private MassageSendLogService sendLogService;

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 每隔十秒执行一次
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void messageSend() {
        QueryWrapper<MassageSendLog> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(MassageSendLog::getStatus, 0)
                .le(MassageSendLog::getTryTime, new Date());
        List<MassageSendLog> list = sendLogService.list(qw);
        for (MassageSendLog sendLog : list) {
            sendLog.setUpdateTime(new Date());
            if (sendLog.getTryCount() > 2) {
                //说明已经重试了三次了，此时直接设置消息发送失败
                sendLog.setStatus(2);
                sendLogService.updateById(sendLog);
            }else {
                //还未达到上限，重试
                //更新重试次数
                sendLog.setTryCount(sendLog.getTryCount() + 1);
                sendLogService.updateById(sendLog);
                rabbitTemplate.convertAndSend(sendLog.getExchange(),sendLog.getRouteKey(),sendLog.getQuestionSubmitId(),
                        new CorrelationData(sendLog.getMsgId()));
            }
        }

    }
}
