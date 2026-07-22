package com.example.campshare.rental;

import com.example.campshare.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findAllByUserOrderByCreatedAtDesc(User user);
    boolean existsByGearId(Long gearId);
}
