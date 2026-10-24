package com.smartFood.donation.Repository;

import com.smartFood.donation.models.FoodListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {
    
    // Fetch listings by status ordered by newest
    List<FoodListing> findByStatusOrderByCreatedAtDesc(String status);

    List<FoodListing> findByStatus(String status);
    
    // Fetch listings posted by a specific donor
    List<FoodListing> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    List<FoodListing> findByDonorId(Long donorId);
    
    // Fetch items claimed by a specific NGO partner
    List<FoodListing> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    List<FoodListing> findByReceiverId(Long receiverId);

    // Search and filter available listings
    @Query("SELECT f FROM FoodListing f WHERE f.status = 'AVAILABLE' AND " +
           "(:foodType IS NULL OR :foodType = '' OR :foodType = 'ALL' OR f.foodType = :foodType) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(f.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.location) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.donor.name) LIKE LOWER(CONCAT('%', :query, '%')))" +
           "ORDER BY f.expiryTime ASC")
    List<FoodListing> searchAvailableListings(@Param("query") String query, @Param("foodType") String foodType);

    // Stats counting
    long countByStatus(String status);
    long countByDonorId(Long donorId);
    long countByDonorIdAndStatus(Long donorId, String status);
    long countByReceiverId(Long receiverId);
    long countByReceiverIdAndStatus(Long receiverId, String status);

    @Query("SELECT COALESCE(SUM(f.quantity), 0) FROM FoodListing f WHERE f.donor.id = :donorId")
    long sumQuantityByDonorId(@Param("donorId") Long donorId);

    @Query("SELECT COALESCE(SUM(f.quantity), 0) FROM FoodListing f WHERE f.receiver.id = :receiverId")
    long sumQuantityByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT COALESCE(SUM(f.quantity), 0) FROM FoodListing f WHERE f.status = 'COMPLETED'")
    long sumCompletedMeals();
}