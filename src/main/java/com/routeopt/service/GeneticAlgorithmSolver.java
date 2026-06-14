package com.routeopt.service;

import com.routeopt.model.DeliveryStop;
import com.routeopt.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeneticAlgorithmSolver {
    private static final int POPULATION_SIZE = 100;
    private static final int GENERATIONS = 300;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.15;
    private static final int TOURNAMENT_SIZE = 5;

    // Penalty coefficients for constraint violations
    private static final double CAPACITY_PENALTY_COEFF = 1000.0;
    private static final double TIME_WINDOW_PENALTY_COEFF = 500.0;

    public static class Individual {
        // Maps vehicle index to the sequence of stops (1-indexed into stops list) assigned to it
        public Map<Integer, List<Integer>> routes;
        public double fitness;
        public double totalDistance;
        public double totalDuration;
        public double capacityViolations;
        public double timeWindowViolations;

        public Individual(int numVehicles) {
            routes = new HashMap<>();
            for (int i = 0; i < numVehicles; i++) {
                routes.put(i, new ArrayList<>());
            }
        }

        public Individual copy() {
            Individual clone = new Individual(routes.size());
            for (Map.Entry<Integer, List<Integer>> entry : routes.entrySet()) {
                clone.routes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            clone.fitness = this.fitness;
            clone.totalDistance = this.totalDistance;
            clone.totalDuration = this.totalDuration;
            clone.capacityViolations = this.capacityViolations;
            clone.timeWindowViolations = this.timeWindowViolations;
            return clone;
        }
    }

    public Individual solve(
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
            return new Individual(numVehicles);
        }

        // Initialize Population
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual ind = createRandomIndividual(numVehicles, numStops);
            evaluate(ind, costMatrix, durationMatrix, vehicles, stops);
            population.add(ind);
        }

        Individual bestEver = population.get(0).copy();

        // GA Loop
        Random rand = new Random();
        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Individual> nextGen = new ArrayList<>();

            // Elitism: carry over top 5%
            population.sort(Comparator.comparingDouble(ind -> ind.fitness));
            for (int i = 0; i < Math.max(1, POPULATION_SIZE / 20); i++) {
                nextGen.add(population.get(i).copy());
            }

            while (nextGen.size() < POPULATION_SIZE) {
                // Selection
                Individual parent1 = tournamentSelect(population, rand);
                Individual parent2 = tournamentSelect(population, rand);

                Individual child1, child2;

                // Crossover
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    Individual[] children = crossover(parent1, parent2, numVehicles, numStops, rand);
                    child1 = children[0];
                    child2 = children[1];
                } else {
                    child1 = parent1.copy();
                    child2 = parent2.copy();
                }

                // Mutation
                if (rand.nextDouble() < MUTATION_RATE) {
                    mutate(child1, numVehicles, numStops, rand);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    mutate(child2, numVehicles, numStops, rand);
                }

                evaluate(child1, costMatrix, durationMatrix, vehicles, stops);
                evaluate(child2, costMatrix, durationMatrix, vehicles, stops);

                nextGen.add(child1);
                if (nextGen.size() < POPULATION_SIZE) {
                    nextGen.add(child2);
                }
            }

            population = nextGen;

            // Track best
            for (Individual ind : population) {
                if (ind.fitness < bestEver.fitness) {
                    bestEver = ind.copy();
                }
            }
        }

        return bestEver;
    }

    private Individual createRandomIndividual(int numVehicles, int numStops) {
        Individual ind = new Individual(numVehicles);
        List<Integer> allStops = new ArrayList<>();
        for (int i = 1; i <= numStops; i++) {
            allStops.add(i);
        }
        Collections.shuffle(allStops);

        Random rand = new Random();
        for (int stop : allStops) {
            int v = rand.nextInt(numVehicles);
            ind.routes.get(v).add(stop);
        }
        return ind;
    }

    private void evaluate(Individual ind, double[][] costMatrix, double[][] durationMatrix, List<Vehicle> vehicles, List<DeliveryStop> stops) {
        double distTotal = 0.0;
        double durationTotal = 0.0;
        double capViolations = 0.0;
        double timeViolations = 0.0;

        for (int v = 0; v < vehicles.size(); v++) {
            Vehicle vehicle = vehicles.get(v);
            List<Integer> route = ind.routes.get(v);

            if (route.isEmpty()) continue;

            double currentLoad = 0.0;
            double currentTime = 8.0; // Start delivery shift at 8:00 AM (depot departure)
            int lastNode = 0; // Depot is index 0

            for (int stopIdx : route) {
                DeliveryStop stop = stops.get(stopIdx - 1);
                
                // Capacity constraint
                currentLoad += stop.getDemand();

                // Travel cost
                double travelDist = costMatrix[lastNode][stopIdx];
                double travelTime = durationMatrix[lastNode][stopIdx];

                distTotal += travelDist;
                currentTime += travelTime;

                // Time window check
                if (currentTime < stop.getTimeWindowStart()) {
                    // Arrived early, vehicle waits
                    currentTime = stop.getTimeWindowStart();
                } else if (currentTime > stop.getTimeWindowEnd()) {
                    // Arrived late! Penalize window violation
                    timeViolations += (currentTime - stop.getTimeWindowEnd());
                }

                // Add service duration
                currentTime += stop.getServiceTime();
                lastNode = stopIdx;
            }

            // Return to Depot (node 0)
            distTotal += costMatrix[lastNode][0];
            
            // Check capacity violation
            if (currentLoad > vehicle.getCapacity()) {
                capViolations += (currentLoad - vehicle.getCapacity());
            }
        }

        ind.totalDistance = distTotal;
        ind.totalDuration = durationTotal; // Will compute detailed paths in service
        ind.capacityViolations = capViolations;
        ind.timeWindowViolations = timeViolations;

        // Fitness objective: minimize total cost (distance) + penalties
        ind.fitness = distTotal 
                + (capViolations * CAPACITY_PENALTY_COEFF) 
                + (timeViolations * TIME_WINDOW_PENALTY_COEFF);
    }

    private Individual tournamentSelect(List<Individual> population, Random rand) {
        Individual best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            Individual ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.fitness < best.fitness) {
                best = ind;
            }
        }
        return best;
    }

    private Individual[] crossover(Individual p1, Individual p2, int numVehicles, int numStops, Random rand) {
        Individual c1 = new Individual(numVehicles);
        Individual c2 = new Individual(numVehicles);

        // Simple single-point crossover of assignments
        // Find assignment representation: stopId -> vehicleId
        int[] assign1 = getAssignmentArray(p1, numStops);
        int[] assign2 = getAssignmentArray(p2, numStops);

        int crossoverPoint = rand.nextInt(numStops);

        int[] childAssign1 = new int[numStops];
        int[] childAssign2 = new int[numStops];

        for (int i = 0; i < numStops; i++) {
            if (i < crossoverPoint) {
                childAssign1[i] = assign1[i];
                childAssign2[i] = assign2[i];
            } else {
                childAssign1[i] = assign2[i];
                childAssign2[i] = assign1[i];
            }
        }

        // Apply back and preserve ordering/sequencing of original routes
        applyAssignment(c1, childAssign1, p1);
        applyAssignment(c2, childAssign2, p2);

        return new Individual[]{c1, c2};
    }

    private int[] getAssignmentArray(Individual ind, int numStops) {
        int[] assignments = new int[numStops];
        for (int v = 0; v < ind.routes.size(); v++) {
            for (int stop : ind.routes.get(v)) {
                assignments[stop - 1] = v;
            }
        }
        return assignments;
    }

    private void applyAssignment(Individual child, int[] assignments, Individual parentPattern) {
        // Build routing assignments
        for (int i = 0; i < assignments.length; i++) {
            int stop = i + 1;
            int v = assignments[i];
            child.routes.get(v).add(stop);
        }

        // Sort child routes matching order sequence of parentPattern as a heuristic
        for (int v = 0; v < child.routes.size(); v++) {
            List<Integer> childRoute = child.routes.get(v);
            List<Integer> parentRoute = parentPattern.routes.get(v);
            
            childRoute.sort(Comparator.comparingInt(stop -> {
                int pos = parentRoute.indexOf(stop);
                return pos == -1 ? Integer.MAX_VALUE : pos;
            }));
        }
    }

    private void mutate(Individual ind, int numVehicles, int numStops, Random rand) {
        // Mutation type 1: Move stop to a different vehicle
        // Mutation type 2: Swap two stops within a vehicle
        if (rand.nextBoolean()) {
            // Reassign stop
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
                    // Add stop at a random index in the new vehicle route
                    List<Integer> newRoute = ind.routes.get(newVeh);
                    int idx = newRoute.isEmpty() ? 0 : rand.nextInt(newRoute.size() + 1);
                    newRoute.add(idx, stop);
                }
            }
        } else {
            // Swap two stops in a random vehicle
            int v = rand.nextInt(numVehicles);
            List<Integer> route = ind.routes.get(v);
            if (route.size() >= 2) {
                int idx1 = rand.nextInt(route.size());
                int idx2 = rand.nextInt(route.size());
                while (idx1 == idx2) {
                    idx2 = rand.nextInt(route.size());
                }
                int temp = route.get(idx1);
                route.set(idx1, route.get(idx2));
                route.set(idx2, temp);
            }
        }
    }
}
