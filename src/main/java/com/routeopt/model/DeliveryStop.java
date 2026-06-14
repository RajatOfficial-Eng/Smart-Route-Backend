package com.routeopt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_stops")
public class DeliveryStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double x;
    private double y;
    private double demand;
    private double timeWindowStart;
    private double timeWindowEnd;
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
