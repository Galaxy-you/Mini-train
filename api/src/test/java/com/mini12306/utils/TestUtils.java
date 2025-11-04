package com.mini12306.utils;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * 测试工具类
 */
public class TestUtils {
    
    /**
     * 调用实例的私有方法
     * 
     * @param instance 实例对象
     * @param methodName 方法名
     * @param args 方法参数
     * @return 方法返回值
     */
    @SuppressWarnings("unchecked")
    public static <T> T callPrivateMethod(Object instance, String methodName, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(instance, methodName, args);
    }
    
    /**
     * 设置实例的私有字段值
     * 
     * @param instance 实例对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    public static void setPrivateField(Object instance, String fieldName, Object value) {
        ReflectionTestUtils.setField(instance, fieldName, value);
    }
    
    /**
     * 获取实例的私有字段值
     * 
     * @param instance 实例对象
     * @param fieldName 字段名
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object instance, String fieldName) {
        return (T) ReflectionTestUtils.getField(instance, fieldName);
    }
}
