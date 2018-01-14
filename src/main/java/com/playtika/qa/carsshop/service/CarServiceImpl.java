package com.playtika.qa.carsshop.service;


import com.playtika.qa.carsshop.dao.entity.*;
import com.playtika.qa.carsshop.dao.entity.repo.AdsEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.CarEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.DealEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.UserEntityRepository;
import com.playtika.qa.carsshop.domain.Car;
import com.playtika.qa.carsshop.domain.CarInStore;
import com.playtika.qa.carsshop.domain.CarInfo;
import com.playtika.qa.carsshop.domain.User;
import com.playtika.qa.carsshop.web.CantRejectAcceptedDeal;
import com.playtika.qa.carsshop.web.CarAlreadyOnSalingException;
import com.playtika.qa.carsshop.web.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CarServiceImpl implements CarService {


    private AdsEntityRepository adsRepository;
    private CarEntityRepository carRepository;
    private UserEntityRepository userRepository;
    private DealEntityRepository dealRepository;

    @Transactional
    @Override
    public CarInStore add(CarInStore carInStore) {

        List<CarEntity> carEntities = findCarByPlateNumber(carInStore.getCar().getPlateNumber());
        if (carEntities.isEmpty()) {
            CarEntity newCarEntity = createCarEntity(carInStore);
            UserEntity newUserEntity = createUserEntity(carInStore);
            AdsEntity newAds = createAndSaveAdsEntities(carInStore, newUserEntity, newCarEntity);
            setCarId(newAds, carInStore);
            return carInStore;
        }
        CarEntity foundCar = carEntities.get(0);
        if (findOpenAdsByCarId(foundCar.getId()).size() > 0) {
            throw new CarAlreadyOnSalingException("Car already selling!");
        }
        UserEntity newUserEntity = createUserEntity(carInStore);
        AdsEntity newAds = createAndSaveAdsEntities(carInStore, newUserEntity, foundCar);
        setCarId(newAds, carInStore);
        return carInStore;
    }

    @Override
    public Optional<CarInStore> get(long id) {
        return findOpenAdsByCarId(id)
                .stream()
                .findFirst()
                .map(this::getCarInStoreFromAds);
    }

    @Override
    public Collection<CarInStore> getAll() {
        return adsRepository.findByDealIsNull()
                .stream()
                .map(this::getCarInStoreFromAds)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    @Override
    public void delete(long id) {
        carRepository.delete(id);
    }

    private CarInStore getCarInStoreFromAds(AdsEntity ads) {
        CarEntity carEntity = ads.getCar();
        Car car = new Car(carEntity.getId(), carEntity.getPlateNumber(),
                carEntity.getModel(), carEntity.getColor(), carEntity.getYear());
        CarInfo carInfo = new CarInfo(ads.getPrice(), ads.getUser().getContact());
        return new CarInStore(car, carInfo);
    }

    @Override
    public long openNewDeal(User user, int price, long adsId) {
        AdsEntity adsEntity = adsRepository.findOne(adsId);
        if (adsEntity == null) {
            throw new NotFoundException("Ads not found!!!");
        }
        List<UserEntity> foundUser = userRepository.findByContact(user.getContact());
        if (foundUser.isEmpty()) {
            UserEntity userEntity = createAndSaveUserEntity(user);
            DealEntity dealEntity = createAndSaveDealEntity(adsEntity, userEntity, price);
            return dealEntity.getId();
        }
        return createAndSaveDealEntity(adsEntity, foundUser.get(0), price).getId();
    }

    @Override
    @Transactional
    public void rejectDeal(long id) {
        DealEntity foundDeal = dealRepository.findOne(id);
        if (foundDeal == null) {
            throw new NotFoundException("Deal not found!");
        }
        if (foundDeal.getStatus() == DealEntity.Status.ACCEPTED) {
            throw new CantRejectAcceptedDeal("Can't reject accepted deal");
        }
        dealRepository.rejectDeal(id);
    }

    private AdsEntity createAndSaveAdsEntities(CarInStore carInStore, UserEntity user, CarEntity car) {
        AdsEntity adsEntity = new AdsEntity(user, car,
                carInStore.getCarInfo().getPrice(), null);
        return adsRepository.save(adsEntity);

    }

    private CarEntity createCarEntity(CarInStore carInStore) {
        Car newCar = carInStore.getCar();
        CarEntity carEntity = new CarEntity(newCar.getPlateNumber(),
                newCar.getModel(), newCar.getYear(), newCar.getColor());
        return carEntity;
    }

    private UserEntity createUserEntity(CarInStore carInStore) {
        UserEntity userEntity = new UserEntity("Name", "", carInStore.getCarInfo().getContact());
        return userEntity;
    }

    private void setCarId(AdsEntity newAds, CarInStore carInStore) {
        carInStore.getCar().setId(newAds.getCar().getId());
    }

    private List<CarEntity> findCarByPlateNumber(String plateNumber) {
        return carRepository.findByPlateNumber(plateNumber);
    }

    private List<AdsEntity> findOpenAdsByCarId(Long id) {
        return adsRepository.findByCarIdAndDealIsNull(id);
    }

    private DealEntity createAndSaveDealEntity(AdsEntity adsEntity, UserEntity userEntity, int price) {
        DealEntity dealEntity = new DealEntity(adsEntity, DealEntity.Status.ACTIVATED, userEntity, price);
        return dealRepository.save(dealEntity);
    }

    private UserEntity createAndSaveUserEntity(User user) {
        UserEntity userEntity = new UserEntity(user.getName(), user.getSurname(), user.getContact());
        return userRepository.save(userEntity);
    }
}
