package com.fasa.orders.repository;

import com.fasa.orders.entity.ApplicationParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationParameterRepository extends JpaRepository<ApplicationParameterEntity, String> {
}
