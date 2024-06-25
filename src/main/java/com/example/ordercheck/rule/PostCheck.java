package com.example.ordercheck.rule;

import com.example.ordercheck.dto.OrderCheckServiceDto;
import com.example.ordercheck.result.Result;

/**
 * 后置医嘱校验执行器，总是最后一个执行
 *
 * @author xxChen
 * @since 2024-6-14 13:35
 */
public class PostCheck implements OrderCheckExecutor {

    @Override
    public void check(OrderCheckServiceDto paramDto, Result result) {

    }
}
