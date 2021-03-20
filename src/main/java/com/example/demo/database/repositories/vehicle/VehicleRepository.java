package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Vehicle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("vehicles")
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE v.fleet IS NOT NULL AND v.fleet.id = :id")
    List<Vehicle> findAllByFleetId(@Param("id") Long id);

}
