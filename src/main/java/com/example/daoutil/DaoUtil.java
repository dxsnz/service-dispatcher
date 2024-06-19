package com.example.daoutil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库工具类
 *
 * @author xxChen
 * @since 2023-12-25 15:00
 */
public class DaoUtil {

    /**
     * 执行查询语句，返回Java对象
     * Java对象要求如下
     * @see #getJavaBeanFromResultSet
     */
    public static <T> T search(String sql, List<String> paramList, Class<T> clazz, Connection connection) throws Exception {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            if (paramList != null && !paramList.isEmpty()) {
                for (int i = 0; i < paramList.size(); i++) {
                    pst.setString(i+1, paramList.get(i));
                }
            }
            rs = pst.executeQuery();
            T obj = getJavaBeanFromResultSet(rs, clazz);
            connection.commit();
            return obj;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 执行查询语句，返回Java对象集合
     * Java对象要求如下
     * @see #getJavaBeanFromResultSet
     */
    public static <T> List<T> searchList(String sql, List<String> paramList, Class<T> clazz, Connection connection) throws Exception {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            if (paramList != null && paramList.size() > 0) {
                for (int i = 0; i < paramList.size(); i++) {
                    pst.setString(i+1, paramList.get(i));
                }
            }
            rs = pst.executeQuery();
            List<T> objList = getJavaBeanListFromResultSet(rs, clazz);
            connection.commit();
            return objList;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 将数据库查询结果，组织成一个对象
     * 说明：
     * （1）公有无参构造函数：对象要有公有无参构造函数。
     * （2）下划线转驼峰：数据库字段是下划线，对象属性是驼峰，两者对应则设置值，比如 USER_NAME 对应 userName。属性没有对应的字段，则不设置值。
     * （3）支持字符串、整数、浮点数。其他类型不保证正常注入。
     * （4）属性爆破注入：本方法可以向私有属性注入值，不需要 set 方法。原理反射爆破。
     */
    public static <T> T getJavaBeanFromResultSet(ResultSet rs, Class<T> clazz) throws Exception {
        if (rs == null || clazz == null) {
            return null;
        }
        Map<String, Field> fieldNameAndFieldMap = getFiledNameAndFieldMap(clazz);
        Map<String, Integer> fieldNameAndColumnIndexMap = getFieldNameAndColumnIndexMap(rs);
        if (rs.next()) {
            T obj = clazz.newInstance();
            for (String fieldName : fieldNameAndColumnIndexMap.keySet()) {
                if (fieldNameAndFieldMap.containsKey(fieldName)) {
                    Field field = fieldNameAndFieldMap.get(fieldName);
                    Integer columnIndex = fieldNameAndColumnIndexMap.get(fieldName);
                    setFieldValue(field, obj, rs, columnIndex);
                }
            }
            return obj;
        }
        return null;
    }

    /**
     * 将数据库查询结果，组织成对象集合
     * 方法使用说明如下
     * @see #getJavaBeanFromResultSet
     */
    public static <T> List<T> getJavaBeanListFromResultSet(ResultSet rs, Class<T> clazz) throws Exception {
        if (rs== null || clazz == null) {
            return null;
        }
        Map<String, Field> fieldNameAndFieldMap = getFiledNameAndFieldMap(clazz);
        Map<String, Integer> fieldNameAndColumnIndexMap = getFieldNameAndColumnIndexMap(rs);
        List<T> objList = new ArrayList<>();
        while (rs.next()) {
            T obj = clazz.newInstance();
            for (String fieldName : fieldNameAndColumnIndexMap.keySet()) {
                if (fieldNameAndFieldMap.containsKey(fieldName)) {
                    Field field = fieldNameAndFieldMap.get(fieldName);
                    Integer columnIndex = fieldNameAndColumnIndexMap.get(fieldName);
                    setFieldValue(field, obj, rs, columnIndex);
                }
            }
            objList.add(obj);
        }
        return objList;
    }

    /**
     * 将数据库字段名，转为java对象属性名
     * 将下划线形式的字段名，转为驼峰形式的属性名。比如 USER_ID 转为 userId
     *
     * @return 驼峰形式的属性名，比如 userId
     */
    private static String getFieldNameFromColumnName(String columnName) {
        if (columnName == null || columnName.length() == 0) {
            return "";
        }
        char[] charArray = columnName.toLowerCase().toCharArray();
        StringBuilder fieldName = new StringBuilder();
        boolean isUp = false;
        for (char ch : charArray){
            if (ch == '_') {
                isUp = true;
                continue;
            }
            if (isUp) {
                fieldName.append(Character.toUpperCase(ch));
                isUp = false;
            } else {
                fieldName.append(ch);
            }
        }
        return fieldName.toString();
    }

    /**
     * 获取类的属性
     * 已反射爆破，可以设置私有属性
     *
     * @return 键是属性名称，值是反射的属性对象
     */
    private static <T> Map<String, Field> getFiledNameAndFieldMap(Class<T> clazz) {
        Field[] fieldArray = clazz.getDeclaredFields();
        // 反射爆破，可以设置私有值
        for (Field f : fieldArray) {
            f.setAccessible(true);
        }
        return Arrays.stream(fieldArray).collect(Collectors.toMap(Field::getName, f -> f));
    }

    private static Map<String, Integer> getFieldNameAndColumnIndexMap(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Integer> map = new HashMap<>(columnCount);
        // ResultSet 字段的序号，是从1开始的
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            String fieldName = getFieldNameFromColumnName(columnName);
            map.put(fieldName, i);
        }
        return map;
    }

    /**
     * 设置属性值
     * 支持字符串、整数、浮点数
     */
    private static <T> void setFieldValue(Field field, T obj, ResultSet rs, Integer columnIndex) throws SQLException, IllegalAccessException {
        Class<?> typeClass = field.getType();
        if (typeClass.isAssignableFrom(String.class)) {
            field.set(obj, rs.getString(columnIndex));
        } else if(typeClass.isAssignableFrom(int.class) || typeClass.isAssignableFrom(Integer.class)){
            field.set(obj, rs.getInt(columnIndex));
        } else if(typeClass.isAssignableFrom(float.class) || typeClass.isAssignableFrom(Float.class)){
            field.set(obj, rs.getFloat(columnIndex));
        } else if (typeClass.isAssignableFrom(double.class) || typeClass.isAssignableFrom(Double.class)) {
            field.set(obj, rs.getDouble(columnIndex));
        } else {
            field.set(obj, rs.getObject(columnIndex));
        }
    }
}
