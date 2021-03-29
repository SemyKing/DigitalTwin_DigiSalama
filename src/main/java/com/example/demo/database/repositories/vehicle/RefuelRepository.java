package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Trip;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("trips")
public interface RefuelRepository extends JpaRepository<Refuel, Long> {

    @Query("SELECT r FROM Refuel r WHERE r.vehicle IS NOT NULL AND r.vehicle.id = :id")
    List<Refuel> findAllByVehicleId(Long id);
}
