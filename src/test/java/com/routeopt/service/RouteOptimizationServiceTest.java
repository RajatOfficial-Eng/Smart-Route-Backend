package com.routeopt.service;

import com.routeopt.model.*;
import com.routeopt.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouteOptimizationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DeliveryStopRepository deliveryStopRepository;

    @Mock
    private OptimizedRouteRepository optimizedRouteRepository;

    @Mock
    private DepotRepository depotRepository;

    @Mock
    private TrafficZoneRepository trafficZoneRepository;

    @Mock
    private AStarPathfinder pathfinder;

    @Mock
    private GeneticAlgorithmSolver gaSolver;

    @Mock
    private SimulatedAnnealingSolver saSolver;

    @InjectMocks
    private RouteOptimizationService service;

    private Vehicle testVehicle;
    private DeliveryStop testStop;
    private Depot testDepot;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setName("Vehicle-1");
        testVehicle.setCapacity(100.0);
        testVehicle.setActive(true);

        testStop = new DeliveryStop();
        testStop.setId(1L);
        testStop.setName("Stop-1");
        testStop.setX(28.7041);
        testStop.setY(77.1025);
        testStop.setDemand(10.0);

        testDepot = new Depot();
        testDepot.setId(1L);
        testDepot.setName("Main Depot");
        testDepot.setX(28.7041);
        testDepot.setY(77.1025);
    }

    @Test
    void testClearWorkspace_DeletesAllData() {
        when(depotRepository.count()).thenReturn(0L);

        service.clearWorkspace();

        verify(optimizedRouteRepository, times(1)).deleteAll();
        verify(deliveryStopRepository, times(1)).deleteAll();
        verify(vehicleRepository, times(1)).deleteAll();
        verify(trafficZoneRepository, times(1)).deleteAll();
    }

    @Test
    void testClearWorkspace_CreatesDepotIfMissing() {
        when(depotRepository.count()).thenReturn(0L);

        service.clearWorkspace();

        verify(depotRepository, times(1)).save(any(Depot.class));
    }

    @Test
    void testOptimizeRoutes_WithEmptyVehicles_ReturnsEmpty() {
        when(vehicleRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        when(deliveryStopRepository.findAll()).thenReturn(Collections.singletonList(testStop));
        when(depotRepository.findAll()).thenReturn(Collections.singletonList(testDepot));

        List<OptimizedRoute> result = service.optimizeRoutes("GA");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOptimizeRoutes_WithEmptyStops_ReturnsEmpty() {
        when(vehicleRepository.findByActiveTrue()).thenReturn(Collections.singletonList(testVehicle));
        when(deliveryStopRepository.findAll()).thenReturn(Collections.emptyList());
        when(depotRepository.findAll()).thenReturn(Collections.singletonList(testDepot));

        List<OptimizedRoute> result = service.optimizeRoutes("GA");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOptimizeRoutes_WithNoDepot_ReturnsEmpty() {
        when(depotRepository.findAll()).thenReturn(Collections.emptyList());

        List<OptimizedRoute> result = service.optimizeRoutes("GA");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
