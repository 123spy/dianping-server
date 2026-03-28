package com.spy.server.utils;

import java.lang.reflect.Field;

public class RedisUtil {

    // 这样真的能用吗·
    public static <T> String generateKey(T data)  {
        String key = "";
        try {
            // 使用反射把全部的字段读取出来，然后将其拼接成为一个key
            Class<?> aClass = data.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                // 获取名字
                String fieldName = field.getName();
                // 获取数值
                field.setAccessible(true);
                Object fileValue = field.get(data);
                if(key.isEmpty()) {
                    key = fieldName + "=" +  fileValue;
                } else {
                    key = key + ":" + fieldName + "=" +  fileValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            key = data.toString();
        }
        return key;
    }
}
