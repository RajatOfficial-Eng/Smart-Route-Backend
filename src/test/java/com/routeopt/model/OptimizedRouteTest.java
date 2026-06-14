package com.routeopt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OptimizedRouteTest {

    private OptimizedRoute route;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        route = new OptimizedRoute();
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setName("Test Vehicle");
    }

    @Test
    void testOptimizedRoute_SettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        route.setId(1L);
        route.setVehicle(vehicle);
        route.setStopSequence("S1,S2,S3");
        route.setTotalDistance(50.5);
        route.setTotalDuration(2.5);
        route.setTotalCost(100.0);
        route.setTotalCo2(25.0);
        route.setPathCoordinatesJson("[[0,0],[1,1]]");
        route.setCreatedAt(now);

        assertEquals(1L, route.getId());
        assertEquals(vehicle, route.getVehicle());
        assertEquals("S1,S2,S3", route.getStopSequence());
        assertEquals(50.5, route.getTotalDistance());
        assertEquals(2.5, route.getTotalDuration());
        assertEquals(100.0, route.getTotalCost());
        assertEquals(25.0, route.getTotalCo2());
        assertEquals("[[0,0],[1,1]]", route.getPathCoordinatesJson());
        assertEquals(now, route.getCreatedAt());
    }

    @Test
    void testOptimizedRoute_NullValues() {
        assertNull(route.getId());
        assertNull(route.getVehicle());
        assertNull(route.getStopSequence());
        assertNull(route.getPathCoordinatesJson());
    }

    @Test
    void testOptimizedRoute_WithZeroDistance() {
        route.setTotalDistance(0.0);
        assertEquals(0.0, route.getTotalDistance());
    }

    @Test
    void testOptimizedRoute_WithHighCost() {
        route.setTotalCost(1000.0);
        assertEquals(1000.0, route.getTotalCost());
    }

    @Test
    void testOptimizedRoute_WithCo2Emissions() {
        route.setTotalCo2(50.5);
        assertEquals(50.5, route.getTotalCo2());
    }

    @Test
    void testOptimizedRoute_WithComplexStopSequence() {
        String sequence = "1,2,3,4,5,6,7,8,9,10";
        route.setStopSequence(sequence);
        assertEquals(sequence, route.getStopSequence());
    }
}
