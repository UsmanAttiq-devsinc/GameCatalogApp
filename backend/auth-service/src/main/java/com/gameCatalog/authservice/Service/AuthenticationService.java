package com.gameCatalog.authservice.Service;

import com.gameCatalog.authservice.Auth.AuthenticationRequest;
import com.gameCatalog.authservice.Auth.AuthenticationResponse;
import com.gameCatalog.authservice.Auth.RegisterRequest;
import com.gameCatalog.authservice.Exception.AuthenticationException;
import com.gameCatalog.authservice.JWT.JwtService;
import com.gameCatalog.authservice.Model.RefreshToken;
import com.gameCatalog.authservice.Model.Role;
import com.gameCatalog.authservice.Model.User;
import com.gameCatalog.authservice.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;


    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest){

        if (repository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthenticationException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        var user= User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();

        repository.save(user);
        var jwtToken= jwtService.generateToken(user);
        var refreshToken= refreshTokenService.createRefreshToken(user.getEmail());
        return AuthenticationResponse.builder().token(jwtToken).refreshToken(refreshToken.getToken()).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
        }
        catch (Exception e){
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED,"Invalid username/password");
        }
        var user = repository.findByEmail(authenticationRequest.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        Optional<RefreshToken> optRefreshToken=refreshTokenService.findbyUser(user);
        RefreshToken refreshToken;
        if(optRefreshToken.isPresent()){
            try{
                refreshToken=refreshTokenService.verifyExpiration(optRefreshToken.get());
            }
            catch (Exception e){
                refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
            }
        }
        else{
            refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        }
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}