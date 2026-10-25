package com.dronemission.clients.dto;

public record ClientResponse(Long id, String type, String name, String document, String email, String phone,
                             String address, String city, boolean active, long missionCount, String userEmail) {
}
