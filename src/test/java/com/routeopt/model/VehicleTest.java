package com.routeopt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
    }

    @Test
    void testVehicle_SettersAndGetters() {
        vehicle.setId(1L);
        vehicle.setName("Vehicle-1");
        vehicle.setCapacity(100.0);

        assertEquals(1L, vehicle.getId());
        assertEquals("Vehicle-1", vehicle.getName());
        assertEquals(100.0, vehicle.getCapacity());
    }

    @Test
    void testVehicle_NullValues() {
        assertNull(vehicle.getId());
        assertNull(vehicle.getName());
        assertEquals(0.0, vehicle.getCapacity());
        assertEquals(0.0, vehicle.getFuelConsumptionRate());
        assertEquals(0.0, vehicle.getCostPerKm());
        assertEquals(0.0, vehicle.getSpeed());
        assertFalse(vehicle.isActive());
    }

    @Test
    void testVehicle_WithHighCapacity() {
        vehicle.setCapacity(1000.0);
        assertEquals(1000.0, vehicle.getCapacity());
    }

    @Test
    void testVehicle_WithZeroCapacity() {
        vehicle.setCapacity(0.0);
        assertEquals(0.0, vehicle.getCapacity());
    }

    @Test
    void testVehicle_WithNegativeCapacity() {
        vehicle.setCapacity(-50.0);
        assertEquals(-50.0, vehicle.getCapacity());
    }
}
