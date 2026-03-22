package com.backend.givr.shared.email;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface Notification {

    public void sendOtpTo(String receiver, AccountType accountType, OtpPurpose purpose);
    public void sendApplicationRejected(String firstname,String projectName, String organizationName, String recipient);
    public void sendVerificationStatusUpdate(@NotBlank String contactFirstname, String email, ReviewStatus reviewStatus, String reason);
    public void sendParticipationUpdate(String firstname, String projectName, String email, String organizationName, ParticipationStatus status);
    public default void broadcastMessage(String message, List<String> receivers){};

    // Default methods
    public default void sendVolunteerWelcomeMessage(String firstname, String volunteerDashboardUrl, String receiver){};
    public default void sendOrganizationWelcomeMessage(String firstname, String orgDashboardUrl, String receiver){};
    public default void sendPasswordChangeNotificationForOathUser(String email){};
    public default void sendApplicationSubmittedMessage(String firstname,String projectName, String organizationName, String address, String recipient){};
    public default void sendApplicationApproved(String firstname,String projectName, String organizationName, String address, String recipient){};

}
