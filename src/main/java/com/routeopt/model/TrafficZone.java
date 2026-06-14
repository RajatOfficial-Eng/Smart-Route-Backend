package com.routeopt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "traffic_zones")
public class TrafficZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double x;
    private double y;
    private double radius;
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
