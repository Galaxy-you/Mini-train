package com.mini12306.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 编号生成工具类
 */
public class CodeGenerator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    
    /**
     * 生成订单号：当前时间+4位随机数
     */
    public static String generateOrderNo() {
        return "ORDER" + DATE_FORMAT.format(new Date()) + String.format("%04d", RANDOM.nextInt(10000));
    }
    
    /**
     * 生成乘车人编号：当前时间+4位随机数
     */
    public static String generateTicketNo() {
        return "TICKET" + DATE_FORMAT.format(new Date()) + String.format("%04d", RANDOM.nextInt(10000));
    }
}