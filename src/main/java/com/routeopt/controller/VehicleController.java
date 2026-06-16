package com.routeopt.controller;

import com.routeopt.model.Vehicle;
import com.routeopt.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        log.info("Creating vehicle: {}", vehicle.getName());
        return ResponseEntity.ok(vehicleRepository.save(vehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleDetails) {
        log.info("Updating vehicle with ID: {}", id);
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.setName(vehicleDetails.getName());
                    vehicle.setCapacity(vehicleDetails.getCapacity());
                    vehicle.setFuelConsumptionRate(vehicleDetails.getFuelConsumptionRate());
                    vehicle.setCostPerKm(vehicleDetails.getCostPerKm());
                    vehicle.setSpeed(vehicleDetails.getSpeed());
                    vehicle.setActive(vehicleDetails.isActive());
                    return ResponseEntity.ok(vehicleRepository.save(vehicle));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicleRepository.delete(vehicle);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
