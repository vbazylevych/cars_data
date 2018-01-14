package com.playtika.qa.carsshop.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class User {
    private String name;
    private String surname;
    private String contact;
}
