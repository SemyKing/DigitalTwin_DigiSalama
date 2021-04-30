package com.example.demo.database.repositories;

import com.example.demo.database.models.ApplicationSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("application_settings")
public interface ApplicationSettingsRepository extends JpaRepository<ApplicationSettings, Long> {

}
