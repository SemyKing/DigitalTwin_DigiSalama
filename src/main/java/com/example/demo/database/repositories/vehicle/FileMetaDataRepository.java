package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.FileMetaData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("file_meta_data")
public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {

    @Query("SELECT f FROM FileMetaData f WHERE f.vehicle IS NOT NULL AND f.vehicle.id = :id")
    List<FileMetaData> findAllByVehicleId(Long id);

    @Query("SELECT f FROM FileMetaData f WHERE f.refuel IS NOT NULL AND f.refuel.id = :id")
    List<FileMetaData> findAllByRefuelId(Long id);

    @Query("SELECT f FROM FileMetaData f WHERE f.refuel IS NOT NULL AND f.vehicle_event.id = :id")
    List<FileMetaData> findAllByVehicleEventId(Long id);
}
