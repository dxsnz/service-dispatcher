package com.example.ordercheck.rule;

import com.example.ordercheck.dto.OrderCheckServiceDto;
import com.example.ordercheck.result.Result;

/**
 * 医嘱校验执行器
 *
 * @author xxChen
 * @since 2024-6-14 11:38
 */
public interface OrderCheckExecutor {

    void check(OrderCheckServiceDto paramDto, Result result) throws Exception;
}
