package com.example.demo.database.services;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.user.User;
import com.example.demo.database.repositories.EventHistoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventHistoryLogService {

	private final EventHistoryLogRepository repository;

	private final ApplicationSettingsService applicationSettingsService;
	private final UserService userService;


	public List<EventHistoryLog> getAll() {
		return repository.findAll();
	}

	public EventHistoryLog getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<EventHistoryLog> eventHistoryLog = repository.findById(id);

		if (eventHistoryLog.isEmpty()) {
			return null;
		}

		return eventHistoryLog.get();
	}

	public EventHistoryLog save(EventHistoryLog eventHistoryLog) {
		return repository.save(eventHistoryLog);
	}

	public void delete(EventHistoryLog eventHistoryLog) {
		if (eventHistoryLog == null || eventHistoryLog.getId() == null) {
			return;
		}

		repository.delete(eventHistoryLog);
	}


	public boolean isLoggingEnabledForUsers() {
		return applicationSettingsService.getApplicationSettings().isUser_event_logging();
	}

	public boolean isLoggingEnabledForOrganisations() {
		return applicationSettingsService.getApplicationSettings().isOrganisation_event_logging();
	}

	public boolean isLoggingEnabledForFleets() {
		return applicationSettingsService.getApplicationSettings().isFleet_event_logging();
	}

	public boolean isLoggingEnabledForVehicles() {
		return applicationSettingsService.getApplicationSettings().isVehicle_event_logging();
	}

	public boolean isLoggingEnabledForVehicleEvents() {
		return applicationSettingsService.getApplicationSettings().isVehicle_event_event_logging();
	}

	public boolean isLoggingEnabledForDistances() {
		return applicationSettingsService.getApplicationSettings().isDistance_event_logging();
	}

	public boolean isLoggingEnabledForRefuels() {
		return applicationSettingsService.getApplicationSettings().isRefuel_event_logging();
	}

	public boolean isLoggingEnabledForTrips() {
		return applicationSettingsService.getApplicationSettings().isTrip_event_logging();
	}

	public boolean isLoggingEnabledForEquipment() {
		return applicationSettingsService.getApplicationSettings().isEquipment_event_logging();
	}

	public boolean isLoggingEnabledForEquipmentTypes() {
		return applicationSettingsService.getApplicationSettings().isEquipment_type_event_logging();
	}

	public boolean isLoggingEnabledForFiles() {
		return applicationSettingsService.getApplicationSettings().isFile_event_logging();
	}

	public User getCurrentUser() {
		return userService.getCurrentUser();
	}
}
