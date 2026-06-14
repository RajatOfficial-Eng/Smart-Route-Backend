package com.routeopt.service;

import com.routeopt.model.TrafficZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AStarPathfinderTest {

    private AStarPathfinder pathfinder;

    @BeforeEach
    void setUp() {
        pathfinder = new AStarPathfinder();
    }

    @Test
    void testFindPath_WithValidCoordinates_ReturnsPath() {
        List<TrafficZone> trafficZones = new ArrayList<>();
        
        List<double[]> path = pathfinder.findPath(0.0, 0.0, 10.0, 10.0, trafficZones);

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(0.0, path.get(0)[0]);
        assertEquals(0.0, path.get(0)[1]);
    }

    @Test
    void testFindPath_SameCoordinates_ReturnsSinglePoint() {
        List<TrafficZone> trafficZones = new ArrayList<>();
        
        List<double[]> path = pathfinder.findPath(5.0, 5.0, 5.0, 5.0, trafficZones);

        assertNotNull(path);
        assertTrue(path.size() > 0);
    }

    @Test
    void testFindPath_WithTrafficZones_ReturnsPath() {
        List<TrafficZone> trafficZones = new ArrayList<>();
        TrafficZone zone = new TrafficZone();
        zone.setX(5.0);
        zone.setY(5.0);
        zone.setRadius(2.0);
        trafficZones.add(zone);
        
        List<double[]> path = pathfinder.findPath(0.0, 0.0, 10.0, 10.0, trafficZones);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    void testGetPathCost_WithEmptyPath_ReturnsZero() {
        List<double[]> path = new ArrayList<>();
        List<TrafficZone> trafficZones = new ArrayList<>();
        
        double cost = pathfinder.getPathCost(path, trafficZones);

        assertEquals(0.0, cost);
    }

    @Test
    void testGetPathCost_WithPath_ReturnsPositiveCost() {
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{0.0, 0.0});
        path.add(new double[]{10.0, 10.0});
        List<TrafficZone> trafficZones = new ArrayList<>();
        
        double cost = pathfinder.getPathCost(path, trafficZones);

        assertTrue(cost >= 0);
    }
}
