package com.gameCatalog.authservice.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    private final HttpStatus httpStatus;

    private final String errorMessage;

    public AuthenticationException(HttpStatus httpStatus, String errorMessage){
        this.httpStatus=httpStatus;
        this.errorMessage=errorMessage;
    }

    ;

}