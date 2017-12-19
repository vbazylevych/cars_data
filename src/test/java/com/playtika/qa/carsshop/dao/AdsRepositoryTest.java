package com.playtika.qa.carsshop.dao;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.playtika.qa.carsshop.dao.entity.AdsEntity;
import com.playtika.qa.carsshop.dao.entity.repo.AdsEntityRepository;
import com.playtika.qa.carsshop.dao.entity.CarEntity;
import com.playtika.qa.carsshop.dao.entity.UserEntity;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertTrue;

public class AdsRepositoryTest extends AbstractDaoTest<AdsEntityRepository> {
    @Test
    @DataSet("empty-car.xml")
    @ExpectedDataSet("seved-ads-table.xml")
    @Commit
    public void adsMayBeStored() {
        CarEntity car = new CarEntity("xxx", "opel", 2000, "red");
        UserEntity user = new UserEntity("kot", "krot", "con1");
        AdsEntity ads = new AdsEntity(user, car, 100500, null);
        dao.save(ads);
    }

    @Test
    @DataSet(value = "filled-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void findByCarIdReturnsOpenAdsIfPresent() {
        List<AdsEntity> result = dao.findByCarIdAndDealIsNull(1);
        assertThat(result.get(0).getId(), is(2L));
    }

    @Test
    @DataSet(value = "empty-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void findByCarIdReturnsEmptyListIfTableIsEmpty() {
        assertThat(dao.findByCarIdAndDealIsNull(1), is(empty()));
    }
    @Test
    @DataSet(value = "only-closed-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void findByCarIdReturnsEmptyListIfOnlyClosedAds() {
        assertThat(dao.findByCarIdAndDealIsNull(1), is(empty()));
    }
    @Test
    @DataSet(value = "filled-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void allOpenedAdsWillBeReturned() {
        List<AdsEntity> result = dao.findByDealIsNull();
        assertTrue(result.size() == 2);
        assertTrue(result.get(0).getDeal().isEmpty());
        assertTrue(result.get(1).getDeal().isEmpty());
    }

}
