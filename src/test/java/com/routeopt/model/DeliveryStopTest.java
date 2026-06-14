package com.routeopt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryStopTest {

    private DeliveryStop stop;

    @BeforeEach
    void setUp() {
        stop = new DeliveryStop();
    }

    @Test
    void testDeliveryStop_SettersAndGetters() {
        stop.setId(1L);
        stop.setName("Test Stop");
        stop.setX(28.7041);
        stop.setY(77.1025);
        stop.setDemand(10.5);

        assertEquals(1L, stop.getId());
        assertEquals("Test Stop", stop.getName());
        assertEquals(28.7041, stop.getX());
        assertEquals(77.1025, stop.getY());
        assertEquals(10.5, stop.getDemand());
    }

    @Test
    void testDeliveryStop_NullValues() {
        assertNull(stop.getId());
        assertNull(stop.getName());
        assertEquals(0.0, stop.getX());
        assertEquals(0.0, stop.getY());
        assertEquals(0.0, stop.getDemand());
    }

    @Test
    void testDeliveryStop_WithNegativeDemand() {
        stop.setDemand(-5.0);
        assertEquals(-5.0, stop.getDemand());
    }

    @Test
    void testDeliveryStop_WithZeroDemand() {
        stop.setDemand(0.0);
        assertEquals(0.0, stop.getDemand());
    }
}
