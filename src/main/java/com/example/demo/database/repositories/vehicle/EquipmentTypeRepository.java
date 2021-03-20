package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.EquipmentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("equipment_types")
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {

}
