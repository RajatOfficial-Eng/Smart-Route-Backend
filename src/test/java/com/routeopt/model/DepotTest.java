package com.routeopt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DepotTest {

    private Depot depot;

    @BeforeEach
    void setUp() {
        depot = new Depot();
    }

    @Test
    void testDepot_SettersAndGetters() {
        depot.setId(1L);
        depot.setName("Main Depot");
        depot.setX(28.7041);
        depot.setY(77.1025);

        assertEquals(1L, depot.getId());
        assertEquals("Main Depot", depot.getName());
        assertEquals(28.7041, depot.getX());
        assertEquals(77.1025, depot.getY());
    }

    @Test
    void testDepot_NullValues() {
        assertNull(depot.getId());
        assertNull(depot.getName());
        assertEquals(0.0, depot.getX());
        assertEquals(0.0, depot.getY());
    }

    @Test
    void testDepot_WithDifferentCoordinates() {
        depot.setX(40.7128);
        depot.setY(-74.0060);

        assertEquals(40.7128, depot.getX());
        assertEquals(-74.0060, depot.getY());
    }

    @Test
    void testDepot_WithZeroCoordinates() {
        depot.setX(0.0);
        depot.setY(0.0);

        assertEquals(0.0, depot.getX());
        assertEquals(0.0, depot.getY());
    }
}
