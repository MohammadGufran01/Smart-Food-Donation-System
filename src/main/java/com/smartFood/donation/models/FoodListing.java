package com.smartFood.donation.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_listings")
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private int quantity;
    private String unit = "Meals"; // e.g. "Meals", "Kg", "Boxes", "Packets"
    private String foodType = "Vegetarian"; // "Vegetarian", "Non-Vegetarian", "Vegan", "Packaged / Dry Food"
    private String location;
    private String contactPhone;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status; // "AVAILABLE", "CLAIMED", "COMPLETED", "CANCELLED"

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "ngo_id")
    private User receiver; // Tracks which NGO claimed this food

    public FoodListing() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
}