package com.example.servicedispatcher.dao;

import java.util.HashMap;

public class DAOFactory {

    private static HashMap<String, Object> daoCache = new HashMap<>();

    static {
        daoCache.put("userDAO", new UserDAO());
    }

    public static Object getDao(String daoName) {
        return daoCache.get(daoName);
    }
}
