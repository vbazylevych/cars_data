package com.playtika.qa.carsshop.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.playtika.qa.carsshop.dao.entity.UserEntity;
import com.playtika.qa.carsshop.dao.entity.repo.UserEntityRepository;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class UserRepositoryTest extends AbstractDaoTest<UserEntityRepository> {
    @Test
    @DataSet(value = "empty-set.xml", disableConstraints = true, useSequenceFiltering = false)
    @ExpectedDataSet("filled-user-table.xml")
    @Commit
    public void userMayBeStored() {
        dao.save(new UserEntity("kot", "krot", "con1"));
    }

    @Test
    @DataSet(value = "filled-user-table.xml", disableConstraints = true, useSequenceFiltering = false)
    public void shouldFindUserByContact() {
        List<UserEntity> result = dao.findByContact("con1");
        result.get(0).setId(null);
        UserEntity expectedResult = new UserEntity("kot", "krot", "con1");
        assertThat(result.get(0), samePropertyValuesAs(expectedResult));
    }
}
