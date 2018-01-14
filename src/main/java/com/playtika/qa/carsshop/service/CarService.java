package com.playtika.qa.carsshop.service;

import com.playtika.qa.carsshop.domain.CarInStore;
import com.playtika.qa.carsshop.domain.User;

import java.util.Optional;
import java.util.Collection;


public interface CarService {

    CarInStore add(CarInStore carInStore);

    Optional<CarInStore> get(long id);

    Collection<CarInStore> getAll();

    void delete(long id);

    long openNewDeal(User user, int price, long adsId);

    void rejectDeal(long id);
}
