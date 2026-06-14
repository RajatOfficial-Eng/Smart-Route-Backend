package com.routeopt.controller;

import com.routeopt.model.TrafficZone;
import com.routeopt.repository.TrafficZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin
public class TrafficController {

    @Autowired
    private TrafficZoneRepository trafficZoneRepository;

    @GetMapping
    public ResponseEntity<List<TrafficZone>> getAllTrafficZones() {
        return ResponseEntity.ok(trafficZoneRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<TrafficZone> createTrafficZone(@RequestBody TrafficZone zone) {
        return ResponseEntity.ok(trafficZoneRepository.save(zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrafficZone(@PathVariable Long id) {
        return trafficZoneRepository.findById(id)
                .map(zone -> {
                    trafficZoneRepository.delete(zone);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAllTraffic() {
        trafficZoneRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
