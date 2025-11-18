package com.mini12306.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String token;
    private Integer authStatus;
}
