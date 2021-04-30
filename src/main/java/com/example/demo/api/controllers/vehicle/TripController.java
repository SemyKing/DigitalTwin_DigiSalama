package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.TripService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"trip"})
@RequestMapping(Constants.UI_API + "/trips")
public class TripController {

	private final String ENTITY = "trip";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final TripService tripService;

	@Autowired
	private final VehicleService vehicleService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Trip> trips = tripService.getAll();
		model.addAttribute("trips", trips);

		return "vehicle/trips/trips_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Trip tripFromDatabase = tripService.getById(id);

		if (tripFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, tripFromDatabase);


		return "vehicle/trips/trip_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Trip());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/trips/new_trip_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Trip tripFromDatabase = tripService.getById(id);

		if (tripFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, tripFromDatabase);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/trips/edit_trip_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Trip trip, Model model) {
		trip = new FieldReflectionUtils<Trip>().getEntityWithEmptyStringValuesAsNull(trip);

		ValidationResponse response = tripService.validate(trip, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Trip tripFromDatabase = tripService.save(trip);

		if (tripFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"create " + ENTITY,
					ENTITY + " created:\n" + tripFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/trips";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Trip trip, Model model) {
		String oldTripFromDatabase = tripService.getById(trip.getId()).toString();

		trip = new FieldReflectionUtils<Trip>().getEntityWithEmptyStringValuesAsNull(trip);

		ValidationResponse response = tripService.validate(trip, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Trip tripFromDatabase = tripService.save(trip);

		if (tripFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"update " + ENTITY,
					ENTITY + " updated from:\n" + oldTripFromDatabase + "\nto:\n" + tripFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/trips/" + tripFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Trip tripFromDatabase = tripService.getById(id);

		if (tripFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		tripService.delete(tripFromDatabase);

		addLog(
				"delete " + ENTITY,
				ENTITY + " deleted:\n" + tripFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/trips";
	}

	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForTrips()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}
}
