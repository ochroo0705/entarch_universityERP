package com.edusys.backend.controller;

import com.edusys.backend.dto.*;
import com.edusys.backend.model.User;
import com.edusys.backend.service.JwtService;
import com.edusys.backend.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/otp")
@Tag(name = "OTP", description = "APIs for OTP (One-Time Password) operations via Email and SMS")
public class OtpController {
    private final OtpService otpService;
    private final JwtService jwtService;

    public OtpController(OtpService otpService, JwtService jwtService) {
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-email")
    @Operation(
            summary = "Send OTP via Email",
            description = "Generate and send a 6-digit OTP to the specified email address. OTP expires in 5 minutes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = OtpResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid email format"),
            @ApiResponse(responseCode = "500", description = "Failed to send OTP")
    })
    public ResponseEntity<OtpResponseDTO> sendOtpViaEmail(@Valid @RequestBody SendOtpEmailRequestDTO request) {
        try {
            String otp = otpService.generateOtp();
            otpService.sendOtpViaEmail(request.email(), otp);

            return ResponseEntity.ok(new OtpResponseDTO(
                    true,
                    "OTP sent successfully to your email",
                    maskEmail(request.email())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new OtpResponseDTO(
                    false,
                    "Failed to send OTP: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/send-sms")
    @Operation(
            summary = "Send OTP via SMS",
            description = "Generate and send a 6-digit OTP to the specified phone number using Twilio. Phone number must be in E.164 format (e.g., +976XXXXXXXX). OTP expires in 5 minutes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = OtpResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid phone number format"),
            @ApiResponse(responseCode = "500", description = "Failed to send OTP or SMS service not configured")
    })
    public ResponseEntity<OtpResponseDTO> sendOtpViaSms(@Valid @RequestBody SendOtpSmsRequestDTO request) {
        try {
            String otp = otpService.generateOtp();
            otpService.sendOtpViaSms(request.phoneNumber(), otp);

            return ResponseEntity.ok(new OtpResponseDTO(
                    true,
                    "OTP sent successfully to your phone",
                    maskPhoneNumber(request.phoneNumber())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new OtpResponseDTO(
                    false,
                    "Failed to send OTP: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate OTP and Get JWT Token",
            description = "Validate the OTP code for the given email or phone number. Returns JWT token with user details on success. Each OTP can only be used once and expires after 5 minutes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP validated successfully, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> validateOtp(@Valid @RequestBody ValidateOtpRequestDTO request) {
        boolean isValid = otpService.validateOtp(request.identifier(), request.otp());

        if (isValid) {
            // Get user from database
            Optional<User> userOpt = otpService.getUserByIdentifier(request.identifier());

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new OtpResponseDTO(
                        false,
                        "User not found for this identifier",
                        maskIdentifier(request.identifier())
                ));
            }

            User user = userOpt.get();

            // Extract roles from roleFlags
            List<String> roles = extractRoles(user.getRoleFlags());

            // Get primary role (first role from the list)
            String primaryRole = roles.isEmpty() ? "ROLE_STUDENT" : roles.get(0);

            // Generate JWT token (use username as subject so auth.getName() works across services)
            String token = jwtService.generateToken(user.getId(), user.getUsername(), roles);

            // Return login response with JWT token and single primary role
            LoginResponseDTO response = new LoginResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    primaryRole,
                    roles,
                    token
            );

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(new OtpResponseDTO(
                    false,
                    "Invalid or expired OTP",
                    maskIdentifier(request.identifier())
            ));
        }
    }

    // Helper method to extract roles from roleFlags
    private List<String> extractRoles(Integer roleFlags) {
        List<String> roles = new ArrayList<>();
        int flags = roleFlags == null ? 0 : roleFlags;

        if ((flags & User.ROLE_ADMIN) != 0) roles.add("ROLE_ADMIN");
        if ((flags & User.ROLE_TEACHER) != 0) roles.add("ROLE_TEACHER");
        if ((flags & User.ROLE_STUDENT) != 0) roles.add("ROLE_STUDENT");
        if ((flags & User.ROLE_PARENT) != 0) roles.add("ROLE_PARENT");
        if ((flags & User.ROLE_COUNSELOR) != 0) roles.add("ROLE_COUNSELOR");
        if ((flags & User.ROLE_NURSE) != 0) roles.add("ROLE_NURSE");
        if ((flags & User.ROLE_FINANCE_STAFF) != 0) roles.add("ROLE_FINANCE_STAFF");
        if ((flags & User.ROLE_LIBRARIAN) != 0) roles.add("ROLE_LIBRARIAN");
        if ((flags & User.ROLE_TRANSPORT_COORDINATOR) != 0) roles.add("ROLE_TRANSPORT_COORDINATOR");
        if ((flags & User.ROLE_ADMISSIONS_STAFF) != 0) roles.add("ROLE_ADMISSIONS_STAFF");
        if ((flags & User.ROLE_CAFETERIA_STAFF) != 0) roles.add("ROLE_CAFETERIA_STAFF");

        // Optional fallback if roleFlags is empty
        if (roles.isEmpty()) roles.add("ROLE_STUDENT");

        return roles;
    }

    // Helper methods for masking sensitive information
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
        return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }

    private String maskIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return maskEmail(identifier);
        } else {
            return maskPhoneNumber(identifier);
        }
    }

    // Legacy endpoint for backward compatibility
    @GetMapping
    @Deprecated
    @Operation(summary = "Send OTP (Legacy)", description = "Legacy endpoint - use /send-email or /send-sms instead")
    public String sendOtp() {
        otpService.sendOtp("bilguunerkhembayar3@gmail.com", "86113597");
        return "OTP sent (legacy method)";
    }
}


