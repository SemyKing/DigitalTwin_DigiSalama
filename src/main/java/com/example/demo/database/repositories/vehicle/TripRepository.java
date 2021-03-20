package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("trips")
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.vehicle IS NOT NULL AND t.vehicle.id = :id")
    List<Trip> findAllByVehicleId(Long id);
}
