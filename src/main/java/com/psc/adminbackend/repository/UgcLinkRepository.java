package com.psc.adminbackend.repository;

import com.psc.adminbackend.entity.UgcLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UgcLinkRepository extends JpaRepository<UgcLink, Long> {
}