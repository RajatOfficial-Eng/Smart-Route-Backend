package com.routeopt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "delivery_stops")
public class DeliveryStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Stop name cannot be blank")
    private String name;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private double x;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private double y;

    @Positive(message = "Demand must be positive")
    private double demand;

    @PositiveOrZero(message = "Time window start must be >= 0")
    private double timeWindowStart;

    @PositiveOrZero(message = "Time window end must be >= 0")
    private double timeWindowEnd;

    @PositiveOrZero(message = "Service time must be >= 0")
    private double serviceTime;

    private String status;

    // Constructors
    public DeliveryStop() {}

    public DeliveryStop(Long id, String name, double x, double y, double demand, double timeWindowStart, double timeWindowEnd, double serviceTime, String status) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.demand = demand;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.serviceTime = serviceTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getDemand() { return demand; }
    public void setDemand(double demand) { this.demand = demand; }

    public double getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(double timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public double getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(double timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public double getServiceTime() { return serviceTime; }
    public void setServiceTime(double serviceTime) { this.serviceTime = serviceTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
