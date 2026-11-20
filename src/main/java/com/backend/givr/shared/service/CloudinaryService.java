package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.MediaUploadResponse;
import com.backend.givr.shared.enums.MediaUploadPurpose;
import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
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
    public MediaUploadResponse generateSignedUploadKey(MediaUploadPurpose purpose){
        final long timestamp = Instant.now().getEpochSecond();
        final long expiresAt = Instant.now().plusSeconds(5 * 60).getEpochSecond();
        final String folder = purpose.getFolder();

        final String uploadPreset = purpose.getUploadPreset();

        final Map<String, Object> params = Map.of(
                "timestamp", timestamp,
                "folder", folder,
                "upload_preset", uploadPreset
        );

        final String signature = cloudinary.apiSignRequest(params, cloudinaryApiSecret, 2);

        return new MediaUploadResponse(
                signature, timestamp,
                expiresAt, cloudinaryApiKey,
                cloudinaryCloudName, folder,
                uploadPreset, purpose.getResourceType(),
                purpose.getAllowedFormats()
        );

    }
}
