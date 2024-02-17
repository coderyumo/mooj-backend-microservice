package com.yumo.moojbackendmodel.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息发送日志表
 * @TableName massage_send_log
 */
@TableName(value ="massage_send_log")
@Data
public class MassageSendLog implements Serializable {
    /**
     * 消息id（uuid）
     */
    @TableId
    private String msgId;

    /**
     * 题目提交id
     */
    private Long questionSubmitId;

    /**
     * 队列名字
     */
    private String routeKey;

    /**
     * 0-发送中 1-发送成功 2-发送失败
     */
    private Integer status;

    /**
     * 交换机名字
     */
    private String exchange;

    /**
     * 重试次数
     */
    private Integer tryCount;

    /**
     * 第一次重试时间
     */
    private Date tryTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}