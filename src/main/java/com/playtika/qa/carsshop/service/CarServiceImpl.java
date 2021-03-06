package com.playtika.qa.carsshop.service;


import com.playtika.qa.carsshop.dao.entity.AdsEntity;
import com.playtika.qa.carsshop.dao.entity.CarEntity;
import com.playtika.qa.carsshop.dao.entity.DealEntity;
import com.playtika.qa.carsshop.dao.entity.UserEntity;
import com.playtika.qa.carsshop.dao.entity.repo.AdsEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.CarEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.DealEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.UserEntityRepository;
import com.playtika.qa.carsshop.domain.*;
import com.playtika.qa.carsshop.web.exceptions.CantRejectAcceptedDeal;
import com.playtika.qa.carsshop.web.exceptions.CarAlreadyOnSalingException;
import com.playtika.qa.carsshop.web.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.playtika.qa.carsshop.dao.entity.DealEntity.Status.ACCEPTED;
import static com.playtika.qa.carsshop.dao.entity.DealEntity.Status.ACTIVATED;

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
            UserEntity newUserEntity = findOrCreateUser(carInStore);
            AdsEntity newAds = createAndSaveAdsEntities(carInStore, newUserEntity, newCarEntity);
            setCarId(newAds, carInStore);
            return carInStore;
        }
        CarEntity foundCar = carEntities.get(0);
        if (findOpenAdsByCarId(foundCar.getId()).size() > 0) {
            throw new CarAlreadyOnSalingException("Car already selling!");
        }
        UserEntity newUserEntity = findOrCreateUser(carInStore);
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

    @Override
    public long openNewDeal(User user, int price, long adsId) {
        AdsEntity adsEntity = adsRepository.findOne(adsId);
        if (adsEntity == null) {
            throw new NotFoundException("Ads not found!");
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
            throw new CantRejectAcceptedDeal("Can't reject accepted deal!");
        }
        foundDeal.setStatus(DealEntity.Status.REJECTED);
        dealRepository.save(foundDeal);
    }

    @Override
    @Transactional
    public BestDealResponse acceptTheBestDeal(long id) {

        List<AdsEntity> openAds = adsRepository.findByIdAndDealIsNull(id);

        if (openAds.isEmpty()) {
            throw new NotFoundException("Open ads with id " + id + " not found!");
        }
        DealEntity bestDeal = findBestDeal(id);
        acceptBesDeal(bestDeal);
        closeAds(openAds.get(0), bestDeal);
        return createBestDealResponse(bestDeal);
    }

    private void acceptBesDeal(DealEntity bestDeal) {
        bestDeal.setStatus(ACCEPTED);
        dealRepository.save(bestDeal);
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
        DealEntity dealEntity = new DealEntity(adsEntity, ACTIVATED, userEntity, price);
        return dealRepository.save(dealEntity);
    }

    private UserEntity createAndSaveUserEntity(User user) {
        UserEntity userEntity = new UserEntity(user.getName(), user.getSurname(), user.getContact());
        return userRepository.save(userEntity);
    }

    private BestDealResponse createBestDealResponse(DealEntity deal) {
        User user = transformUserEntityToUser(deal.getUser());
        return new BestDealResponse(user, deal.getPrice(), deal.getId());
    }

    private User transformUserEntityToUser(UserEntity userEntity) {
        return new User(userEntity.getName(), userEntity.getSurname(), userEntity.getContact());
    }

    private void closeAds(AdsEntity foundAds, DealEntity deal) {
        foundAds.setDeal(deal);
        adsRepository.save(foundAds);
    }

    private DealEntity findBestDeal(long id) {
        return dealRepository.findByAdsId(id).stream()
                .max(Comparator.comparing(DealEntity::getPrice))
                .orElseThrow(() -> new NotFoundException("You haven't any deals!"));
    }

    private CarInStore getCarInStoreFromAds(AdsEntity ads) {
        CarEntity carEntity = ads.getCar();
        Car car = new Car(carEntity.getId(), carEntity.getPlateNumber(),
                carEntity.getModel(), carEntity.getColor(), carEntity.getYear());
        CarInfo carInfo = new CarInfo(ads.getPrice(), ads.getUser().getContact());
        return new CarInStore(car, carInfo);
    }

    private UserEntity findOrCreateUser(CarInStore carInStore) {
        List<UserEntity> userList = userRepository.findByContact(carInStore.getCarInfo().getContact());
        if (userList.isEmpty()) {
            return createUserEntity(carInStore);
        }
        return userList.get(0);
    }
}
