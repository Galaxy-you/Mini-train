package com.mini12306.dto;

import lombok.Data;

/**
 * 身份认证请求DTO
 */
@Data
public class AuthRequest {
    private String realName;
    private String cardId;
}
