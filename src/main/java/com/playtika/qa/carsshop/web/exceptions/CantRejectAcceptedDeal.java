package com.playtika.qa.carsshop.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class CantRejectAcceptedDeal extends RuntimeException {
    public CantRejectAcceptedDeal(String message) {
        super(message);
    }
}
