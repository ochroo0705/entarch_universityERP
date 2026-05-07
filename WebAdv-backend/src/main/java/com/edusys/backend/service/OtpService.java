package com.edusys.backend.service;

import com.edusys.backend.config.TwilioConfig;
import com.edusys.backend.helper.EmailHelper;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final static Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final EmailHelper emailHelper;
    private final TwilioConfig twilioConfig;
    private final UserRepository userRepository;
    private final boolean mailEnabled;

    // In-memory storage for OTPs (for production, use Redis or database)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    @Autowired
    public OtpService(
            EmailHelper emailHelper,
            TwilioConfig twilioConfig,
            UserRepository userRepository,
            @Value("${app.integrations.mail.enabled:false}") boolean mailEnabled
    ) {
        this.emailHelper = emailHelper;
        this.twilioConfig = twilioConfig;
        this.userRepository = userRepository;
        this.mailEnabled = mailEnabled;
    }

    /**
     * Generate a 6-digit OTP
     */
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP via Email
     */
    public void sendOtpViaEmail(String email, String otp) {
        if (!mailEnabled) {
            throw new RuntimeException("Email OTP is disabled. Please contact administrator.");
        }

        String html = buildOtpEmailTemplate(otp);

        try {
            emailHelper.sendEmail(html, "Таны нэвтрэх нэг удаагийн код", true, email);
            storeOtp(email, otp);
            logger.info("OTP sent successfully to email: {}", maskEmail(email));
        } catch (Exception e) {
            logger.error("Error sending OTP via email [email={}, error={}]", maskEmail(email), e.getMessage());
            throw new RuntimeException("Failed to send OTP via email: " + e.getMessage());
        }
    }

    /**
     * Send OTP via SMS using Twilio
     */
    public void sendOtpViaSms(String phoneNumber, String otp) {
        if (!twilioConfig.isEnabled()) {
            throw new RuntimeException("SMS OTP is disabled. Please contact administrator.");
        }

        if (!twilioConfig.isConfigured()) {
            logger.error("Twilio is enabled but not fully configured. Cannot send SMS.");
            throw new RuntimeException("SMS service is not configured. Please contact administrator.");
        }

        // Normalize phone number: add +976 if it's 8 digits (Mongolian number)
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        logger.debug("Phone number normalized from {} to {}", maskPhoneNumber(phoneNumber), maskPhoneNumber(normalizedPhone));

        String messageBody = String.format("Таны нэвтрэх код: %s\nЭнэ код 5 минутын дараа хүчингүй болно.", otp);

        try {
            Message message = Message.creator(
                    new PhoneNumber(normalizedPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            storeOtp(normalizedPhone, otp);
            logger.info("OTP sent successfully via SMS to: {} [MessageSid: {}]",
                       maskPhoneNumber(normalizedPhone), message.getSid());
        } catch (Exception e) {
            logger.error("Error sending OTP via SMS [phone={}, error={}]",
                        maskPhoneNumber(phoneNumber), e.getMessage());
            throw new RuntimeException("Failed to send OTP via SMS: " + e.getMessage());
        }
    }


    /**
     * Validate OTP
     */
    public boolean validateOtp(String identifier, String otp) {
        // Normalize phone number if it's not an email
        String normalizedIdentifier = identifier.contains("@") ? identifier : normalizePhoneNumber(identifier);

        OtpData storedData = otpStorage.get(normalizedIdentifier);

        if (storedData == null) {
            logger.warn("No OTP found for identifier: {}", maskIdentifier(normalizedIdentifier));
            return false;
        }

        // Check if OTP is expired (5 minutes)
        if (System.currentTimeMillis() - storedData.timestamp > 5 * 60 * 1000) {
            otpStorage.remove(normalizedIdentifier);
            logger.warn("OTP expired for identifier: {}", maskIdentifier(normalizedIdentifier));
            return false;
        }

        if (storedData.otp.equals(otp)) {
            otpStorage.remove(normalizedIdentifier);
            logger.info("OTP validated successfully for: {}", maskIdentifier(normalizedIdentifier));
            return true;
        }

        logger.warn("Invalid OTP for identifier: {}", maskIdentifier(normalizedIdentifier));
        return false;
    }

    /**
     * Get user by email or phone number
     */
    public Optional<User> getUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier);
        } else {
            // Normalize phone number before searching
//            String normalizedPhone = normalizePhoneNumber(identifier);
            return userRepository.findByPhone(identifier);
        }
    }

    /**
     * Normalize phone number: add +976 for 8-digit Mongolian numbers
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return phoneNumber;
        }

        // Remove all spaces and dashes
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        // If it's exactly 8 digits (Mongolian mobile number), add +976
        if (cleaned.matches("^\\d{8}$")) {
            logger.debug("Adding +976 to 8-digit phone number");
            return "+976" + cleaned;
        }

        // If it already has +976 or other country code, return as is
        return cleaned.startsWith("+") ? cleaned : "+" + cleaned;
    }

    /**
     * Store OTP with timestamp
     */
    private void storeOtp(String identifier, String otp) {
        otpStorage.put(identifier, new OtpData(otp, System.currentTimeMillis()));
    }

    /**
     * Evict expired OTPs every 5 minutes to prevent memory leak
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void evictExpiredOtps() {
        long now = System.currentTimeMillis();
        int removed = 0;
        var iterator = otpStorage.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now - entry.getValue().timestamp > 5 * 60 * 1000) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            logger.debug("Evicted {} expired OTPs", removed);
        }
    }

    /**
     * Build HTML email template for OTP
     */
    private String buildOtpEmailTemplate(String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            line-height: 1.6;\n" +
                "            color: #333;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            padding: 20px;\n" +
                "            background-color: #f4f4f4;\n" +
                "        }\n" +
                "        .content {\n" +
                "            background-color: white;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #4CAF50;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            border-radius: 5px 5px 0 0;\n" +
                "        }\n" +
                "        .otp-code {\n" +
                "            font-size: 32px;\n" +
                "            font-weight: bold;\n" +
                "            color: #4CAF50;\n" +
                "            text-align: center;\n" +
                "            padding: 20px;\n" +
                "            background-color: #f0f0f0;\n" +
                "            border-radius: 5px;\n" +
                "            margin: 20px 0;\n" +
                "            letter-spacing: 5px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            margin-top: 20px;\n" +
                "            font-size: 12px;\n" +
                "            color: #666;\n" +
                "        }\n" +
                "        .warning {\n" +
                "            color: #e74c3c;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Нэвтрэх Код</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Сайн байна уу!</h2>\n" +
                "            <p>Таны нэвтрэх нэг удаагийн код:</p>\n" +
                "            <div class=\"otp-code\">" + otp + "</div>\n" +
                "            <p class=\"warning\">Энэ код 5 минутын дараа хүчингүй болно.</p>\n" +
                "            <p>Хэрэв та энэ кодыг хүсээгүй бол энэ имэйлийг үл хэрэгсэх.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Энэ бол автомат илгээсэн имэйл. Хариу бичих шаардлагагүй.</p>\n" +
                "            <p>&copy; 2025 School Management System</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Mask email for logging
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
        return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
    }

    /**
     * Mask phone number for logging
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }

    /**
     * Mask identifier (email or phone) for logging
     */
    private String maskIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return maskEmail(identifier);
        } else {
            return maskPhoneNumber(identifier);
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    @Deprecated
    public void sendOtp(String email, String phone_no) {
        String otp = generateOtp();
        sendOtpViaEmail(email, otp);
        logger.info("Legacy sendOtp method called");
    }

    /**
     * Inner class to store OTP with timestamp
     */
    private static class OtpData {
        String otp;
        long timestamp;

        OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}


