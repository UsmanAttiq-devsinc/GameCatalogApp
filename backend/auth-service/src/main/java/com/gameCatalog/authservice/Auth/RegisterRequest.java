package com.gameCatalog.authservice.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotEmpty
    private String firstname;

    @NotEmpty
    private String lastname;

    @NotEmpty(message = "Email is required.")
    @Email(message = "Email is not well formatted.")
    private String email;

    @NotEmpty
    @Size(min=6, message = "Password should be at least 6 characters.")
    private String password;
}