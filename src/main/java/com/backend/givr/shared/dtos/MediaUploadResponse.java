package com.backend.givr.shared.dtos;

public record MediaUploadResponse(
        String signature, long timestamp,
        long expiresAt, String apiKey,
        String cloudName, String folder,
        String uploadPreset, String resourceType
) {
}
