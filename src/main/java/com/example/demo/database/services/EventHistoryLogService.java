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


	private User getCurrentUser() {
		return userService.getCurrentUser();
	}


	public void addUserLog(String action, String description) {
		if (isLoggingEnabledForUsers()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForUsers() {
		return applicationSettingsService.getApplicationSettings().isUser_event_logging();
	}


	public void addOrganisationLog(String action, String description) {
		if (isLoggingEnabledForOrganisations()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForOrganisations() {
		return applicationSettingsService.getApplicationSettings().isOrganisation_event_logging();
	}


	public void addFleetLog(String action, String description) {
		if (isLoggingEnabledForFleets()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForFleets() {
		return applicationSettingsService.getApplicationSettings().isFleet_event_logging();
	}


	public void addVehicleLog(String action, String description) {
		if (isLoggingEnabledForVehicles()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForVehicles() {
		return applicationSettingsService.getApplicationSettings().isVehicle_event_logging();
	}


	public void addVehicleEventLog(String action, String description) {
		if (isLoggingEnabledForVehicleEvents()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForVehicleEvents() {
		return applicationSettingsService.getApplicationSettings().isVehicle_event_event_logging();
	}


	public void addDistanceLog(String action, String description) {
		if (isLoggingEnabledForDistances()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForDistances() {
		return applicationSettingsService.getApplicationSettings().isDistance_event_logging();
	}


	public void addRefuelLog(String action, String description) {
		if (isLoggingEnabledForRefuels()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForRefuels() {
		return applicationSettingsService.getApplicationSettings().isRefuel_event_logging();
	}


	public void addTripLog(String action, String description) {
		if (isLoggingEnabledForTrips()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForTrips() {
		return applicationSettingsService.getApplicationSettings().isTrip_event_logging();
	}


	public void addEquipmentLog(String action, String description) {
		if (isLoggingEnabledForEquipment()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForEquipment() {
		return applicationSettingsService.getApplicationSettings().isEquipment_event_logging();
	}


	public void addEquipmentTypeLog(String action, String description) {
		if (isLoggingEnabledForEquipmentTypes()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	public boolean isLoggingEnabledForEquipmentTypes() {
		return applicationSettingsService.getApplicationSettings().isEquipment_type_event_logging();
	}


	public void addFileLog(String action, String description) {
		if (isLoggingEnabledForFiles()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(getCurrentUser() == null ? "NULL" : getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			save(log);
		}
	}

	private boolean isLoggingEnabledForFiles() {
		return applicationSettingsService.getApplicationSettings().isFile_event_logging();
	}
}
