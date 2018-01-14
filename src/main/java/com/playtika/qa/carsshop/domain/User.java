package com.playtika.qa.carsshop.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@AllArgsConstructor
public class User {
    private String name;
    private String surname;
    private String contact;
}
