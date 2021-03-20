package com.example.demo.database.repositories;

import com.example.demo.database.models.Organisation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Qualifier("organisations")
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    @Query("SELECT o FROM Organisation o WHERE o.name = :name")
    Optional<Organisation> findOrganisationByName(@Param("name") String name);
}
