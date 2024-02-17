package com.yumo.moojbackendjudgeservice.judge;

import com.yumo.moojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.yumo.moojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.yumo.moojbackendjudgeservice.judge.strategy.JudgeContext;
import com.yumo.moojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.yumo.moojbackendmodel.model.codesandbox.JudgeInfo;
import com.yumo.moojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
