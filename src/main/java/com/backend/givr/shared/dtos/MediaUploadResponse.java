package com.backend.givr.shared.dtos;

import java.util.List;

public record MediaUploadResponse(
        String signature, long timestamp,
        long expiresAt, String apiKey,
        String cloudName, String folder,
        String uploadPreset, String resourceType,
        List<String> allowedFormat
) {
}
