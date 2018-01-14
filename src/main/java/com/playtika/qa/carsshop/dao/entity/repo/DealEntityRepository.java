package com.playtika.qa.carsshop.dao.entity.repo;

import com.playtika.qa.carsshop.dao.entity.DealEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DealEntityRepository extends JpaRepository<DealEntity, Long> {
    @Modifying
    @Query("UPDATE DealEntity d SET d.status = 'REJECTED' WHERE d.id = :id")
    void rejectDeal(@Param("id") long id);

    List<DealEntity> findByAdsId(long id);
}
