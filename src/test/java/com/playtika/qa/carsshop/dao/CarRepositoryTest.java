package com.playtika.qa.carsshop.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.playtika.qa.carsshop.dao.entity.CarEntity;
import com.playtika.qa.carsshop.dao.entity.repo.CarEntityRepository;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.samePropertyValuesAs;


public class CarRepositoryTest extends AbstractDaoTest<CarEntityRepository> {

    @Test
    @DataSet(value = "empty-car.xml", disableConstraints = true, useSequenceFiltering = false)
    @ExpectedDataSet("inserted-car-table.xml")
    @Commit
    public void carMayBeStored() {
        dao.save(new CarEntity("test", "opel", 2000, "red"));
    }

    @Test
    @DataSet(value = "filled-car-table.xml", disableConstraints = true, useSequenceFiltering = false)
    public void findByPlatNumberReturnsCarIfPresent() {
        List<CarEntity> result = dao.findByPlateNumber("test");
        CarEntity expectedResult = new CarEntity("test", "opel", 2000, "red");
        expectedResult.setId(1L);
        assertThat(result, hasItem(samePropertyValuesAs(expectedResult)));
    }

    @Test
    @DataSet(value = "no-number-car-table.xml", disableConstraints = true, useSequenceFiltering = false)
    public void ifThereIsNoCarWithSuchPlateNumberEmptyListIsReturned() {
        assertThat(dao.findByPlateNumber("xxx"), is(empty()));
    }

    @Test
    @DataSet(value = "filled-car-table.xml", disableConstraints = true, useSequenceFiltering = false)
    public void deleteCar() {
        dao.delete(1L);
        assertThat(dao.findAll(), is(empty()));
    }


}
