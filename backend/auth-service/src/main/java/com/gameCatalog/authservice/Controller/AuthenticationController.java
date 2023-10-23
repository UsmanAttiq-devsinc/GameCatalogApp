package com.gameCatalog.authservice.Controller;

import com.gameCatalog.authservice.Auth.AuthenticationRequest;
import com.gameCatalog.authservice.Auth.AuthenticationResponse;
import com.gameCatalog.authservice.Auth.RefreshTokenRequest;
import com.gameCatalog.authservice.Auth.RegisterRequest;
import com.gameCatalog.authservice.Exception.AuthenticationException;
import com.gameCatalog.authservice.JWT.JwtService;
import com.gameCatalog.authservice.Service.AuthenticationService;
import com.gameCatalog.authservice.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.gameCatalog.authservice.Model.RefreshToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    )
    {
        return  ResponseEntity.ok(authenticationService.register(request));
    }


    @PostMapping("/refreshToken")
    public ResponseEntity<AuthenticationResponse> refeshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .map(refreshToken -> {
                    try{
                        return refreshTokenService.verifyExpiration(refreshToken);
                    }
                    catch (Exception e){
                        throw new AuthenticationException(HttpStatus.BAD_REQUEST,"Refresh Token is Expired");
                    }
                })
                .map(RefreshToken::getUser)
                .map(
                        user -> {
                            String accessToken = jwtService.generateToken(user);
                            return
                                    ResponseEntity.ok(AuthenticationResponse.builder()
                                            .token(accessToken)
                                            .refreshToken(refreshTokenRequest.getToken())
                                            .build());
                        }
                ).orElseThrow(()-> new AuthenticationException(HttpStatus.BAD_REQUEST,"Refresh Token is invalid"));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid AuthenticationRequest request
    ){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}