package com.example.campshare.rental;

import com.example.campshare.gear.Gear;
import com.example.campshare.gear.GearRepository;
import com.example.campshare.user.User;
import com.example.campshare.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RentalService {

    private final UserRepository userRepository;
    private final GearRepository gearRepository;
    private final RentalRepository rentalRepository;

    public RentalService(UserRepository userRepository, GearRepository gearRepository, RentalRepository rentalRepository) {
        this.userRepository = userRepository;
        this.gearRepository = gearRepository;
        this.rentalRepository = rentalRepository;
    }

    @Transactional
    public void reserve(String email, Long gearId, LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new InvalidRentalDateException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        Gear gear = gearRepository.findByIdForUpdate(gearId)
                .orElseThrow(() -> new GearNotFoundException(gearId));

        if (gear.getStockCount() <= 0) {
            throw new OutOfStockException();
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
        int totalPrice = Math.toIntExact(rentalDays * gear.getDailyPrice());
        gear.decreaseStock();
        rentalRepository.save(new Rental(user, gear, startDate, endDate, totalPrice));
    }

    @Transactional(readOnly = true)
    public java.util.List<Rental> findRentalsFor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        java.util.List<Rental> rentals = rentalRepository.findAllByUserOrderByCreatedAtDesc(user);
        rentals.forEach(rental -> rental.getGear().getName());
        return rentals;
    }
}
