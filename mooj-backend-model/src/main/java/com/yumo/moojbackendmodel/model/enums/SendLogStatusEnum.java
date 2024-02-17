package com.yumo.moojbackendmodel.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 * @author  yumo
 *  
 */
public enum SendLogStatusEnum {
    //0-发送中 1-发送成功 2-发送失败

    Sending("发送中", 0),
    SEND_SUCCESS("发送中", 1),
    SEND_ERROR("发送失败", 2);

    private final String text;

    private final Integer value;

    SendLogStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SendLogStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SendLogStatusEnum anEnum : SendLogStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
