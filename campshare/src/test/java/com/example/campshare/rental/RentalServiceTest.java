package com.example.campshare.rental;

import com.example.campshare.gear.Gear;
import com.example.campshare.gear.GearRepository;
import com.example.campshare.user.User;
import com.example.campshare.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private GearRepository gearRepository;
    @Mock private RentalRepository rentalRepository;
    @InjectMocks private RentalService rentalService;

    @Test
    void reserveDecreasesStockAndSavesRentalWithCalculatedPrice() {
        User user = org.mockito.Mockito.mock(User.class);
        Gear gear = org.mockito.Mockito.mock(Gear.class);
        when(userRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(gearRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(gear));
        when(gear.getStockCount()).thenReturn(3);
        when(gear.getDailyPrice()).thenReturn(2500);

        rentalService.reserve("camper@example.com", 1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3));

        ArgumentCaptor<Rental> savedRental = ArgumentCaptor.forClass(Rental.class);
        verify(gear).decreaseStock();
        verify(rentalRepository).save(savedRental.capture());
        assertThat(savedRental.getValue().getTotalPrice()).isEqualTo(5000);
        assertThat(savedRental.getValue().getStatus()).isEqualTo("RESERVED");
    }

    @Test
    void reserveRejectsOutOfStockGearWithoutSaving() {
        User user = org.mockito.Mockito.mock(User.class);
        Gear gear = org.mockito.Mockito.mock(Gear.class);
        when(userRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(gearRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(gear));
        when(gear.getStockCount()).thenReturn(0);

        assertThatThrownBy(() -> rentalService.reserve("camper@example.com", 1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)))
                .isInstanceOf(OutOfStockException.class);

        verify(gear, never()).decreaseStock();
        verify(rentalRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reserveRejectsInvalidDatesWithoutSaving() {
        assertThatThrownBy(() -> rentalService.reserve("camper@example.com", 1L,
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3)))
                .isInstanceOf(InvalidRentalDateException.class);

        verify(rentalRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findRentalsForInitializesGearForRentalHistoryView() {
        User user = org.mockito.Mockito.mock(User.class);
        Rental rental = org.mockito.Mockito.mock(Rental.class);
        Gear gear = org.mockito.Mockito.mock(Gear.class);
        when(userRepository.findByEmail("camper@example.com")).thenReturn(Optional.of(user));
        when(rentalRepository.findAllByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.List.of(rental));
        when(rental.getGear()).thenReturn(gear);

        rentalService.findRentalsFor("camper@example.com");

        verify(gear).getName();
    }
}
