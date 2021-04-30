package com.example.demo.database.services;

import com.example.demo.database.models.ApplicationSettings;
import com.example.demo.database.repositories.ApplicationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationSettingsService {

    private final ApplicationSettingsRepository repository;
    private final boolean USE_LOG = true;

    public ApplicationSettings getApplicationSettings() {
        if (repository.findAll().size() <= 0) {

            ApplicationSettings settings = new ApplicationSettings();
            settings.setUser_event_logging(USE_LOG);
            settings.setOrganisation_event_logging(USE_LOG);
            settings.setFleet_event_logging(USE_LOG);
            settings.setVehicle_event_logging(USE_LOG);
            settings.setVehicle_event_event_logging(USE_LOG);
            settings.setDistance_event_logging(USE_LOG);
            settings.setRefuel_event_logging(USE_LOG);
            settings.setTrip_event_logging(USE_LOG);
            settings.setEquipment_event_logging(USE_LOG);
            settings.setEquipment_type_event_logging(USE_LOG);
            settings.setFile_event_logging(USE_LOG);

            return repository.save(settings);
        }

        return repository.findAll().get(0);
    }

    public ApplicationSettings save(ApplicationSettings applicationSettings) {

        ApplicationSettings settings = getApplicationSettings();
        settings.setUser_event_logging(             applicationSettings.isUser_event_logging());
        settings.setOrganisation_event_logging(     applicationSettings.isOrganisation_event_logging());
        settings.setFleet_event_logging(            applicationSettings.isFleet_event_logging());
        settings.setVehicle_event_logging(          applicationSettings.isVehicle_event_logging());
        settings.setVehicle_event_event_logging(    applicationSettings.isVehicle_event_event_logging());
        settings.setDistance_event_logging(         applicationSettings.isDistance_event_logging());
        settings.setRefuel_event_logging(           applicationSettings.isRefuel_event_logging());
        settings.setTrip_event_logging(             applicationSettings.isTrip_event_logging());
        settings.setEquipment_event_logging(        applicationSettings.isEquipment_event_logging());
        settings.setEquipment_type_event_logging(   applicationSettings.isEquipment_type_event_logging());
        settings.setFile_event_logging(             applicationSettings.isFile_event_logging());

        return repository.save(settings);
    }
}
