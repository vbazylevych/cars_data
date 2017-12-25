package com.playtika.qa.carsshop.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;


@Data
@AllArgsConstructor
public class Car {
    private long id;
    @ApiModelProperty(required = true)
    private final String plateNumber;
    @ApiModelProperty(required = true)
    private String model = "";
    @ApiModelProperty(required = true)
    private String color = "";
    @ApiModelProperty(required = true)
    private int year = 1901;

    public Car(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Car(long id, String plateNumber) {
        this.id = id;
        this.plateNumber = plateNumber;
    }
}
