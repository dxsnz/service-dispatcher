package com.example.servicedispatcher.service;

import com.example.servicedispatcher.annotation.MethodName;
import com.example.servicedispatcher.annotation.ParamDto;
import com.example.servicedispatcher.dao.UserDAO;
import com.example.servicedispatcher.dto.service.test.TestServiceDto;
import com.example.servicedispatcher.result.Result;

public class TestService {

    public UserDAO userDAO;

    @MethodName("testMethod")
    public Result m1(@ParamDto TestServiceDto paremDto) {
        System.out.println("userName: " + paremDto.userName + " ,password: " + paremDto.password);
        userDAO.m1();
        return Result.success("成功返回");
    }
}
