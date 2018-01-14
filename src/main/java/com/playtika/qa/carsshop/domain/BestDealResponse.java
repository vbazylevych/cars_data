package com.playtika.qa.carsshop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BestDealResponse {
    private User user;
    private int price;
    private long id;
}
