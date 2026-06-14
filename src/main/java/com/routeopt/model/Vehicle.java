package com.routeopt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double capacity;
    private double fuelConsumptionRate;
    private double costPerKm;
    private double speed;
    private boolean active;

    // Constructors
    public Vehicle() {}

    public Vehicle(Long id, String name, double capacity, double fuelConsumptionRate, double costPerKm, double speed, boolean active) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.costPerKm = costPerKm;
        this.speed = speed;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public double getFuelConsumptionRate() { return fuelConsumptionRate; }
    public void setFuelConsumptionRate(double fuelConsumptionRate) { this.fuelConsumptionRate = fuelConsumptionRate; }

    public double getCostPerKm() { return costPerKm; }
    public void setCostPerKm(double costPerKm) { this.costPerKm = costPerKm; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
