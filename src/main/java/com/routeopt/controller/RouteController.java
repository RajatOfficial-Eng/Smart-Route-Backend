package com.routeopt.controller;

import com.routeopt.model.Depot;
import com.routeopt.model.OptimizedRoute;
import com.routeopt.repository.DepotRepository;
import com.routeopt.repository.OptimizedRouteRepository;
import com.routeopt.service.RouteOptimizationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    private RouteOptimizationService optimizationService;

    @Autowired
    private OptimizedRouteRepository routeRepository;

    @Autowired
    private DepotRepository depotRepository;

    @PostMapping("/optimize")
    public ResponseEntity<List<OptimizedRoute>> runOptimization(
            @RequestParam(value = "algorithm", defaultValue = "GA") String algorithm
    ) {
        long startTime = System.currentTimeMillis();
        List<OptimizedRoute> routes = optimizationService.optimizeRoutes(algorithm);
        long endTime = System.currentTimeMillis();
        
        log.info("Optimization completed using {} in {} ms", algorithm, (endTime - startTime));
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<OptimizedRoute>> getRoutes() {
        return ResponseEntity.ok(routeRepository.findAll());
    }

    @GetMapping("/depot")
    public ResponseEntity<Depot> getDepot() {
        List<Depot> depots = depotRepository.findAll();
        if (depots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(depots.get(0));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDatabase() {
        optimizationService.seedDatabase();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Database successfully reset to seed defaults.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearWorkspace() {
        optimizationService.clearWorkspace();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Workspace cleared. Ready for custom configuration.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/depot")
    public ResponseEntity<Depot> updateDepot(@Valid @RequestBody Depot updatedDepot) {
        List<Depot> depots = depotRepository.findAll();
        if (depots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Depot depot = depots.get(0);
        depot.setName(updatedDepot.getName());
        depot.setX(updatedDepot.getX());
        depot.setY(updatedDepot.getY());
        return ResponseEntity.ok(depotRepository.save(depot));
    }
}
