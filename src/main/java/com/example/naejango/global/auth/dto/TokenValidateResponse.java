package com.example.naejango.global.auth.dto;

import lombok.Data;

@Data
public class TokenValidateResponse {
    private boolean isValidAccessToken;
    private boolean isValidRefreshToken;
    private String userKey;
}
