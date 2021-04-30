package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.database.services.vehicle.VehicleEventService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"event"})
@RequestMapping(Constants.UI_API + "/vehicle_events")
public class VehicleEventController {

	private final String ENTITY = "event";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final VehicleEventService vehicleEventService;

	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final FileService fileService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<VehicleEvent> events = vehicleEventService.getAll();
		model.addAttribute("events", events);

		return "vehicle/events/events_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, eventFromDatabase);

		return "vehicle/events/event_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new VehicleEvent());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<FileDB> files = fileService.getAll();
		model.addAttribute("files", files);

		return "vehicle/events/new_event_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, eventFromDatabase);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<FileDB> files = fileService.getAll();
		model.addAttribute("files", files);

		return "vehicle/events/edit_event_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute VehicleEvent event, Model model) {
		event = new FieldReflectionUtils<VehicleEvent>().getEntityWithEmptyStringValuesAsNull(event);

		ValidationResponse response = vehicleEventService.validate(event, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(ENTITY, event);
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			List<Vehicle> vehicles = vehicleService.getAll();
			model.addAttribute("vehicles", vehicles);

			List<FileDB> files = fileService.getAll();
			model.addAttribute("files", files);

			return "vehicle/events/new_event_page";
		}

		VehicleEvent eventFromDatabase = vehicleEventService.save(event);

		if (eventFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"create " + ENTITY,
					ENTITY + " created:\n" + eventFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/vehicle_events";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute VehicleEvent event, Model model, HttpServletRequest request) {
		String oldEventFromDatabase = vehicleEventService.getById(event.getId()).toString();

		event = new FieldReflectionUtils<VehicleEvent>().getEntityWithEmptyStringValuesAsNull(event);

		ValidationResponse response = vehicleEventService.validate(event, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			String referer = request.getHeader("Referer");

			if (referer.contains("/edit")) {
				model.addAttribute(ENTITY, event);

				List<Vehicle> vehicles = vehicleService.getAll();
				model.addAttribute("vehicles", vehicles);

				List<FileDB> files = fileService.getAll();
				model.addAttribute("files", files);

				return referer;
			}

			return Constants.ERROR_PAGE;
		}

		VehicleEvent eventFromDatabase = vehicleEventService.save(event);

		if (eventFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"update " + ENTITY,
					ENTITY + " updated from:\n" + oldEventFromDatabase + "\nto:\n" + eventFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/vehicle_events/" + eventFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		vehicleEventService.delete(eventFromDatabase);

		addLog(
				"delete " + ENTITY,
				ENTITY + " deleted:\n" + eventFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/vehicle_events";
	}

	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForVehicleEvents()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}
}
