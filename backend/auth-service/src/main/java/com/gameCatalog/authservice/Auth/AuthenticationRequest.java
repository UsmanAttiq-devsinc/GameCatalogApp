package com.gameCatalog.authservice.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @NotEmpty(message = "Email address is required.")
    @Email(message = "Provide a valid email address.")
    private String email;

    @NotEmpty(message = "Password is required.")
    String password;
}