package com.example.demo.database.repositories;

import com.example.demo.database.models.Vehicle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("vehicles")
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

}
