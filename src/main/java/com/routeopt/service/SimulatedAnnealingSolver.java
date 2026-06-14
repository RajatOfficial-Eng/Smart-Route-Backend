package com.routeopt.service;

import com.routeopt.model.DeliveryStop;
import com.routeopt.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimulatedAnnealingSolver {
    private static final double START_TEMP = 1000.0;
    private static final double COOLING_RATE = 0.998;
    private static final double MIN_TEMP = 0.1;

    private static final double CAPACITY_PENALTY_COEFF = 1000.0;
    private static final double TIME_WINDOW_PENALTY_COEFF = 500.0;

    public GeneticAlgorithmSolver.Individual solve(
            double[][] costMatrix, 
            double[][] durationMatrix, 
            List<Vehicle> vehicles, 
            List<DeliveryStop> stops, 
            double depotX, 
            double depotY
    ) {
        int numVehicles = vehicles.size();
        int numStops = stops.size();

        if (numVehicles == 0 || numStops == 0) {
            return new GeneticAlgorithmSolver.Individual(numVehicles);
        }

        Random rand = new Random();
        
        // Initial state (greedy random assignment)
        GeneticAlgorithmSolver.Individual current = createInitialState(numVehicles, numStops, rand);
        evaluate(current, costMatrix, durationMatrix, vehicles, stops);

        GeneticAlgorithmSolver.Individual best = current.copy();

        double temp = START_TEMP;

        while (temp > MIN_TEMP) {
            GeneticAlgorithmSolver.Individual neighbor = current.copy();
            
            // Perturb neighbor
            perturb(neighbor, numVehicles, numStops, rand);
            evaluate(neighbor, costMatrix, durationMatrix, vehicles, stops);

            double currentCost = current.fitness;
            double neighborCost = neighbor.fitness;

            if (neighborCost < currentCost) {
                current = neighbor;
                if (neighborCost < best.fitness) {
                    best = neighbor.copy();
                }
            } else {
                // Accept worse solution with probability exp(-delta / Temp)
                double delta = neighborCost - currentCost;
                double acceptanceProbability = Math.exp(-delta / temp);
                if (rand.nextDouble() < acceptanceProbability) {
                    current = neighbor;
                }
            }

            temp *= COOLING_RATE;
        }

        return best;
    }

    private GeneticAlgorithmSolver.Individual createInitialState(int numVehicles, int numStops, Random rand) {
        GeneticAlgorithmSolver.Individual ind = new GeneticAlgorithmSolver.Individual(numVehicles);
        List<Integer> allStops = new ArrayList<>();
        for (int i = 1; i <= numStops; i++) {
            allStops.add(i);
        }
        Collections.shuffle(allStops);

        for (int stop : allStops) {
            int v = rand.nextInt(numVehicles);
            ind.routes.get(v).add(stop);
        }
        return ind;
    }

    private void evaluate(GeneticAlgorithmSolver.Individual ind, double[][] costMatrix, double[][] durationMatrix, List<Vehicle> vehicles, List<DeliveryStop> stops) {
        double distTotal = 0.0;
        double capViolations = 0.0;
        double timeViolations = 0.0;

        for (int v = 0; v < vehicles.size(); v++) {
            Vehicle vehicle = vehicles.get(v);
            List<Integer> route = ind.routes.get(v);

            if (route.isEmpty()) continue;

            double currentLoad = 0.0;
            double currentTime = 8.0; // Starts at 8:00 AM
            int lastNode = 0; // Depot

            for (int stopIdx : route) {
                DeliveryStop stop = stops.get(stopIdx - 1);
                currentLoad += stop.getDemand();

                double travelDist = costMatrix[lastNode][stopIdx];
                double travelTime = durationMatrix[lastNode][stopIdx];

                distTotal += travelDist;
                currentTime += travelTime;

                if (currentTime < stop.getTimeWindowStart()) {
                    currentTime = stop.getTimeWindowStart();
                } else if (currentTime > stop.getTimeWindowEnd()) {
                    timeViolations += (currentTime - stop.getTimeWindowEnd());
                }

                currentTime += stop.getServiceTime();
                lastNode = stopIdx;
            }

            // Return to Depot
            distTotal += costMatrix[lastNode][0];

            if (currentLoad > vehicle.getCapacity()) {
                capViolations += (currentLoad - vehicle.getCapacity());
            }
        }

        ind.totalDistance = distTotal;
        ind.capacityViolations = capViolations;
        ind.timeWindowViolations = timeViolations;

        ind.fitness = distTotal 
                + (capViolations * CAPACITY_PENALTY_COEFF) 
                + (timeViolations * TIME_WINDOW_PENALTY_COEFF);
    }

    private void perturb(GeneticAlgorithmSolver.Individual ind, int numVehicles, int numStops, Random rand) {
        // Simple swap or shift
        if (rand.nextBoolean()) {
            // Re-route a single stop
            int stop = rand.nextInt(numStops) + 1;
            int currentVeh = -1;
            for (int v = 0; v < numVehicles; v++) {
                if (ind.routes.get(v).contains(stop)) {
                    currentVeh = v;
                    break;
                }
            }

            if (currentVeh != -1) {
                int newVeh = rand.nextInt(numVehicles);
                if (newVeh != currentVeh) {
                    ind.routes.get(currentVeh).remove((Integer) stop);
                    List<Integer> newRoute = ind.routes.get(newVeh);
                    int idx = newRoute.isEmpty() ? 0 : rand.nextInt(newRoute.size() + 1);
                    newRoute.add(idx, stop);
                }
            }
        } else {
            // Swap stops between vehicles or in the same vehicle
            int v1 = rand.nextInt(numVehicles);
            int v2 = rand.nextInt(numVehicles);
            
            List<Integer> r1 = ind.routes.get(v1);
            List<Integer> r2 = ind.routes.get(v2);

            if (!r1.isEmpty() && !r2.isEmpty()) {
                int idx1 = rand.nextInt(r1.size());
                int idx2 = rand.nextInt(r2.size());

                int temp = r1.get(idx1);
                r1.set(idx1, r2.get(idx2));
                r2.set(idx2, temp);
            }
        }
    }
}
