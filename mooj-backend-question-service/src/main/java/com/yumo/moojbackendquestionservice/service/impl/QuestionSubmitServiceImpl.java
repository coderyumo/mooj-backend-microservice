package com.yumo.moojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yumo.moojbackendcommon.common.ErrorCode;
import com.yumo.moojbackendcommon.config.MessageSendConfig;
import com.yumo.moojbackendcommon.constant.CommonConstant;
import com.yumo.moojbackendcommon.exception.BusinessException;
import com.yumo.moojbackendcommon.utils.SqlUtils;
import com.yumo.moojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yumo.moojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yumo.moojbackendmodel.model.entity.MassageSendLog;
import com.yumo.moojbackendmodel.model.entity.Question;
import com.yumo.moojbackendmodel.model.entity.QuestionSubmit;
import com.yumo.moojbackendmodel.model.entity.User;
import com.yumo.moojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.yumo.moojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.yumo.moojbackendmodel.model.enums.SendLogStatusEnum;
import com.yumo.moojbackendmodel.model.vo.QuestionSubmitVO;
import com.yumo.moojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.yumo.moojbackendquestionservice.rabbitmq.MyMessageProducer;
import com.yumo.moojbackendquestionservice.service.MassageSendLogService;
import com.yumo.moojbackendquestionservice.service.QuestionService;
import com.yumo.moojbackendquestionservice.service.QuestionSubmitService;
import com.yumo.moojbackendserviceclient.service.JudgeFeignClient;
import com.yumo.moojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author yumo
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-08-07 20:58:53
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {
    
    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MassageSendLogService massageSendLogService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();

        // 存储消息发送的信息
        String msgId = UUID.randomUUID().toString();
        MassageSendLog sendLog = new MassageSendLog();
        sendLog.setMsgId(msgId);
        // 题目提交id
        sendLog.setQuestionSubmitId(questionSubmitId);
        // 队列名字
        sendLog.setRouteKey(MessageSendConfig.SEND_CODE_QUEUE_NAME);
        // 交换机名字
        sendLog.setExchange(MessageSendConfig.SEND_CODE_EXCHANGE_NAME);
        // 表示消息正在发送
        sendLog.setStatus(SendLogStatusEnum.Sending.getValue());
        // 表示没重试 最多重试三次
        sendLog.setTryCount(0);
        // 设置重试时间在一分钟之后
        sendLog.setTryTime(new Date(System.currentTimeMillis() + 60 * 1000));
        boolean sendLogSave = massageSendLogService.save(sendLog);
        if (!sendLogSave){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "日志插入失败");
        }
        // 发送消息
        rabbitTemplate.convertAndSend(MessageSendConfig.SEND_CODE_EXCHANGE_NAME,
                MessageSendConfig.SEND_CODE_QUEUE_NAME,questionSubmitId,new CorrelationData(msgId));
        return questionSubmitId;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        List<QuestionSubmitVO> finalQuestionSubmitVOList = questionSubmitVOList.stream()
                .map(questionSubmitVO -> {
                    // 获取状态对应的文本值并设置到对象中
                    String statusText = QuestionSubmitStatusEnum.getEnumByValue(questionSubmitVO.getStatus()).getText();
                    questionSubmitVO.setStatusValue(statusText);
                    return questionSubmitVO;
                })
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(finalQuestionSubmitVOList);
        return questionSubmitVOPage;
    }


}




