package com.smartFood.donation.controller;

import com.smartFood.donation.models.FoodListing;
import com.smartFood.donation.models.User;
import com.smartFood.donation.service.FoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // 1. Public Home Page Directory (With Search, Filter, and Live Stats)
    @GetMapping("/")
    public String viewHomePage(@RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "foodType", required = false) String foodType,
                               Model model) {
        List<FoodListing> listings = foodService.searchListings(query, foodType);
        model.addAttribute("listings", listings);
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("selectedFoodType", foodType != null ? foodType : "ALL");
        model.addAttribute("stats", foodService.getGlobalStats());
        return "index";
    }

    // 2. Show Food Donation Form (Donors Only)
    @GetMapping("/donate")
    public String showDonateForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null || !"DONOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("listing")) {
            FoodListing listing = new FoodListing();
            listing.setContactPhone(loggedInUser.getPhone());
            model.addAttribute("listing", listing);
        }
        return "donate";
    }

    // 3. Process Food Donation Submission
    @PostMapping("/donate")
    public String saveFoodListing(@ModelAttribute FoodListing listing,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null || !"DONOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        if (listing.getTitle() == null || listing.getTitle().trim().isEmpty() ||
            listing.getLocation() == null || listing.getLocation().trim().isEmpty() ||
            listing.getExpiryTime() == null || listing.getQuantity() <= 0) {
            model.addAttribute("errorMessage", "Please provide a valid title, positive quantity, location, and expiry time.");
            model.addAttribute("listing", listing);
            return "donate";
        }

        try {
            foodService.createListing(listing, loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Food donation listed successfully! NGOs can now claim it.");
            return "redirect:/donor/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create listing: " + e.getMessage());
            model.addAttribute("listing", listing);
            return "donate";
        }
    }

    // 4. Handle NGO Claim Workflow
    @GetMapping("/claim/{id}")
    public String claimFoodListing(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in as an NGO to claim food provisions.");
            return "redirect:/login";
        }

        if (!"NGO".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only registered NGO partners can claim food donations.");
            return "redirect:/";
        }

        boolean claimed = foodService.claimListing(id, loggedInUser);
        if (claimed) {
            redirectAttributes.addFlashAttribute("successMessage", "Food listing claimed successfully! Please coordinate pickup with the donor.");
            return "redirect:/ngo/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "This food listing is no longer available to claim.");
            return "redirect:/";
        }
    }

    // 5. Handle Complete / Pickup Workflow
    @GetMapping("/complete/{id}")
    public String completeFoodListing(@PathVariable Long id,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        boolean completed = foodService.completeListing(id, loggedInUser);
        if (completed) {
            redirectAttributes.addFlashAttribute("successMessage", "Listing successfully marked as Picked Up & Completed! Thank you for reducing waste.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to complete this listing. Please check permissions.");
        }

        return "redirect:/dashboard";
    }

    // 6. Handle Donor Cancel Listing Workflow
    @GetMapping("/cancel/{id}")
    public String cancelFoodListing(@PathVariable Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null || !"DONOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        boolean cancelled = foodService.cancelListing(id, loggedInUser);
        if (cancelled) {
            redirectAttributes.addFlashAttribute("successMessage", "Donation listing has been cancelled.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to cancel listing. It may already be claimed or completed.");
        }

        return "redirect:/donor/dashboard";
    }
}