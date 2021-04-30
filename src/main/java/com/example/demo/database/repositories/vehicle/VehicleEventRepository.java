package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.VehicleEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("trips")
public interface VehicleEventRepository extends JpaRepository<VehicleEvent, Long> {

    @Query("SELECT e FROM VehicleEvent e WHERE e.vehicle IS NOT NULL AND e.vehicle.id = :id")
    List<VehicleEvent> findAllByVehicleId(Long id);
}
