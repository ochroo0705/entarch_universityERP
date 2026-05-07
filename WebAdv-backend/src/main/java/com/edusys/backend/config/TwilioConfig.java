package com.edusys.backend.config;

import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TwilioConfig {

    private static final Logger logger = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${app.integrations.twilio.enabled:false}")
    private boolean enabled;

    @Value("${twilio.accountSid:}")
    private String accountSid;

    @Value("${twilio.authToken:}")
    private String authToken;

    @Value("${twilio.phoneNumber:}")
    private String phoneNumber;

    @PostConstruct
    public void initTwilio() {
        if (!enabled) {
            logger.info("Twilio integration is disabled");
            return;
        }

        // Only initialize if credentials are provided
        if (accountSid != null && !accountSid.isEmpty() &&
            authToken != null && !authToken.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                logger.info("Twilio initialized successfully with Account SID: {}***",
                           accountSid.substring(0, Math.min(10, accountSid.length())));

                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    logger.warn("Twilio phone number is not configured. SMS sending will fail.");
                } else {
                    logger.info("Twilio phone number configured: {}***",
                               phoneNumber.substring(0, Math.min(5, phoneNumber.length())));
                }
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio: {}", e.getMessage());
            }
        } else {
            logger.warn("Twilio is enabled but credentials are incomplete.");
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isConfigured() {
        if (!enabled) {
            return false;
        }

        boolean configured = accountSid != null && !accountSid.isEmpty() &&
               authToken != null && !authToken.isEmpty() &&
               phoneNumber != null && !phoneNumber.isEmpty();

        if (!configured) {
            if (accountSid == null || accountSid.isEmpty()) {
                logger.debug("Twilio not configured: Account SID is missing");
            }
            if (authToken == null || authToken.isEmpty()) {
                logger.debug("Twilio not configured: Auth Token is missing");
            }
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                logger.debug("Twilio not configured: Phone Number is missing");
            }
        }

        return configured;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

