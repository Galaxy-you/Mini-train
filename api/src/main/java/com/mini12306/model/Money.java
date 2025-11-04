package com.mini12306.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 金额处理类
 */
public class Money {
    private BigDecimal amount;

    public Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }

    public Money(String amount) {
        this(new BigDecimal(amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double toDouble() {
        return amount.doubleValue();
    }

    public String toString() {
        return amount.toString();
    }

    public String toYuanString() {
        return String.format("￥%.2f", amount.doubleValue());
    }

    public static Money fromYuan(double yuan) {
        return new Money(yuan);
    }

    public static Money fromFen(long fen) {
        return new Money(BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100)));
    }

    public long toFen() {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
    
    /**
     * 默认构造函数
     */
    public Money() {
        this.amount = BigDecimal.ZERO;
    }
    
    /**
     * 设置金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    /**
     * 加法运算
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    /**
     * 减法运算
     */
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    /**
     * 乘法运算
     */
    public Money multiply(double factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }
    
    /**
     * 除法运算
     */
    public Money divide(double divisor) {
        return new Money(this.amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }
    
    /**
     * JPA 转换器，用于自动转换 Money 和数据库字段
     */
    @Converter(autoApply = true)
    public static class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
        @Override
        public BigDecimal convertToDatabaseColumn(Money money) {
            return money == null ? null : money.getAmount();
        }
        
        @Override
        public Money convertToEntityAttribute(BigDecimal amount) {
            return amount == null ? null : new Money(amount);
        }
    }
}
