package com.playtika.qa.carsshop.service;

import com.playtika.qa.carsshop.dao.entity.*;
import com.playtika.qa.carsshop.dao.entity.repo.AdsEntityRepository;
import com.playtika.qa.carsshop.dao.entity.repo.CarEntityRepository;
import com.playtika.qa.carsshop.domain.Car;
import com.playtika.qa.carsshop.domain.CarInStore;
import com.playtika.qa.carsshop.domain.CarInfo;
import com.playtika.qa.carsshop.web.CarAlreadyOnSalingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CarServiceImplTests {

    @InjectMocks
    private CarServiceImpl service;
    @Mock
    private AdsEntityRepository adsRepository;
    @Mock
    private CarEntityRepository carRepository;

    CarInStore first = new CarInStore(new Car(1L, "xxx"), new CarInfo(1, "Sema"));

    @Test
    public void addNewAdsThenCarIsAbsent() {

        CarEntity carEntity = createCarEntity(first, 1L);
        AdsEntity firstAds = createAdsEntities(first, createUserEntity(first), carEntity);
        firstAds.setId(1L);

        when(carRepository.findByPlateNumber("xxx")).thenReturn(EMPTY_LIST);
        when(adsRepository.save(notNull(AdsEntity.class))).thenReturn(firstAds);
        CarInStore resalt = service.add(first);

        assertThat(resalt.getCar().getPlateNumber(), is("xxx"));
        assertThat(resalt.getCarInfo().getPrice(), is(1));
        assertThat(resalt.getCarInfo().getContact(), is("Sema"));
    }

    @Test
    public void addNewAdsThenCarIsPresentAndClosedAdsExist() {

        CarEntity carEntity = createCarEntity(first, 1L);
        AdsEntity firstAds = createAdsEntities(first, createUserEntity(first), carEntity);
        firstAds.setId(1L);

        when(carRepository.findByPlateNumber("xxx")).thenReturn(asList(carEntity));
        when(adsRepository.findByCarIdAndDealIsNull(1L)).thenReturn(EMPTY_LIST);

        when(adsRepository.save(notNull(AdsEntity.class))).thenReturn(firstAds);
        CarInStore resalt = service.add(first);

        assertThat(resalt.getCar().getPlateNumber(), is("xxx"));
        assertThat(resalt.getCarInfo().getPrice(), is(1));
        assertThat(resalt.getCarInfo().getContact(), is("Sema"));
    }

    @Test(expected = CarAlreadyOnSalingException.class)
    public void addNewAdsThenCarIsPresentAndOpenAdsExistThrowsException() {

        CarEntity carEntity = createCarEntity(first, 1L);
        AdsEntity firstAds = createAdsEntities(first, createUserEntity(first), carEntity);
        firstAds.setId(1L);
        List<AdsEntity> adsList = new ArrayList<>();
        adsList.add(firstAds);

        when(carRepository.findByPlateNumber("xxx")).thenReturn(asList(carEntity));
        when(adsRepository.findByCarIdAndDealIsNull(1L)).thenReturn(adsList);

        when(adsRepository.save(notNull(AdsEntity.class))).thenReturn(firstAds);
        CarInStore resalt = service.add(first);
    }

    @Test
    public void allCarsReturnsListOfAdsIfPresent() {

        CarInStore second = new CarInStore(new Car(2L, "2"), new CarInfo(2, "с2"));

        AdsEntity firstAds = createAdsEntities(first, createUserEntity(first), createCarEntity(first, 1L));
        AdsEntity secondAds = createAdsEntities(second, createUserEntity(second), createCarEntity(second, 2L));

        List<AdsEntity> result = new ArrayList<>();
        result.add(firstAds);
        result.add(secondAds);

        when(adsRepository.findByDealIsNull()).thenReturn(result);
        Collection<CarInStore> allCars = service.getAll();
        assertThat(allCars.size(), is(2));
        assertThat(allCars, hasItem(first));
        assertThat(allCars, hasItem(second));
    }

    @Test
    public void allCarsReturnsEmptyResponseIfRepositoryIsEmpty() {
        when(adsRepository.findByDealIsNull()).thenReturn(EMPTY_LIST);
        assertThat(service.getAll(), is(empty()));
    }

    @Test
    public void getCarReturnsAppropriateCarInfo() {

        AdsEntity firstAds = createAdsEntities(first, createUserEntity(first), createCarEntity(first, 1L));
        when(adsRepository.findByCarIdAndDealIsNull(1)).thenReturn(asList(firstAds));

        assertThat(1, is(service.get(1).get().getCarInfo().getPrice()));
        assertThat("Sema", is(service.get(1).get().getCarInfo().getContact()));
    }

    @Test
    public void getCarReturnsEmptyResponseIfCarIsAbsent() {
        when(adsRepository.findByCarIdAndDealIsNull(1)).thenReturn(EMPTY_LIST);
        assertFalse(service.get(1).isPresent());
    }

    @Test
    public void deleteCarCallsDaoDeleteMethod() {
        service.delete(1L);
        verify(carRepository).delete(1L);
    }

    private AdsEntity createAdsEntities(CarInStore carInStore, UserEntity user, CarEntity car) {
        AdsEntity adsEntity = new AdsEntity(user, car,
                carInStore.getCarInfo().getPrice(), null);
        return adsEntity;
    }

    private CarEntity createCarEntity(CarInStore carInStore, Long id) {
        Car newCar = carInStore.getCar();
        CarEntity carEntity = new CarEntity(newCar.getPlateNumber(),
                newCar.getModel(), newCar.getYear(), newCar.getColor());
        carEntity.setId(id);
        return carEntity;
    }

    private UserEntity createUserEntity(CarInStore carInStore) {
        UserEntity userEntity = new UserEntity("Name", "", carInStore.getCarInfo().getContact());
        return userEntity;
    }
}
