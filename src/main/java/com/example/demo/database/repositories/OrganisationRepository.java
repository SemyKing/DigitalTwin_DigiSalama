package com.example.demo.database.repositories;

import com.example.demo.database.models.Organisation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("organisations")
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

}
