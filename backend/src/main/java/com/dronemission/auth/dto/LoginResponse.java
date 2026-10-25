package com.dronemission.auth.dto;

public record LoginResponse(String token, String tokenType, long expiresInMinutes, UserInfo user) {
}
