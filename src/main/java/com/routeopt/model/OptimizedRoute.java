package com.routeopt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "optimized_routes")
public class OptimizedRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(columnDefinition = "TEXT")
    private String stopSequence;

    private double totalDistance;
    private double totalDuration;
    private double totalCost;
    private double totalCo2;

    @Column(columnDefinition = "LONGTEXT")
    private String pathCoordinatesJson;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public OptimizedRoute() {}

    public OptimizedRoute(Long id, Vehicle vehicle, String stopSequence, double totalDistance, double totalDuration, double totalCost, double totalCo2, String pathCoordinatesJson, LocalDateTime createdAt) {
        this.id = id;
        this.vehicle = vehicle;
        this.stopSequence = stopSequence;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.totalCost = totalCost;
        this.totalCo2 = totalCo2;
        this.pathCoordinatesJson = pathCoordinatesJson;
        if (createdAt != null) {
            this.createdAt = createdAt;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public String getStopSequence() { return stopSequence; }
    public void setStopSequence(String stopSequence) { this.stopSequence = stopSequence; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

    public double getTotalDuration() { return totalDuration; }
    public void setTotalDuration(double totalDuration) { this.totalDuration = totalDuration; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getTotalCo2() { return totalCo2; }
    public void setTotalCo2(double totalCo2) { this.totalCo2 = totalCo2; }

    public String getPathCoordinatesJson() { return pathCoordinatesJson; }
    public void setPathCoordinatesJson(String pathCoordinatesJson) { this.pathCoordinatesJson = pathCoordinatesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
