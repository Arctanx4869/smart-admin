package com.xinhua.base.common.enumeration;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统环境枚举类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2020-10-15 22:45:04
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */
@AllArgsConstructor
@Getter
public enum SystemEnvironmentEnum implements BaseEnum {

    /** 本机环境，对应 Maven local 和 resources/local。 */
    LOCAL(SystemEnvironmentNameConst.LOCAL, "本机环境"),

    /** 测试环境，按照本项目约定统一命名为 dev。 */
    DEV(SystemEnvironmentNameConst.DEV, "测试环境"),

    /** 生产环境。 */
    PROD(SystemEnvironmentNameConst.PROD, "生产环境");

    private final String value;

    private final String desc;

    public static final class SystemEnvironmentNameConst {

        // 字符串必须与 Maven profile、配置目录名称完全一致。
        public static final String LOCAL = "local";
        public static final String DEV = "dev";
        public static final String PROD = "prod";
    }
}
