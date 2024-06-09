package com.example.servicedispatcher;

import com.example.servicedispatcher.dispatcher.ServiceDispatcher;
import com.example.servicedispatcher.service.TestService;

import java.util.HashMap;

public class Test {

    public static void main(String[] args) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", "张三");
        map.put("password", "123456");
        String result = ServiceDispatcher.execute(TestService.class, "testMethod", map);
        System.out.println(result);
    }
}
