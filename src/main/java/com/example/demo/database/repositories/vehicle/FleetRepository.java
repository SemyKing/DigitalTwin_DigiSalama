package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Fleet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("fleets")
public interface FleetRepository extends JpaRepository<Fleet, Long> {

}
