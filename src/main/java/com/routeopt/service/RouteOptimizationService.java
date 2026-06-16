package com.routeopt.service;

import com.routeopt.model.*;
import com.routeopt.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationService.class);

    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DeliveryStopRepository deliveryStopRepository;
    @Autowired
    private TrafficZoneRepository trafficZoneRepository;
    @Autowired
    private OptimizedRouteRepository optimizedRouteRepository;

    @Autowired
    private AStarPathfinder pathfinder;
    @Autowired
    private GeneticAlgorithmSolver gaSolver;
    @Autowired
    private SimulatedAnnealingSolver saSolver;

    @PostConstruct
    public void init() {
        // Automatically seed data if db is empty
        if (depotRepository.count() == 0) {
            log.info("Database empty on startup. Triggering auto-seeding...");
            seedDatabase();
        }
    }

    public void seedDatabase() {
        log.info("Seeding database with default Delhi/NCR data hubs, vehicles, stops, and traffic zones...");
        // Clear existing
        optimizedRouteRepository.deleteAll();
        deliveryStopRepository.deleteAll();
        vehicleRepository.deleteAll();
        depotRepository.deleteAll();
        trafficZoneRepository.deleteAll();

        // Create Depot Hub in New Delhi Center (Lng 77.25, Lat 28.60)
        Depot depot = new Depot(null, "Main Distribution Center", 77.25, 28.60);
        depotRepository.save(depot);

        // Create Vehicles
        vehicleRepository.save(new Vehicle(null, "Ford Transit (Electric)", 30.0, 15.0, 1.2, 40.0, true));
        vehicleRepository.save(new Vehicle(null, "Mercedes Sprinter (Diesel)", 80.0, 25.0, 2.5, 30.0, true));
        vehicleRepository.save(new Vehicle(null, "Toyota HiAce (Gas)", 45.0, 18.0, 1.6, 35.0, true));
        vehicleRepository.save(new Vehicle(null, "Delivery E-Bike", 15.0, 8.0, 0.8, 50.0, true));

        // Create Delivery Stops with real-world Lng (X) / Lat (Y) values mapped to Delhi/NCR
        deliveryStopRepository.save(new DeliveryStop(null, "Downtown Retail Branch", 77.375, 28.70, 10.0, 9.0, 12.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Uptown Warehouse", 77.10, 28.72, 20.0, 10.0, 14.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Eastside Clinic", 77.40, 28.50, 15.0, 8.0, 11.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Westside Pharmacy", 77.125, 28.48, 25.0, 13.0, 17.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Central Tech Office", 77.275, 28.66, 8.0, 9.5, 13.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Industrial Park Factory", 77.425, 28.58, 35.0, 11.0, 15.0, 0.50, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Suburb Residential Complex", 77.075, 28.64, 12.0, 8.5, 12.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Harbor Port Terminal", 77.225, 28.46, 18.0, 14.0, 18.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Shopping Mall Plaza", 77.325, 28.54, 14.0, 10.0, 16.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Regional Hospital Center", 77.175, 28.70, 5.0, 9.0, 11.0, 0.25, "PENDING"));

        // Create initial traffic zones (one bottleneck, one complete roadblock) in degrees
        trafficZoneRepository.save(new TrafficZone(null, 77.25, 28.52, 0.036, 5.0)); // Heavy traffic bottleneck (~3.5km radius)
        trafficZoneRepository.save(new TrafficZone(null, 77.175, 28.62, 0.027, 999.0)); // Complete roadblock (~2.7km radius)
    }

    public void clearWorkspace() {
        log.info("Clearing optimized routes, delivery stops, vehicles, and traffic zones...");
        optimizedRouteRepository.deleteAll();
        deliveryStopRepository.deleteAll();
        vehicleRepository.deleteAll();
        trafficZoneRepository.deleteAll();
        // Ensure at least one depot exists
        if (depotRepository.count() == 0) {
            depotRepository.save(new Depot(null, "Main Distribution Center", 77.25, 28.60));
        }
    }

    public List<OptimizedRoute> optimizeRoutes(String algorithm) {
        log.info("Starting route optimization using algorithm: {}", algorithm);
        // Fetch inputs
        List<Depot> depots = depotRepository.findAll();
        if (depots.isEmpty()) return new ArrayList<>();
        Depot depot = depots.get(0);

        List<Vehicle> activeVehicles = vehicleRepository.findByActiveTrue();
        List<DeliveryStop> pendingStops = deliveryStopRepository.findAll();
        List<TrafficZone> trafficZones = trafficZoneRepository.findAll();

        if (activeVehicles.isEmpty() || pendingStops.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Determine bounding box for dynamic coordinates scaling
        double minLng = depot.getX();
        double maxLng = depot.getX();
        double minLat = depot.getY();
        double maxLat = depot.getY();

        for (DeliveryStop stop : pendingStops) {
            minLng = Math.min(minLng, stop.getX());
            maxLng = Math.max(maxLng, stop.getX());
            minLat = Math.min(minLat, stop.getY());
            maxLat = Math.max(maxLat, stop.getY());
        }
        for (TrafficZone zone : trafficZones) {
            minLng = Math.min(minLng, zone.getX());
            maxLng = Math.max(maxLng, zone.getX());
            minLat = Math.min(minLat, zone.getY());
            maxLat = Math.max(maxLat, zone.getY());
        }

        // Add 10% padding to prevent zero spans and allow margins
        double lngSpan = maxLng - minLng;
        double latSpan = maxLat - minLat;

        if (lngSpan <= 0.0001) {
            minLng -= 0.05;
            maxLng += 0.05;
            lngSpan = 0.1;
        } else {
            double padding = lngSpan * 0.1;
            minLng -= padding;
            maxLng += padding;
            lngSpan = maxLng - minLng;
        }

        if (latSpan <= 0.0001) {
            minLat -= 0.05;
            maxLat += 0.05;
            latSpan = 0.1;
        } else {
            double padding = latSpan * 0.1;
            minLat -= padding;
            maxLat += padding;
            latSpan = maxLat - minLat;
        }

        final double fMinLng = minLng;
        final double fLngSpan = lngSpan;
        final double fMinLat = minLat;
        final double fLatSpan = latSpan;

        // 2. Scale traffic zones to 0-100 range
        List<TrafficZone> scaledTrafficZones = new ArrayList<>();
        for (TrafficZone zone : trafficZones) {
            double sx = (zone.getX() - fMinLng) / fLngSpan * 100.0;
            double sy = (zone.getY() - fMinLat) / fLatSpan * 100.0;
            double sRadius = (zone.getRadius() / fLngSpan) * 100.0;
            scaledTrafficZones.add(new TrafficZone(zone.getId(), sx, sy, sRadius, zone.getSeverity()));
        }

        int n = pendingStops.size();
        int totalNodes = n + 1;

        // Cost and duration matrices (A* paths are calculated on scaled coordinates)
        double[][] costMatrix = new double[totalNodes][totalNodes];
        double[][] durationMatrix = new double[totalNodes][totalNodes];

        double[] xScaled = new double[totalNodes];
        double[] yScaled = new double[totalNodes];

        xScaled[0] = (depot.getX() - fMinLng) / fLngSpan * 100.0;
        yScaled[0] = (depot.getY() - fMinLat) / fLatSpan * 100.0;

        for (int i = 1; i <= n; i++) {
            DeliveryStop stop = pendingStops.get(i - 1);
            xScaled[i] = (stop.getX() - fMinLng) / fLngSpan * 100.0;
            yScaled[i] = (stop.getY() - fMinLat) / fLatSpan * 100.0;
        }

        // Populate matrices via A* pathfinding on scaled grid
        for (int i = 0; i < totalNodes; i++) {
            for (int j = 0; j < totalNodes; j++) {
                if (i == j) {
                    costMatrix[i][j] = 0.0;
                    durationMatrix[i][j] = 0.0;
                } else {
                    List<double[]> legPath = pathfinder.findPath(xScaled[i], yScaled[i], xScaled[j], yScaled[j], scaledTrafficZones);
                    double scaledCost = pathfinder.getPathCost(legPath, scaledTrafficZones);
                    costMatrix[i][j] = scaledCost;
                    
                    // Compute real-world distance in km for travel time estimation
                    double realDistKm = 0.0;
                    for (int p = 0; p < legPath.size() - 1; p++) {
                        double[] p1 = legPath.get(p);
                        double[] p2 = legPath.get(p + 1);
                        double rx1 = fMinLng + (p1[0] / 100.0) * fLngSpan;
                        double ry1 = fMinLat + (p1[1] / 100.0) * fLatSpan;
                        double rx2 = fMinLng + (p2[0] / 100.0) * fLngSpan;
                        double ry2 = fMinLat + (p2[1] / 100.0) * fLatSpan;
                        realDistKm += calculateRealDistanceKm(ry1, rx1, ry2, rx2);
                    }
                    
                    // Duration = distance (km) / speed (km/h)
                    // Fallback to average speed 40km/h
                    durationMatrix[i][j] = realDistKm / 40.0;
                }
            }
        }

        // Solve VRP
        GeneticAlgorithmSolver.Individual bestSolution;
        if ("SA".equalsIgnoreCase(algorithm)) {
            bestSolution = saSolver.solve(costMatrix, durationMatrix, activeVehicles, pendingStops, xScaled[0], yScaled[0]);
        } else {
            bestSolution = gaSolver.solve(costMatrix, durationMatrix, activeVehicles, pendingStops, xScaled[0], yScaled[0]);
        }

        // Clear existing optimization results
        optimizedRouteRepository.deleteAll();

        // Convert best individual into OptimizedRoute entities (saving real Lat/Lng)
        List<OptimizedRoute> savedRoutes = new ArrayList<>();
        
        for (int v = 0; v < activeVehicles.size(); v++) {
            Vehicle vehicle = activeVehicles.get(v);
            List<Integer> stopIndices = bestSolution.routes.get(v);

            if (stopIndices == null || stopIndices.isEmpty()) {
                continue;
            }

            List<double[]> fullRealCoordsPath = new ArrayList<>();
            StringBuilder sequenceBuilder = new StringBuilder();

            double lastRealX = depot.getX();
            double lastRealY = depot.getY();
            
            fullRealCoordsPath.add(new double[]{lastRealX, lastRealY});

            double totalDistanceKm = 0.0;
            double currentTime = 8.0;

            for (int idx = 0; idx < stopIndices.size(); idx++) {
                int stopNodeIdx = stopIndices.get(idx);
                DeliveryStop stop = pendingStops.get(stopNodeIdx - 1);

                if (idx > 0) sequenceBuilder.append(",");
                sequenceBuilder.append(stop.getId());

                // Run pathfinder on scaled coords
                double sxLast = (lastRealX - fMinLng) / fLngSpan * 100.0;
                double syLast = (lastRealY - fMinLat) / fLatSpan * 100.0;
                double sxStop = (stop.getX() - fMinLng) / fLngSpan * 100.0;
                double syStop = (stop.getY() - fMinLat) / fLatSpan * 100.0;

                List<double[]> leg = pathfinder.findPath(sxLast, syLast, sxStop, syStop, scaledTrafficZones);
                
                // Translate scaled path back to real Lat/Lng and append to full route coordinates
                for (int p = 1; p < leg.size(); p++) {
                    double[] pt = leg.get(p);
                    double rx = fMinLng + (pt[0] / 100.0) * fLngSpan;
                    double ry = fMinLat + (pt[1] / 100.0) * fLatSpan;
                    fullRealCoordsPath.add(new double[]{rx, ry});
                }

                // Compute real leg distance and travel time
                double legRealDist = 0.0;
                for (int p = 0; p < leg.size() - 1; p++) {
                    double[] pt1 = leg.get(p);
                    double[] pt2 = leg.get(p + 1);
                    double rx1 = fMinLng + (pt1[0] / 100.0) * fLngSpan;
                    double ry1 = fMinLat + (pt1[1] / 100.0) * fLatSpan;
                    double rx2 = fMinLng + (pt2[0] / 100.0) * fLngSpan;
                    double ry2 = fMinLat + (pt2[1] / 100.0) * fLatSpan;
                    double stepDist = calculateRealDistanceKm(ry1, rx1, ry2, rx2);
                    
                    double mx = (rx1 + rx2) / 2.0;
                    double my = (ry1 + ry2) / 2.0;
                    double trafficMultiplier = getTrafficMultiplier(mx, my, trafficZones);
                    legRealDist += stepDist * trafficMultiplier;
                }

                double legTime = legRealDist / vehicle.getSpeed();
                totalDistanceKm += legRealDist;
                currentTime += legTime;

                if (currentTime < stop.getTimeWindowStart()) {
                    currentTime = stop.getTimeWindowStart();
                }
                currentTime += stop.getServiceTime();

                lastRealX = stop.getX();
                lastRealY = stop.getY();
            }

            // Path back to depot
            double sxLast = (lastRealX - fMinLng) / fLngSpan * 100.0;
            double syLast = (lastRealY - fMinLat) / fLatSpan * 100.0;
            double sxDepot = (depot.getX() - fMinLng) / fLngSpan * 100.0;
            double syDepot = (depot.getY() - fMinLat) / fLatSpan * 100.0;

            List<double[]> returnLeg = pathfinder.findPath(sxLast, syLast, sxDepot, syDepot, scaledTrafficZones);
            for (int p = 1; p < returnLeg.size(); p++) {
                double[] pt = returnLeg.get(p);
                double rx = fMinLng + (pt[0] / 100.0) * fLngSpan;
                double ry = fMinLat + (pt[1] / 100.0) * fLatSpan;
                fullRealCoordsPath.add(new double[]{rx, ry});
            }

            double returnRealDist = 0.0;
            for (int p = 0; p < returnLeg.size() - 1; p++) {
                double[] pt1 = returnLeg.get(p);
                double[] pt2 = returnLeg.get(p + 1);
                double rx1 = fMinLng + (pt1[0] / 100.0) * fLngSpan;
                double ry1 = fMinLat + (pt1[1] / 100.0) * fLatSpan;
                double rx2 = fMinLng + (pt2[0] / 100.0) * fLngSpan;
                double ry2 = fMinLat + (pt2[1] / 100.0) * fLatSpan;
                double stepDist = calculateRealDistanceKm(ry1, rx1, ry2, rx2);
                
                double mx = (rx1 + rx2) / 2.0;
                double my = (ry1 + ry2) / 2.0;
                double trafficMultiplier = getTrafficMultiplier(mx, my, trafficZones);
                returnRealDist += stepDist * trafficMultiplier;
            }

            totalDistanceKm += returnRealDist;
            currentTime += (returnRealDist / vehicle.getSpeed());

            double totalDuration = currentTime - 8.0;
            double totalCost = totalDistanceKm * vehicle.getCostPerKm();
            double fuelUsed = (totalDistanceKm / 100.0) * vehicle.getFuelConsumptionRate();
            double totalCo2 = fuelUsed * 2.62;

            String pathJson = serializePath(fullRealCoordsPath);

            OptimizedRoute route = new OptimizedRoute(
                    null, 
                    vehicle, 
                    sequenceBuilder.toString(), 
                    totalDistanceKm, 
                    totalDuration, 
                    totalCost, 
                    totalCo2, 
                    pathJson, 
                    java.time.LocalDateTime.now()
            );

            savedRoutes.add(optimizedRouteRepository.save(route));
        }

        return savedRoutes;
    }

    private double calculateRealDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double latMid = (lat1 + lat2) * Math.PI / 180.0;
        double dLat = (lat2 - lat1) * 111.32;
        double dLng = (lng2 - lng1) * 111.32 * Math.cos(latMid);
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }

    private double getTrafficMultiplier(double x, double y, List<TrafficZone> trafficZones) {
        double multiplier = 1.0;
        for (TrafficZone zone : trafficZones) {
            double dist = Math.sqrt(Math.pow(x - zone.getX(), 2) + Math.pow(y - zone.getY(), 2));
            if (dist <= zone.getRadius()) {
                double influence = (zone.getRadius() - dist) / zone.getRadius();
                double severity = 1.0 + (zone.getSeverity() - 1.0) * influence;
                multiplier = Math.max(multiplier, severity);
            }
        }
        return multiplier;
    }

    private String serializePath(List<double[]> path) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("[").append(path.get(i)[0]).append(",").append(path.get(i)[1]).append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}
