package com.psc.adminbackend.repository;

import com.psc.adminbackend.entity.TravelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long> {}