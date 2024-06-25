package com.example.ordercheck.rule;

import com.example.ordercheck.annotation.OrderCheckRule;
import com.example.ordercheck.dto.OrderCheckServiceDto;
import com.example.ordercheck.result.Result;

/**
 * 校验是否全部是自备药
 *
 * @author xxChen
 * @since 2024-6-18 14:41
 */
@OrderCheckRule(ruleName = "CheckIsAllSelfOrder", opType = "save,import", ordinal = "7", inoutFlag = "o", available = "Y")
public class CheckIsAllSelfOrder implements OrderCheckExecutor {

    @Override
    public void check(OrderCheckServiceDto paramDto, Result result) throws Exception {
        result.setCode("1");
    }
}
