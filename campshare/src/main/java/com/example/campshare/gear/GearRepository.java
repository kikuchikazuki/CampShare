package com.example.campshare.gear;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GearRepository extends JpaRepository<Gear, Long> {
    List<Gear> findAllByOrderByIdAsc();
}
