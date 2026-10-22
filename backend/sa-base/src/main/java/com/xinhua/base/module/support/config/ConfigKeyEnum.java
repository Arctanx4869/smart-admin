package com.xinhua.base.module.support.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.xinhua.base.common.enumeration.BaseEnum;

@Getter
@AllArgsConstructor
public enum ConfigKeyEnum implements BaseEnum {

    /**
     * 万能密码
     */
    SUPER_PASSWORD("super_password", "万能密码"),

    ;

    private final String value;

    private final String desc;
}
