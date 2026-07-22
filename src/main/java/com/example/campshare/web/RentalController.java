package com.example.campshare.web;

import com.example.campshare.gear.Gear;
import com.example.campshare.gear.GearRepository;
import com.example.campshare.rental.InvalidRentalDateException;
import com.example.campshare.rental.OutOfStockException;
import com.example.campshare.rental.RentalForm;
import com.example.campshare.rental.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RentalController {

    private final RentalService rentalService;
    private final GearRepository gearRepository;

    public RentalController(RentalService rentalService, GearRepository gearRepository) {
        this.rentalService = rentalService;
        this.gearRepository = gearRepository;
    }

    @GetMapping("/gears/{gearId}/rentals/new")
    public String reservationForm(@PathVariable Long gearId, Model model) {
        model.addAttribute("gear", findGear(gearId));
        model.addAttribute("rentalForm", new RentalForm());
        return "rental-form";
    }

    @PostMapping("/gears/{gearId}/rentals/new")
    public String reserve(@PathVariable Long gearId,
                          @Valid @ModelAttribute RentalForm rentalForm,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        Gear gear = findGear(gearId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("gear", gear);
            return "rental-form";
        }

        try {
            rentalService.reserve(userDetails.getUsername(), gearId,
                    rentalForm.getStartDate(), rentalForm.getEndDate());
        } catch (OutOfStockException | InvalidRentalDateException exception) {
            model.addAttribute("gear", gear);
            model.addAttribute("errorMessage", exception.getMessage());
            return "rental-form";
        }
        return "redirect:/mypage/rentals?reserved=true";
    }

    @GetMapping("/mypage/rentals")
    public String rentals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("rentals", rentalService.findRentalsFor(userDetails.getUsername()));
        return "rentals";
    }

    private Gear findGear(Long gearId) {
        return gearRepository.findById(gearId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品が見つかりません。"));
    }
}
