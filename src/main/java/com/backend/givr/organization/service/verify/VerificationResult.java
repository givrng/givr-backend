package com.backend.givr.organization.service.verify;

import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class VerificationResult {
    private boolean overallVerified;
    private boolean cacNumberMatch;
    private boolean nameMatch;
    private boolean addressMatch;
    private double nameSimilarity;
    private double addressSimilarity;
    private double confidenceScore;
    private ReviewStatus verificationStatus;
    private List<String> messages = new ArrayList<>();
    // Getters and setters
    public void addMessage(String message) {
        this.messages.add(message);
    }
}
