package com.smartFood.donation.service;

import com.smartFood.donation.Repository.FoodListingRepository;
import com.smartFood.donation.Repository.UserRepository;
import com.smartFood.donation.models.FoodListing;
import com.smartFood.donation.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FoodService {

    private final FoodListingRepository foodListingRepository;
    private final UserRepository userRepository;

    public FoodService(FoodListingRepository foodListingRepository, UserRepository userRepository) {
        this.foodListingRepository = foodListingRepository;
        this.userRepository = userRepository;
    }

    public List<FoodListing> getAvailableListings() {
        return foodListingRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE");
    }

    public List<FoodListing> searchListings(String query, String foodType) {
        if ((query == null || query.trim().isEmpty()) && (foodType == null || foodType.trim().isEmpty() || "ALL".equalsIgnoreCase(foodType))) {
            return getAvailableListings();
        }
        return foodListingRepository.searchAvailableListings(
                query != null ? query.trim() : "",
                foodType != null ? foodType.trim() : ""
        );
    }

    public List<FoodListing> getListingsByDonor(Long donorId) {
        return foodListingRepository.findByDonorIdOrderByCreatedAtDesc(donorId);
    }

    public List<FoodListing> getListingsByReceiver(Long receiverId) {
        return foodListingRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    public Optional<FoodListing> getListingById(Long id) {
        return foodListingRepository.findById(id);
    }

    @Transactional
    public FoodListing createListing(FoodListing listing, User sessionUser) {
        User donor = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor user not found"));

        listing.setDonor(donor);
        listing.setStatus("AVAILABLE");
        listing.setCreatedAt(LocalDateTime.now());
        if (listing.getContactPhone() == null || listing.getContactPhone().trim().isEmpty()) {
            listing.setContactPhone(donor.getPhone());
        }
        return foodListingRepository.save(listing);
    }

    @Transactional
    public boolean claimListing(Long listingId, User sessionUser) {
        Optional<FoodListing> listingOpt = foodListingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            return false;
        }

        FoodListing listing = listingOpt.get();
        if (!"AVAILABLE".equals(listing.getStatus())) {
            return false;
        }

        User receiver = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver user not found"));

        listing.setStatus("CLAIMED");
        listing.setReceiver(receiver);
        foodListingRepository.save(listing);
        return true;
    }

    @Transactional
    public boolean completeListing(Long listingId, User sessionUser) {
        Optional<FoodListing> listingOpt = foodListingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            return false;
        }

        FoodListing listing = listingOpt.get();
        if (!"CLAIMED".equals(listing.getStatus())) {
            return false;
        }

        // Only the receiver (NGO) or donor can mark it completed
        boolean isReceiver = listing.getReceiver() != null && listing.getReceiver().getId().equals(sessionUser.getId());
        boolean isDonor = listing.getDonor() != null && listing.getDonor().getId().equals(sessionUser.getId());

        if (!isReceiver && !isDonor) {
            return false;
        }

        listing.setStatus("COMPLETED");
        foodListingRepository.save(listing);
        return true;
    }

    @Transactional
    public boolean cancelListing(Long listingId, User sessionUser) {
        Optional<FoodListing> listingOpt = foodListingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            return false;
        }

        FoodListing listing = listingOpt.get();
        if (!"AVAILABLE".equals(listing.getStatus())) {
            return false;
        }

        if (listing.getDonor() == null || !listing.getDonor().getId().equals(sessionUser.getId())) {
            return false;
        }

        listing.setStatus("CANCELLED");
        foodListingRepository.save(listing);
        return true;
    }

    public Map<String, Object> getDonorStats(Long donorId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDonations", foodListingRepository.countByDonorId(donorId));
        stats.put("activeCount", foodListingRepository.countByDonorIdAndStatus(donorId, "AVAILABLE"));
        stats.put("claimedCount", foodListingRepository.countByDonorIdAndStatus(donorId, "CLAIMED"));
        stats.put("completedCount", foodListingRepository.countByDonorIdAndStatus(donorId, "COMPLETED"));
        stats.put("totalQuantity", foodListingRepository.sumQuantityByDonorId(donorId));
        return stats;
    }

    public Map<String, Object> getReceiverStats(Long receiverId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClaimed", foodListingRepository.countByReceiverId(receiverId));
        stats.put("pendingPickup", foodListingRepository.countByReceiverIdAndStatus(receiverId, "CLAIMED"));
        stats.put("completedCount", foodListingRepository.countByReceiverIdAndStatus(receiverId, "COMPLETED"));
        stats.put("totalQuantityReceived", foodListingRepository.sumQuantityByReceiverId(receiverId));
        return stats;
    }

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("availableCount", foodListingRepository.countByStatus("AVAILABLE"));
        stats.put("claimedCount", foodListingRepository.countByStatus("CLAIMED"));
        stats.put("completedCount", foodListingRepository.countByStatus("COMPLETED"));
        stats.put("totalMealsSaved", foodListingRepository.sumCompletedMeals());
        return stats;
    }
}
