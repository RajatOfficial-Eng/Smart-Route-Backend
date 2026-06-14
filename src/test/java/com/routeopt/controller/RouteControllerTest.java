package com.routeopt.controller;

import com.routeopt.model.OptimizedRoute;
import com.routeopt.model.Vehicle;
import com.routeopt.repository.OptimizedRouteRepository;
import com.routeopt.service.RouteOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouteControllerTest {

    @Mock
    private RouteOptimizationService routeOptimizationService;

    @Mock
    private OptimizedRouteRepository routeRepository;

    @InjectMocks
    private RouteController routeController;

    private OptimizedRoute testRoute;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setName("Test Vehicle");

        testRoute = new OptimizedRoute();
        testRoute.setId(1L);
        testRoute.setVehicle(testVehicle);
        testRoute.setStopSequence("S1,S2,S3");
        testRoute.setTotalDistance(50.5);
    }

    @Test
    void testRunOptimization_WithGAAlgorithm_ReturnsOkStatus() {
        List<OptimizedRoute> routes = Collections.singletonList(testRoute);
        when(routeOptimizationService.optimizeRoutes("GA")).thenReturn(routes);

        ResponseEntity<List<OptimizedRoute>> response = routeController.runOptimization("GA");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(routeOptimizationService, times(1)).optimizeRoutes("GA");
    }

    @Test
    void testRunOptimization_WithSAAlgorithm_ReturnsOkStatus() {
        List<OptimizedRoute> routes = Collections.singletonList(testRoute);
        when(routeOptimizationService.optimizeRoutes("SA")).thenReturn(routes);

        ResponseEntity<List<OptimizedRoute>> response = routeController.runOptimization("SA");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(routeOptimizationService, times(1)).optimizeRoutes("SA");
    }

    @Test
    void testRunOptimization_WithEmptyRoutes() {
        when(routeOptimizationService.optimizeRoutes("GA")).thenReturn(Collections.emptyList());

        ResponseEntity<List<OptimizedRoute>> response = routeController.runOptimization("GA");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetRoutes_ReturnsAllRoutes() {
        List<OptimizedRoute> routes = Collections.singletonList(testRoute);
        when(routeRepository.findAll()).thenReturn(routes);

        ResponseEntity<List<OptimizedRoute>> response = routeController.getRoutes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(routeRepository, times(1)).findAll();
    }
}
