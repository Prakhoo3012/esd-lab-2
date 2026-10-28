package com.esd.lab2.dto;

import jakarta.validation.constraints.*;

/**
 * S1 — FILL IN MANUALLY (no AI for this slice).
 *
 * This is the request body for POST /api/auth/signup.
 * Add the correct jakarta.validation annotation on each field.
 *
 * Rules:
 * - email : must be a valid email format, required (not blank)
 * - password: required, between 8 and 64 characters
 * - fullName: required (not blank), max 100 characters
 */
public record SignupRequest(

                @NotBlank(message = "Email is required") @Email(message = "Invalid Email format") String email,

                @NotBlank(message = "Password is required") @Size(max = 64, min = 8, message = "Password must be between 8 to 64 characters") String password,

                @NotBlank(message = "Name is required") @Size(max = 100, message = "Max Length can be 100 characters only") String fullName) {
        public SignupRequest {
                if (email != null) {
                        email = email.trim();
                }
        }

}
