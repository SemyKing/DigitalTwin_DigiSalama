package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.Trip;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("equipment")
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("SELECT e FROM Equipment e WHERE e.type IS NOT NULL AND e.type.id = :id")
    List<Trip> findAllByTypeId(@Param("id") Long id);

    @Query("SELECT e FROM Equipment e WHERE e.vehicle IS NOT NULL AND e.vehicle.id = :id")
    List<Equipment> findAllByVehicleId(@Param("id") Long id);
}
