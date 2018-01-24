package com.playtika.qa.carsshop.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FOUND)
public class CarAlreadyOnSalingException extends RuntimeException{
    public CarAlreadyOnSalingException(String message) {
        super(message);
    }
}
