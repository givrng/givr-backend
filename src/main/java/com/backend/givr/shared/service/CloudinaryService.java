package com.backend.givr.shared.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(byte[] imageBytes,String folder, Object id) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    Map.of(
                            "public_id", String.format("/givr/%s/%s", folder, id),
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
}
