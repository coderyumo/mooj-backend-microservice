package com.yumo.moojbackendjudgeservice.judge.strategy;

import com.yumo.moojbackendmodel.model.codesandbox.JudgeInfo;
import com.yumo.moojbackendmodel.model.dto.question.JudgeCase;
import com.yumo.moojbackendmodel.model.entity.Question;
import com.yumo.moojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
