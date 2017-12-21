package com.playtika.qa.carsshop.web;

import com.playtika.qa.carsshop.domain.Car;
import com.playtika.qa.carsshop.domain.CarInStore;
import com.playtika.qa.carsshop.domain.CarInfo;
import com.playtika.qa.carsshop.service.CarService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@Slf4j
@RestController
@AllArgsConstructor
@Api(description = "Operations pertaining to products in Online Store")
public class CarController {

    private final CarService service;

    @ApiOperation("Opens new ads")
    @PostMapping(value = "/cars", produces = MediaType.APPLICATION_JSON_VALUE)
    public long createCar(@RequestParam("price") int price,
                          @RequestParam("contact") String contactDetails,
                          @RequestBody Car car) {
        log.info("Create new car request was received");

        CarInStore carInStore = new CarInStore(car, new CarInfo(price, contactDetails));
        return service.add(carInStore).getCar().getId();

    }

    @ApiOperation("Returns info about specified car")
    @GetMapping(value = "/cars/{id}")
    public CarInfo getCar(@PathVariable(value = "id") long id) throws NotFoundException {
        log.info("get request with id {} received", id);

        return service.get(id)
                .map(CarInStore::getCarInfo)
                .orElseThrow(() -> new NotFoundException("Can't find car info"));
    }

    @ApiOperation("Returns all opened ads")
    @GetMapping("/cars")
    public Collection<CarInStore> getAllCars() {
        log.info("Get all cars request was received");
        return service.getAll();
    }

    @ApiOperation("Deletes car and related ads")
    @DeleteMapping("cars/{id}")
    public void deleteCars(@PathVariable("id") long id) {
        service.delete(id);
    }
}