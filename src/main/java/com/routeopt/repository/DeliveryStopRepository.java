package com.routeopt.repository;

import com.routeopt.model.DeliveryStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryStopRepository extends JpaRepository<DeliveryStop, Long> {
    List<DeliveryStop> findByStatus(String status);
}
