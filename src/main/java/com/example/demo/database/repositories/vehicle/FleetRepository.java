package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.Fleet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("fleets")
public interface FleetRepository extends JpaRepository<Fleet, Long> {

    @Query("SELECT f FROM Fleet f WHERE f.organisation IS NOT NULL AND f.organisation.id = :id")
    List<Fleet> findAllByOrganisationId(@Param("id") Long id);
}
