package com.routeopt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficZoneTest {

    private TrafficZone zone;

    @BeforeEach
    void setUp() {
        zone = new TrafficZone();
    }

    @Test
    void testTrafficZone_SettersAndGetters() {
        zone.setId(1L);
        zone.setX(50.0);
        zone.setY(60.0);
        zone.setRadius(5.0);
        zone.setSeverity(8.5);

        assertEquals(1L, zone.getId());
        assertEquals(50.0, zone.getX());
        assertEquals(60.0, zone.getY());
        assertEquals(5.0, zone.getRadius());
        assertEquals(8.5, zone.getSeverity());
    }

    @Test
    void testTrafficZone_NullValues() {
        assertNull(zone.getId());
        assertEquals(0.0, zone.getX());
        assertEquals(0.0, zone.getY());
        assertEquals(0.0, zone.getRadius());
        assertEquals(0.0, zone.getSeverity());
    }

    @Test
    void testTrafficZone_WithHighSeverity() {
        zone.setSeverity(10.0);
        assertEquals(10.0, zone.getSeverity());
    }

    @Test
    void testTrafficZone_WithZeroRadius() {
        zone.setRadius(0.0);
        assertEquals(0.0, zone.getRadius());
    }

    @Test
    void testTrafficZone_WithNegativeCoordinates() {
        zone.setX(-50.0);
        zone.setY(-60.0);

        assertEquals(-50.0, zone.getX());
        assertEquals(-60.0, zone.getY());
    }

    @Test
    void testTrafficZone_Constructor() {
        TrafficZone zone2 = new TrafficZone(1L, 10.0, 20.0, 2.5, 5.0);

        assertEquals(1L, zone2.getId());
        assertEquals(10.0, zone2.getX());
        assertEquals(20.0, zone2.getY());
        assertEquals(2.5, zone2.getRadius());
        assertEquals(5.0, zone2.getSeverity());
    }
}
