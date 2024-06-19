package com.example.servicedispatcher.dto.service;

public class UserServiceDto {

    public String userName;
    public String password;
    public int age;

    @Override
    public String toString() {
        return "UserServiceDto{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                '}';
    }
}
