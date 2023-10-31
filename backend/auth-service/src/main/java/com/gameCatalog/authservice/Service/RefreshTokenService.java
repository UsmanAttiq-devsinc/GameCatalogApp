package com.gameCatalog.authservice.Service;

import com.gameCatalog.authservice.Model.RefreshToken;
import com.gameCatalog.authservice.Model.User;
import com.gameCatalog.authservice.Repository.RefreshTokenRepository;
import com.gameCatalog.authservice.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;



    public Optional<RefreshToken> findbyUser(User user){
        return refreshTokenRepository.findByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token){
        return  refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw  new RuntimeException(token.getToken() + " Refresh token was expired. Please sign-in again");
        }
        return token;
    }

    public RefreshToken createRefreshToken(String username){
        RefreshToken refreshToken=RefreshToken.builder()
                .user(userRepository.findByEmail(username).get())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
