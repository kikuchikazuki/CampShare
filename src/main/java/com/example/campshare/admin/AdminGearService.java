package com.example.campshare.admin;
import com.example.campshare.gear.Gear;
import com.example.campshare.gear.GearRepository;
import com.example.campshare.rental.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGearService {
 private final GearRepository gears; private final RentalRepository rentals;
 public AdminGearService(GearRepository gears, RentalRepository rentals){this.gears=gears;this.rentals=rentals;}
 @Transactional public void create(AdminGearForm f){gears.save(new Gear(f.getName(),f.getCategory(),f.getDescription(),f.getDailyPrice(),f.getStockCount(),f.getImageUrl()));}
 @Transactional public void update(Long id, AdminGearForm f){Gear g=get(id);g.update(f.getName(),f.getCategory(),f.getDescription(),f.getDailyPrice(),f.getStockCount(),f.getImageUrl());}
 @Transactional public void delete(Long id){if(rentals.existsByGearId(id))throw new GearCannotBeDeletedException(); gears.delete(get(id));}
 public Gear get(Long id){return gears.findById(id).orElseThrow(() -> new IllegalArgumentException("用品が見つかりません。"));}
}
