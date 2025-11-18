package com.mini12306.dto;

import lombok.Data;
import java.util.Date;

@Data
public class AccountDTO {
    private Long id;
    private String username;
    private String realName;
    private String cardId;
    private String phone;
    private Date createTime;
    private Date updateTime;
    private Integer authStatus;
}
