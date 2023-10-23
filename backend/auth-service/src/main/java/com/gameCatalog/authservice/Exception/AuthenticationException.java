package com.gameCatalog.authservice.Exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends RuntimeException {
    private HttpStatus httpStatus;

    private String errorMessage;

    public AuthenticationException(HttpStatus httpStatus, String errorMessage){
        this.httpStatus=httpStatus;
        this.errorMessage=errorMessage;
    }

    public  HttpStatus getHttpStatus(){return httpStatus;};

    public String getErrorMessage(){return errorMessage;}
}
