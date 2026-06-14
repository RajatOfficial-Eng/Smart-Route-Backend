package com.routeopt.repository;

import com.routeopt.model.OptimizedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptimizedRouteRepository extends JpaRepository<OptimizedRoute, Long> {
    List<OptimizedRoute> findTop10ByOrderByCreatedAtDesc();
}
