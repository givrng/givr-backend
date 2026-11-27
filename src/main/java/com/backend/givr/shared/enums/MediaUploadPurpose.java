package com.backend.givr.shared.enums;


import java.util.List;

public enum MediaUploadPurpose {
    AVATAR(
            "givr/avatar",
            "givr_avatar",
            "image",
            List.of("jpg", "jpeg", "png", "webp")
    ),

    ID_PHOTO(
            "givr/identity",
            "givr_id_photo",
            "image",
            List.of("jpg", "jpeg", "png", "webp")
    ),

    CAC_PHOTO(
            "givr/cac",
            "givr_cac_upload",
            "image",
            List.of("jpg", "jpeg", "png", "webp", "pdf")
    ),

    INITIATIVE_VIDEO(
            "givr/initiatives/videos",
                    "givr_initiative_video",
                    "video",
            List.of("mp4", "mov", "webm")
    ),

    INITIATIVE_FLIER(
            "givr/initiatives/fliers",
                    "givr_initiative_flier",
                    "image",
            List.of("jpg", "jpeg", "png", "webp")
    );

    private final String folder;
    private final String uploadPreset;
    private final String resourceType;
    private final List<String> allowedFormats;
    MediaUploadPurpose(
            String folder,
            String uploadPreset,
            String resourceType,
            List<String> allowedFormats
    ) {
        this.folder = folder;
        this.uploadPreset = uploadPreset;
        this.resourceType = resourceType;
        this.allowedFormats = allowedFormats;
    }

    public String getFolder() {
        return folder;
    }

    public String getUploadPreset() {
        return uploadPreset;
    }

    public String getResourceType() {
        return resourceType;
    }

    public List<String> getAllowedFormats(){
        return allowedFormats;
    }
}
