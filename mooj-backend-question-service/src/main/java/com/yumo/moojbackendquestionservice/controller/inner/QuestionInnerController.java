package com.yumo.moojbackendquestionservice.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yumo.moojbackendmodel.model.entity.Question;
import com.yumo.moojbackendmodel.model.entity.QuestionSubmit;
import com.yumo.moojbackendquestionservice.service.QuestionService;
import com.yumo.moojbackendquestionservice.service.QuestionSubmitService;
import com.yumo.moojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @GetMapping("/get/id")
    @Override
    public Question getQuestionById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    @GetMapping("/question_submit/get/id")
    @Override
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @PostMapping("/question_submit/update")
    @Override
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    /**
     * 添加题目提交数
     *
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/add/submitNum")
    public boolean addSubmitNum(@RequestParam("questionSubmitId") long questionSubmitId) {
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Question::getId, questionSubmit.getQuestionId());
        Question question = questionService.getOne(queryWrapper);
        question.setSubmitNum(question.getSubmitNum() + 1);
        return questionService.updateById(question);
    }

    /**
     * 添加题目通过数
     *
     * @param questionId
     * @return
     */
    @PostMapping("/add/acceptedNum")
    public boolean addAcceptedNum(@RequestParam("questionId") long questionId) {
        Question question = questionService.getById(questionId);
        question.setAcceptedNum(question.getAcceptedNum() + 1);
        return questionService.updateById(question);
    }

}
