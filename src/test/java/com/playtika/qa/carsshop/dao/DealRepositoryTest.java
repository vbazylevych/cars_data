package com.playtika.qa.carsshop.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.playtika.qa.carsshop.dao.entity.AdsEntity;
import com.playtika.qa.carsshop.dao.entity.CarEntity;
import com.playtika.qa.carsshop.dao.entity.DealEntity;
import com.playtika.qa.carsshop.dao.entity.UserEntity;
import com.playtika.qa.carsshop.dao.entity.repo.DealEntityRepository;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static com.playtika.qa.carsshop.dao.entity.DealEntity.Status.ACTIVATED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DealRepositoryTest extends AbstractDaoTest<DealEntityRepository> {
    @Test
    @DataSet(value = "empty-set.xml", disableConstraints = true, useSequenceFiltering = false)
    @ExpectedDataSet("inserted-deal-table.xml")
    @Commit
    public void dealMayBeStored() {
        DealEntity deal = initDeal();
        dao.save(deal);
    }

    @Test
    @DataSet(value = "filled-deal-table.xml", disableConstraints = true, useSequenceFiltering = false)
    public void shouldFindDealById() {
        List<DealEntity> result = dao.findByAdsId(1);
        assertThat(result.get(0).getAds().getId(), is(1L));
    }

    DealEntity initDeal() {
        CarEntity car = new CarEntity("yyy", "opel", 2000, "red");
        car.setId(1L);
        UserEntity user = new UserEntity("kot", "krot", "con1");
        user.setId(2L);
        AdsEntity ads = new AdsEntity(user, car, 100500, null);
        ads.setId(1L);
        return new DealEntity(ads, ACTIVATED, user, 100500);
    }
}
