package com.example.campshare.gear;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface GearRepository extends JpaRepository<Gear, Long> {
    List<Gear> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Gear g where g.id = :id")
    Optional<Gear> findByIdForUpdate(@Param("id") Long id);
}
