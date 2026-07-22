package com.example.campshare.web;

import com.example.campshare.config.SecurityConfig;
import com.example.campshare.gear.Gear;
import com.example.campshare.gear.GearRepository;
import com.example.campshare.rental.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RentalController.class)
@Import(SecurityConfig.class)
class RentalControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private RentalService rentalService;
    @MockitoBean private GearRepository gearRepository;

    @Test
    void anonymousUserIsRedirectedToLoginForReservationForm() throws Exception {
        mockMvc.perform(get("/gears/1/rentals/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "camper@example.com")
    void authenticatedUserCanOpenReservationForm() throws Exception {
        Gear gear = org.mockito.Mockito.mock(Gear.class);
        when(gearRepository.findById(1L)).thenReturn(Optional.of(gear));

        mockMvc.perform(get("/gears/1/rentals/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("rental-form"))
                .andExpect(model().attributeExists("rentalForm", "gear"));
    }

    @Test
    @WithMockUser(username = "camper@example.com")
    void myPageShowsOnlyCurrentUsersRentals() throws Exception {
        when(rentalService.findRentalsFor("camper@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/mypage/rentals"))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals"))
                .andExpect(model().attributeExists("rentals"));
    }

    @Test
    @WithMockUser(username = "camper@example.com")
    void reservationRedirectsToMyPageAfterSuccess() throws Exception {
        Gear gear = org.mockito.Mockito.mock(Gear.class);
        when(gearRepository.findById(1L)).thenReturn(Optional.of(gear));

        mockMvc.perform(post("/gears/1/rentals/new")
                        .with(csrf())
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-03"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/rentals?reserved=true"));
    }
}
