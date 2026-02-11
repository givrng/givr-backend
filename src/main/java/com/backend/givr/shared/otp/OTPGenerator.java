package com.backend.givr.shared.otp;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.Duration;

public class OTPGenerator {
    public static final Duration DURATION = Duration.ofMinutes(10); // Token should be valid for duration

    public static String generateOTP(){
        SecureRandom generator = new SecureRandom();
        int token = generator.nextInt(1000000);
        DecimalFormat format = new DecimalFormat("000000");
        return format.format(token);
    }


    public static void main(String[] args){
        System.out.println(generateOTP());
    }
}
