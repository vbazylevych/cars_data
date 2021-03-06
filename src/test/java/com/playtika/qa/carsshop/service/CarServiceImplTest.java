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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.playtika.qa.carsshop.dao.entity.DealEntity.Status.*;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CarServiceImplTest {

    @InjectMocks
    private CarServiceImpl service;
    @Mock
    private AdsEntityRepository adsRepository;
    @Mock
    private CarEntityRepository carRepository;

    @Mock
    private UserEntityRepository userRepository;

    @Mock
    private DealEntityRepository dealRepository;

    CarInStore first = new CarInStore(new Car(1L, "xxx"), new CarInfo(1, "Sema"));
    AdsEntity adsEntity = new AdsEntity();
    User user = new User("kot", "krot", "con1");
    UserEntity userEntity = new UserEntity("kot", "krot", "con1");
    DealEntity dealEntity = new DealEntity(adsEntity, ACTIVATED, userEntity, 100500);
    DealEntity dealEntity2 = new DealEntity(adsEntity, ACTIVATED, userEntity, 100501);

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

    @Test
    public void addNewDeal_returnsId_IfUserAndAdsPresent() {

        dealEntity.setId(1L);

        when(adsRepository.findOne(1L)).thenReturn(adsEntity);
        when(userRepository.findByContact("con1")).thenReturn(asList(userEntity));
        when(dealRepository.save(notNull(DealEntity.class))).thenReturn(dealEntity);

        long result = service.openNewDeal(user, 100500, 1);
        assertThat(result, is(1L));
    }

    @Test
    public void addNewDeal_createNewUserIfUserAbsent_AndReturnsDealId() {

        dealEntity.setId(1L);

        when(adsRepository.findOne(1L)).thenReturn(adsEntity);
        when(userRepository.findByContact("con1")).thenReturn(EMPTY_LIST);
        when(dealRepository.save(notNull(DealEntity.class))).thenReturn(dealEntity);

        long result = service.openNewDeal(user, 100500, 1);
        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(argument.capture());
        assertThat(argument.getAllValues().get(0), samePropertyValuesAs(userEntity));
        assertThat(result, is(1L));
    }

    @Test(expected = NotFoundException.class)
    public void addNewDeal_throwNotFoundException_IfAdsIsAbsent() {
        when(adsRepository.findOne(1L)).thenReturn(null);
        service.openNewDeal(new User(), 100500, 1);
    }

    @Test
    public void rejectDeal_successful_IfDealPresent() {

        dealEntity.setId(1L);

        when(dealRepository.findOne(1L)).thenReturn(dealEntity);
        service.rejectDeal(1);

        ArgumentCaptor<DealEntity> argument = ArgumentCaptor.forClass(DealEntity.class);
        dealEntity.setStatus(REJECTED);
        verify(dealRepository).save(argument.capture());
        assertThat(argument.getAllValues().get(0), samePropertyValuesAs(dealEntity));
    }

    @Test(expected = CantRejectAcceptedDeal.class)
    public void rejectDeal_throwsCantRejectAcceptedDeal_IfDealAlreadyAccepted() {

        dealEntity.setId(1L);
        dealEntity.setStatus(ACCEPTED);

        when(dealRepository.findOne(1L)).thenReturn(dealEntity);
        service.rejectDeal(1);
    }

    @Test(expected = NotFoundException.class)
    public void rejectDeal_throwsNotFoundException_IfDealisAbsent() {
        when(dealRepository.findOne(1L)).thenReturn(null);
        service.rejectDeal(1);
    }

    @Test
    public void acceptBestDeal_ChangeStatusOfDealAsAccepted_IfDealAndAdsPresent() {
        when(adsRepository.findByIdAndDealIsNull(1L)).thenReturn(asList(adsEntity));
        when(dealRepository.findByAdsId(1)).thenReturn(createDealEntityList());

        ArgumentCaptor<DealEntity> argument = ArgumentCaptor.forClass(DealEntity.class);
        dealEntity2.setStatus(ACCEPTED);
        service.acceptTheBestDeal(1L);
        verify(dealRepository).save(argument.capture());
        assertThat(argument.getAllValues().get(0), samePropertyValuesAs(dealEntity2));
    }

    @Test
    public void acceptBestDeal_CloseAdsWithDealId_IfDealAndAdsPresent() {
        when(adsRepository.findByIdAndDealIsNull(1L)).thenReturn(asList(adsEntity));
        when(dealRepository.findByAdsId(1)).thenReturn(createDealEntityList());

        service.acceptTheBestDeal(1L);
        ArgumentCaptor<AdsEntity> adsArgument = ArgumentCaptor.forClass(AdsEntity.class);
        adsEntity.setDeal(dealEntity2);
        verify(adsRepository).save(adsArgument.capture());
        assertThat(adsArgument.getAllValues().get(0), samePropertyValuesAs(adsEntity));
    }

    @Test
    public void acceptBestDeal_ReturnCorrectResponse_IfDealAndAdsPresent() {
        when(adsRepository.findByIdAndDealIsNull(1L)).thenReturn(asList(adsEntity));
        when(dealRepository.findByAdsId(1)).thenReturn(createDealEntityList());

        BestDealResponse bestDealResponse = service.acceptTheBestDeal(1);
        BestDealResponse expectedBestDealResponse = new BestDealResponse(user, 100501, 1);

        assertThat(bestDealResponse.getUser(), samePropertyValuesAs(expectedBestDealResponse.getUser()));
        assertThat(bestDealResponse.getPrice(), is(100501));
        assertThat(bestDealResponse.getId(), is(1L));
    }

    @Test(expected = NotFoundException.class)
    public void acceptBestDeal_ThrowsNotFoundException_IfAdsAbsent() {
        when(adsRepository.findByIdAndDealIsNull(1L)).thenThrow(NotFoundException.class);
        service.acceptTheBestDeal(1);
    }

    @Test(expected = NotFoundException.class)
    public void acceptBestDeal_ThrowsNotFoundException_IfDealAbsent() {
        when(dealRepository.findByAdsId(1L)).thenThrow(NotFoundException.class);
        service.acceptTheBestDeal(1);
    }

    private AdsEntity createAdsEntities(CarInStore carInStore, UserEntity user, CarEntity car) {
        AdsEntity createdAdsEntity = new AdsEntity(user, car,
                carInStore.getCarInfo().getPrice(), null);
        return createdAdsEntity;
    }

    private CarEntity createCarEntity(CarInStore carInStore, Long id) {
        Car newCar = carInStore.getCar();
        CarEntity carEntity = new CarEntity(newCar.getPlateNumber(),
                newCar.getModel(), newCar.getYear(), newCar.getColor());
        carEntity.setId(id);
        return carEntity;
    }

    private UserEntity createUserEntity(CarInStore carInStore) {
        UserEntity createdUserEntity = new UserEntity("Name", "", carInStore.getCarInfo().getContact());
        return createdUserEntity;
    }

    private List<DealEntity> createDealEntityList() {
        List<DealEntity> dealEntityList = new ArrayList<>();
        dealEntity.setId(1L);
        dealEntity2.setId(1L);
        dealEntityList.add(dealEntity);
        dealEntityList.add(dealEntity2);
        return dealEntityList;
    }
}

