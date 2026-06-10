package com.lineage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDTO user;
}
