package com.playtika.qa.carsshop.dao.entity.repo;


import com.playtika.qa.carsshop.dao.entity.AdsEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface AdsEntityRepository extends JpaRepository<AdsEntity, Long> {
    List<AdsEntity> findByDealIsNull();

    List<AdsEntity> findByCarIdAndDealIsNull(long id);

    List<AdsEntity> findByIdAndDealIsNull(long id);
}
