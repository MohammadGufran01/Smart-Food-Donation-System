package com.smartFood.donation.controller;

import com.smartFood.donation.models.FoodListing;
import com.smartFood.donation.models.User;
import com.smartFood.donation.service.FoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final FoodService foodService;

    public DashboardController(FoodService foodService) {
        this.foodService = foodService;
    }

    // Smart router: Redirects users to their specific dashboard based on role
    @GetMapping("/dashboard")
    public String dashboardRouter(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if ("DONOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/donor/dashboard";
        } else if ("NGO".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/ngo/dashboard";
        }

        return "redirect:/";
    }

    // 1. Donor Dedicated Dashboard View
    @GetMapping("/donor/dashboard")
    public String donorDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DONOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        List<FoodListing> myListings = foodService.getListingsByDonor(loggedInUser.getId());
        model.addAttribute("listings", myListings);

        Map<String, Object> stats = foodService.getDonorStats(loggedInUser.getId());
        model.addAttribute("stats", stats);

        return "donor-dashboard";
    }

    // 2. NGO Dedicated Dashboard View
    @GetMapping("/ngo/dashboard")
    public String ngoDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"NGO".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        List<FoodListing> claimedItems = foodService.getListingsByReceiver(loggedInUser.getId());
        model.addAttribute("claimedListings", claimedItems);

        Map<String, Object> stats = foodService.getReceiverStats(loggedInUser.getId());
        model.addAttribute("stats", stats);

        return "reciever-dashboard";
    }
}