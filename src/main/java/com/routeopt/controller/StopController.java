package com.routeopt.controller;

import com.routeopt.model.DeliveryStop;
import com.routeopt.repository.DeliveryStopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
@CrossOrigin
public class StopController {

    @Autowired
    private DeliveryStopRepository stopRepository;

    @GetMapping
    public ResponseEntity<List<DeliveryStop>> getAllStops() {
        return ResponseEntity.ok(stopRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<DeliveryStop> createStop(@RequestBody DeliveryStop stop) {
        if (stop.getStatus() == null) {
            stop.setStatus("PENDING");
        }
        return ResponseEntity.ok(stopRepository.save(stop));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryStop> updateStop(@PathVariable Long id, @RequestBody DeliveryStop stopDetails) {
        return stopRepository.findById(id)
                .map(stop -> {
                    stop.setName(stopDetails.getName());
                    stop.setX(stopDetails.getX());
                    stop.setY(stopDetails.getY());
                    stop.setDemand(stopDetails.getDemand());
                    stop.setTimeWindowStart(stopDetails.getTimeWindowStart());
                    stop.setTimeWindowEnd(stopDetails.getTimeWindowEnd());
                    stop.setServiceTime(stopDetails.getServiceTime());
                    stop.setStatus(stopDetails.getStatus());
                    return ResponseEntity.ok(stopRepository.save(stop));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStop(@PathVariable Long id) {
        return stopRepository.findById(id)
                .map(stop -> {
                    stopRepository.delete(stop);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
