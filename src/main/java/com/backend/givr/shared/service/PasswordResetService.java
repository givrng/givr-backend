package com.backend.givr.shared.service;

import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.volunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private OrganizationService organizationService;

    public void requestPasswordReset(String email, AccountType role){
        switch (role){
            case VOLUNTEER -> {
                volunteerService.requestOtp(email, OtpPurpose.PASSWORD_UPDATE);
            }
            case ORGANIZATION -> {
                organizationService.requestOtp(email, OtpPurpose.PASSWORD_UPDATE);
            }
        }
    }

    public void resetPassword(String email, AccountType role, String newPassword, String otp){
        switch (role){
            case VOLUNTEER -> {
                volunteerService.resetPassword(email, newPassword, otp);
            }
            case ORGANIZATION -> {
                organizationService.resetPassword(email, newPassword, otp);
            }
        }
    }
}
