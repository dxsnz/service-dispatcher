package com.example.servicedispatcher.service;

import com.example.servicedispatcher.annotation.MethodName;
import com.example.servicedispatcher.annotation.ParamDto;
import com.example.servicedispatcher.annotation.ParamHashMap;
import com.example.servicedispatcher.dao.UserDAO;
import com.example.servicedispatcher.dto.service.UserServiceDto;
import com.example.servicedispatcher.result.Result;

import java.util.HashMap;

public class UserService {

    public UserDAO userDAO;

    @MethodName("testDto")
    public Result method1(@ParamDto UserServiceDto paremDto) {
        System.out.println(paremDto);
        return Result.success("testDto success");
    }

    @MethodName("testHashMap")
    public Result method2(@ParamHashMap HashMap<String, Object> paramMap) {
        System.out.println(paramMap.toString());
        return Result.success("testHashMap success");
    }

    @MethodName("testPlainParam")
    public Result method3(String userName, String password, Integer age) {
        System.out.println(userName + " " + password + " " + age);
        return Result.success("testPlainParam success");
    }

    @MethodName("testDao")
    public Result method4(String userName, String password, Integer age) {
        userDAO.m1();
        return Result.success("testDao success");
    }
}
