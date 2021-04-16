package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Distance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("distances")
public interface DistanceRepository extends JpaRepository<Distance, Long> {

    @Query("SELECT d FROM Distance d WHERE d.vehicle IS NOT NULL AND d.vehicle.id = :id")
    List<Distance> findAllByVehicleId(Long id);
}
