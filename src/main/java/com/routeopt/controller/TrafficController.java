package com.routeopt.controller;

import com.routeopt.model.TrafficZone;
import com.routeopt.repository.TrafficZoneRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private static final Logger log = LoggerFactory.getLogger(TrafficController.class);

    @Autowired
    private TrafficZoneRepository trafficZoneRepository;

    @GetMapping
    public ResponseEntity<List<TrafficZone>> getAllTrafficZones() {
        return ResponseEntity.ok(trafficZoneRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<TrafficZone> createTrafficZone(@Valid @RequestBody TrafficZone zone) {
        log.info("Creating traffic zone at coordinate [{}, {}] with radius {} and severity {}", 
                zone.getX(), zone.getY(), zone.getRadius(), zone.getSeverity());
        return ResponseEntity.ok(trafficZoneRepository.save(zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrafficZone(@PathVariable Long id) {
        log.info("Deleting traffic zone with ID: {}", id);
        return trafficZoneRepository.findById(id)
                .map(zone -> {
                    trafficZoneRepository.delete(zone);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAllTraffic() {
        log.info("Clearing all traffic zones");
        trafficZoneRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
