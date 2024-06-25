package com.example.ordercheck.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 医嘱校验规则注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface OrderCheckRule {

    /**
     * 规则名称
     */
    String ruleName();
    /**
     * 使用范围，select,save,import
     */
    String opType();
    /**
     *  校验顺序
     */
    String ordinal();
    /**
     * 住院门诊标记，e=急诊,i=住院,if=输液,o=门诊,空字符串=通用
     */
    String inoutFlag();
    /**
     * 是否可用，Y=可用,N=不可用
     */
    String available();

}
