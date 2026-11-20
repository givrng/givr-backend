package com.backend.givr.shared.enums;


public enum MediaUploadPurpose {
    AVATAR(
            "givr/avatar",
            "givr_avatar",
            "image"
    ),

    ID_PHOTO(
            "givr/identity",
            "givr_id_photo",
            "image"
    ),

    REPORT_IMAGE(
            "givr/reports/images",
            "givr_report_image",
            "image"
    ),

    REPORT_VIDEO(
            "givr/reports/videos",
            "givr_report_video",
            "video"
    );

    private final String folder;
    private final String uploadPreset;
    private final String resourceType;

    MediaUploadPurpose(
            String folder,
            String uploadPreset,
            String resourceType
    ) {
        this.folder = folder;
        this.uploadPreset = uploadPreset;
        this.resourceType = resourceType;
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
}
