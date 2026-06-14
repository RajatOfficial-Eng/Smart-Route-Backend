package com.routeopt.service;

import com.routeopt.model.DeliveryStop;
import com.routeopt.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GeneticAlgorithmSolverTest {

    private GeneticAlgorithmSolver solver;
    private List<DeliveryStop> testStops;
    private List<Vehicle> testVehicles;

    @BeforeEach
    void setUp() {
        solver = new GeneticAlgorithmSolver();
        testStops = new ArrayList<>();
        testVehicles = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            DeliveryStop stop = new DeliveryStop();
            stop.setId((long) i);
            stop.setName("Stop-" + i);
            stop.setX(28.7041 + i * 0.01);
            stop.setY(77.1025 + i * 0.01);
            stop.setDemand(10.0);
            testStops.add(stop);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setName("Vehicle-1");
        vehicle.setCapacity(100.0);
        testVehicles.add(vehicle);
    }

    @Test
    void testSolve_WithValidInputs_ReturnsIndividual() {
        int numNodes = testStops.size() + 1;
        double[][] costMatrix = new double[numNodes][numNodes];
        double[][] durationMatrix = new double[numNodes][numNodes];

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                costMatrix[i][j] = i == j ? 0 : 10.0;
                durationMatrix[i][j] = i == j ? 0 : 0.5;
            }
        }

        GeneticAlgorithmSolver.Individual result = solver.solve(
                costMatrix, durationMatrix, testVehicles, testStops, 50.0, 50.0
        );

        assertNotNull(result);
        assertNotNull(result.routes);
        assertEquals(testVehicles.size(), result.routes.size());
    }

    @Test
    void testSolve_WithEmptyStops_ReturnsEmptyIndividual() {
        double[][] costMatrix = new double[1][1];
        double[][] durationMatrix = new double[1][1];

        GeneticAlgorithmSolver.Individual result = solver.solve(
                costMatrix, durationMatrix, testVehicles, new ArrayList<>(), 50.0, 50.0
        );

        assertNotNull(result);
        assertNotNull(result.routes);
    }

    @Test
    void testIndividual_Copy_CreatesIndependentCopy() {
        GeneticAlgorithmSolver.Individual individual = new GeneticAlgorithmSolver.Individual(1);
        individual.routes.get(0).add(1);
        individual.fitness = 100.0;

        GeneticAlgorithmSolver.Individual copy = individual.copy();

        assertEquals(individual.fitness, copy.fitness);
        copy.routes.get(0).add(2);
        assertEquals(1, individual.routes.get(0).size());
        assertEquals(2, copy.routes.get(0).size());
    }
}
