package com.playtika.qa.carsshop.dao;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.playtika.qa.carsshop.dao.entity.AdsEntity;
import com.playtika.qa.carsshop.dao.entity.DealEntity;
import com.playtika.qa.carsshop.dao.entity.repo.AdsEntityRepository;
import com.playtika.qa.carsshop.dao.entity.CarEntity;
import com.playtika.qa.carsshop.dao.entity.UserEntity;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static com.playtika.qa.carsshop.dao.entity.DealEntity.Status.ACTIVATED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AdsRepositoryTest extends AbstractDaoTest<AdsEntityRepository> {
    @Test
    @DataSet(value = "empty-set.xml", disableConstraints = true, useSequenceFiltering = false)
    @ExpectedDataSet(value = "seved-ads-table.xml")
    @Commit
    public void adsShouldBeStored() {
        CarEntity car = new CarEntity("yyy", "opel", 2000, "red");
        UserEntity user = new UserEntity("kot", "krot", "con1");
        AdsEntity ads = new AdsEntity(user, car, 100500, null);
        DealEntity deal = new DealEntity(ads, ACTIVATED, user, 100500);
        deal.setId(1L);
        ads.setDeal(deal);
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
    @DataSet(value = "empty-set.xml", disableConstraints = true, useSequenceFiltering = false)
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
        assertNull(result.get(0).getDeal());
        assertNull(result.get(1).getDeal());
    }

    @Test
    @DataSet(value = "filled-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void openedAdsWillBeReturned() {
        List<AdsEntity> result = dao.findByIdAndDealIsNull(2);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getId(), is(2L));
    }

    @Test
    @DataSet(value = "filled-ads-table.xml", disableConstraints = true, useSequenceFiltering = false)
    @DBUnit(allowEmptyFields = true)
    public void closedAdsNotReturnedWhenSearchOpen() {
        assertThat(dao.findByIdAndDealIsNull(1), is(empty()));
    }

}
