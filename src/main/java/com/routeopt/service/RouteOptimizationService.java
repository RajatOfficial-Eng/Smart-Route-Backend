package com.routeopt.service;

import com.routeopt.model.*;
import com.routeopt.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteOptimizationService {

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
            seedDatabase();
        }
    }

    public void seedDatabase() {
        // Clear existing
        optimizedRouteRepository.deleteAll();
        deliveryStopRepository.deleteAll();
        vehicleRepository.deleteAll();
        depotRepository.deleteAll();
        trafficZoneRepository.deleteAll();

        // Create Depot at Center (50, 50)
        Depot depot = new Depot(null, "Main Distribution Center", 50.0, 50.0);
        depotRepository.save(depot);

        // Create Vehicles
        vehicleRepository.save(new Vehicle(null, "Ford Transit (Electric)", 30.0, 15.0, 1.2, 40.0, true));
        vehicleRepository.save(new Vehicle(null, "Mercedes Sprinter (Diesel)", 80.0, 25.0, 2.5, 30.0, true));
        vehicleRepository.save(new Vehicle(null, "Toyota HiAce (Gas)", 45.0, 18.0, 1.6, 35.0, true));
        vehicleRepository.save(new Vehicle(null, "Delivery E-Bike", 15.0, 8.0, 0.8, 50.0, true));

        // Create Delivery Stops
        // Time window start/end in hours of day: e.g. 9.0 to 12.0
        deliveryStopRepository.save(new DeliveryStop(null, "Downtown Retail Branch", 75.0, 75.0, 10.0, 9.0, 12.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Uptown Warehouse", 20.0, 80.0, 20.0, 10.0, 14.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Eastside Clinic", 80.0, 25.0, 15.0, 8.0, 11.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Westside Pharmacy", 25.0, 20.0, 25.0, 13.0, 17.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Central Tech Office", 55.0, 65.0, 8.0, 9.5, 13.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Industrial Park Factory", 85.0, 45.0, 35.0, 11.0, 15.0, 0.50, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Suburb Residential Complex", 15.0, 60.0, 12.0, 8.5, 12.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Harbor Port Terminal", 45.0, 15.0, 18.0, 14.0, 18.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Shopping Mall Plaza", 65.0, 35.0, 14.0, 10.0, 16.0, 0.25, "PENDING"));
        deliveryStopRepository.save(new DeliveryStop(null, "Regional Hospital Center", 35.0, 75.0, 5.0, 9.0, 11.0, 0.25, "PENDING"));

        // Create initial traffic zones (one bottleneck, one complete roadblock)
        trafficZoneRepository.save(new TrafficZone(null, 50.0, 30.0, 8.0, 5.0)); // Heavy traffic bottleneck
        trafficZoneRepository.save(new TrafficZone(null, 35.0, 55.0, 6.0, 999.0)); // Complete roadblock
    }

    public void clearWorkspace() {
        optimizedRouteRepository.deleteAll();
        deliveryStopRepository.deleteAll();
        vehicleRepository.deleteAll();
        trafficZoneRepository.deleteAll();
        // Ensure at least one depot exists
        if (depotRepository.count() == 0) {
            depotRepository.save(new Depot(null, "Main Distribution Center", 50.0, 50.0));
        }
    }

    public List<OptimizedRoute> optimizeRoutes(String algorithm) {
        // Fetch inputs
        List<Depot> depots = depotRepository.findAll();
        if (depots.isEmpty()) return new ArrayList<>();
        Depot depot = depots.get(0);

        List<Vehicle> activeVehicles = vehicleRepository.findByActiveTrue();
        List<DeliveryStop> pendingStops = deliveryStopRepository.findAll(); // Optimizing all stops in list
        List<TrafficZone> trafficZones = trafficZoneRepository.findAll();

        if (activeVehicles.isEmpty() || pendingStops.isEmpty()) {
            return new ArrayList<>();
        }

        int n = pendingStops.size();
        int totalNodes = n + 1; // Node 0 is Depot, Node 1..N are stops

        // Build cost and duration matrices
        double[][] costMatrix = new double[totalNodes][totalNodes];
        double[][] durationMatrix = new double[totalNodes][totalNodes];

        // Node points mapping
        double[] xCoords = new double[totalNodes];
        double[] yCoords = new double[totalNodes];
        
        xCoords[0] = depot.getX();
        yCoords[0] = depot.getY();
        
        for (int i = 1; i <= n; i++) {
            DeliveryStop stop = pendingStops.get(i - 1);
            xCoords[i] = stop.getX();
            yCoords[i] = stop.getY();
        }

        // Populate matrices via A* pathfinding
        for (int i = 0; i < totalNodes; i++) {
            for (int j = 0; j < totalNodes; j++) {
                if (i == j) {
                    costMatrix[i][j] = 0.0;
                    durationMatrix[i][j] = 0.0;
                } else {
                    List<double[]> legPath = pathfinder.findPath(xCoords[i], yCoords[i], xCoords[j], yCoords[j], trafficZones);
                    double cost = pathfinder.getPathCost(legPath, trafficZones);
                    costMatrix[i][j] = cost;
                    
                    // Travel time (using a generic 40 units/hr average speed)
                    durationMatrix[i][j] = cost / 40.0; 
                }
            }
        }

        // Solve VRP
        GeneticAlgorithmSolver.Individual bestSolution;
        if ("SA".equalsIgnoreCase(algorithm)) {
            bestSolution = saSolver.solve(costMatrix, durationMatrix, activeVehicles, pendingStops, depot.getX(), depot.getY());
        } else {
            bestSolution = gaSolver.solve(costMatrix, durationMatrix, activeVehicles, pendingStops, depot.getX(), depot.getY());
        }

        // Clear existing optimization results
        optimizedRouteRepository.deleteAll();

        // Convert the best individual into OptimizedRoute entities
        List<OptimizedRoute> savedRoutes = new ArrayList<>();
        
        for (int v = 0; v < activeVehicles.size(); v++) {
            Vehicle vehicle = activeVehicles.get(v);
            List<Integer> stopIndices = bestSolution.routes.get(v);

            if (stopIndices == null || stopIndices.isEmpty()) {
                continue;
            }

            // Construct exact coordinates path and calculate metrics
            List<double[]> fullGridPath = new ArrayList<>();
            StringBuilder sequenceBuilder = new StringBuilder();

            double lastX = depot.getX();
            double lastY = depot.getY();
            
            // Add depot starting point to full path
            fullGridPath.add(new double[]{lastX, lastY});

            double totalDistance = 0.0;
            double currentTime = 8.0; // Start at 8:00 AM
            double totalDuration = 0.0;

            for (int idx = 0; idx < stopIndices.size(); idx++) {
                int stopNodeIdx = stopIndices.get(idx);
                DeliveryStop stop = pendingStops.get(stopNodeIdx - 1);

                // Add to sequence string
                if (idx > 0) sequenceBuilder.append(",");
                sequenceBuilder.append(stop.getId());

                // Path from last point to this stop
                List<double[]> leg = pathfinder.findPath(lastX, lastY, stop.getX(), stop.getY(), trafficZones);
                
                // Add coordinates (skipping first one to avoid duplicates)
                for (int p = (fullGridPath.isEmpty() ? 0 : 1); p < leg.size(); p++) {
                    fullGridPath.add(leg.get(p));
                }

                double legDist = pathfinder.getPathCost(leg, trafficZones);
                double legTime = legDist / vehicle.getSpeed();

                totalDistance += legDist;
                currentTime += legTime;

                if (currentTime < stop.getTimeWindowStart()) {
                    currentTime = stop.getTimeWindowStart(); // waits
                }
                
                currentTime += stop.getServiceTime();

                lastX = stop.getX();
                lastY = stop.getY();
            }

            // Path back to depot
            List<double[]> returnLeg = pathfinder.findPath(lastX, lastY, depot.getX(), depot.getY(), trafficZones);
            for (int p = 1; p < returnLeg.size(); p++) {
                fullGridPath.add(returnLeg.get(p));
            }
            double returnDist = pathfinder.getPathCost(returnLeg, trafficZones);
            totalDistance += returnDist;
            currentTime += (returnDist / vehicle.getSpeed());

            totalDuration = currentTime - 8.0; // Total duration in hours
            
            // Financial calculations
            double totalCost = totalDistance * vehicle.getCostPerKm();

            // CO2 emissions calculations: liters per km * emissions/liter * km
            // fuelConsumptionRate is per 100km, so divide by 100
            double fuelUsed = (totalDistance / 100.0) * vehicle.getFuelConsumptionRate();
            double totalCo2 = fuelUsed * 2.62; // 2.62kg of CO2 per liter of fuel

            // Serialize path to coordinates JSON
            String pathJson = serializePath(fullGridPath);

            OptimizedRoute route = new OptimizedRoute(
                    null, 
                    vehicle, 
                    sequenceBuilder.toString(), 
                    totalDistance, 
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
