package com.example.servicedispatcher;

import com.example.servicedispatcher.service.UserService;

import java.util.HashMap;

public class RunThis {

    public static void main(String[] args) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", "张三");
        map.put("password", "123456");
        map.put("age", 18);
        String result1 = ServiceDispatcher.execute(UserService.class, "testDto", map);
        String result2 = ServiceDispatcher.execute(UserService.class, "testHashMap", map);
        String result3 = ServiceDispatcher.execute(UserService.class, "testPlainParam", map);
        String result4 = ServiceDispatcher.execute(UserService.class, "testDao", map);
        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);
        System.out.println(result4);
    }
}
