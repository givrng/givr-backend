package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.MediaUploadResponse;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;
    @Value("${cloudinary.api.secret}")
    private String cloudinaryApiSecret;
    @Value("${cloudinary.api.key}")
    private String cloudinaryApiKey;
    @Value("${cloudinary.cloud-name}")
    private String cloudinaryCloudName;

    public String uploadImage(byte[] imageBytes,String folder, Object id) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    Map.of(
                            "public_id", String.format("givr/%s/%s", folder, id),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            System.err.printf("Upload failed because %s", e.getLocalizedMessage());
            throw new RuntimeException("Upload failed", e);
        }
    }

    /**
     * Generates a signature for frontend client for upload of files
     */
    public MediaUploadResponse generateSignedUploadKey(String subFolder){
        final long timestamp = Instant.now().getEpochSecond();
        final long expiresAt = Instant.now().plusSeconds(20 * 60).getEpochSecond();
        final String folder = String.format("%s/%s", "/givr", subFolder);

        final Map<String, Object> params = Map.of(
                "timestamp", timestamp,
                "folder", folder
        );

        final String signature = cloudinary.apiSignRequest(params, cloudinaryApiSecret, 2);

        return new MediaUploadResponse(
                signature, timestamp,
                expiresAt, cloudinaryApiKey,
                cloudinaryCloudName, folder
        );

    }
}
