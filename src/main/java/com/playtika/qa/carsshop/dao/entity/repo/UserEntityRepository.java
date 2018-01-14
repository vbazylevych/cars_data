package com.playtika.qa.carsshop.dao.entity.repo;

import com.playtika.qa.carsshop.dao.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

   List<UserEntity> findByContact(String contact);
}
