package com.mini12306.dto;

import lombok.Data;

@Data
public class UserQueryRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String keyword;
    private Integer authStatus;
}
