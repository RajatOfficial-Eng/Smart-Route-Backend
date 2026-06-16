package com.routeopt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "traffic_zones")
public class TrafficZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private double x;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private double y;

    @Positive(message = "Radius must be positive")
    private double radius;

    @Positive(message = "Severity must be positive")
    private double severity;

    // Constructors
    public TrafficZone() {}

    public TrafficZone(Long id, double x, double y, double radius, double severity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.severity = severity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public double getSeverity() { return severity; }
    public void setSeverity(double severity) { this.severity = severity; }
}
