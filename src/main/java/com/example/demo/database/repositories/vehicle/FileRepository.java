package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.FileDB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("images")
public interface FileRepository extends JpaRepository<FileDB, Long> {

    @Query("SELECT f FROM FileDB f WHERE f.vehicle IS NOT NULL AND f.vehicle.id = :id")
    List<FileDB> findAllByVehicleId(Long id);

    @Query("SELECT f FROM FileDB f WHERE f.refuel IS NOT NULL AND f.refuel.id = :id")
    List<FileDB> findAllByRefuelId(Long id);

    @Query("SELECT f FROM FileDB f WHERE f.refuel IS NOT NULL AND f.vehicle_event.id = :id")
    List<FileDB> findAllByVehicleEventId(Long id);
}
