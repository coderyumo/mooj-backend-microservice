package com.yumo.moojbackendjudgeservice.judge.codesandbox;

import com.yumo.moojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.yumo.moojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
