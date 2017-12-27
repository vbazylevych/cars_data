package com.playtika.qa.carsshop.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;


@Data
@AllArgsConstructor
public class Car {
    @ApiModelProperty(hidden = true)
    private long id;
    @ApiModelProperty(required = true, example = "xxx")
    private final String plateNumber;
    @ApiModelProperty(required = true, example = "opel")
    private String model = "";
    @ApiModelProperty(required = true, example = "red")
    private String color = "";
    @ApiModelProperty(required = true, value = ">1900", example = "2000")
    private int year = 1901;

    public Car(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Car(long id, String plateNumber) {
        this.id = id;
        this.plateNumber = plateNumber;
    }
}
