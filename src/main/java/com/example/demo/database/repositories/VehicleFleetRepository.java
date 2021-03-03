package com.example.demo.database.repositories;

import com.example.demo.database.models.VehicleFleet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("fleets")
public interface VehicleFleetRepository extends JpaRepository<VehicleFleet, Long> {

}
