package com.example.ordercheck.rule;

import com.example.ordercheck.dto.OrderCheckServiceDto;
import com.example.ordercheck.result.Result;

/**
 * 前置医嘱校验执行器，总是第一个执行
 *
 * @author xxChen
 * @since 2024-6-14 13:34
 */
public class PreCheck implements OrderCheckExecutor {

    @Override
    public void check(OrderCheckServiceDto paramDto, Result result) {

    }
}
