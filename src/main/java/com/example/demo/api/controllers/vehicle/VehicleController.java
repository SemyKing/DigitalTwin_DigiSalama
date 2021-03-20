package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.*;
import com.example.demo.database.services.vehicle.FleetService;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.database.services.vehicle.TripService;
import com.example.demo.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("vehicle")
@RequestMapping(StringUtils.UI_API + "/vehicles")
public class VehicleController {

	private static final String ENTITY = "vehicle";

	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final FleetService fleetService;

	@Autowired
	private final TripService tripService;

	@Autowired
	private final FileService fileService;

	@Autowired
	private final OrganisationService organisationService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Vehicle> vehicles = vehicleService.getAll();
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		model.addAttribute("vehicles", vehicles);

		return "vehicle/vehicles_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Vehicle vehicle = vehicleService.getById(id);

		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicle);

		List<Trip> trips = tripService.getAllByVehicleId(id);
		model.addAttribute("trips", trips);

		List<FileDB> fileDBS = fileService.getAllByVehicleId(id);
		model.addAttribute("images", fileDBS);

		return "vehicle/vehicle_details_page";
	}


	// NEW VEHICLE FORM
	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Vehicle());

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		List<Fleet> fleets = fleetService.getAll();
		model.addAttribute("fleets", fleets);

		return "vehicle/new_vehicle_page";
	}


	// EDIT VEHICLE FORM
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Vehicle vehicle = vehicleService.getById(id);

		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicle);

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		List<Fleet> fleets = fleetService.getAll();
		model.addAttribute("fleets", fleets);

		return "vehicle/edit_vehicle_page";
	}


	// POST VEHICLE
	@PostMapping({"", "/"})
	public String post(@ModelAttribute Vehicle vehicle, Model model) {

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST_UI);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/vehicles";
		}
	}


	// UPDATE VEHICLE
	@PostMapping("/{id}/update")
	public String put(@ModelAttribute Vehicle vehicle, Model model) {
		if (vehicle.getId() == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Missing parameter");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT_UI);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/vehicles/" + vehicleFromDatabase.getId();
		}
	}


	// DELETE VEHICLE
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		vehicleService.delete(vehicleFromDatabase);

		return StringUtils.REDIRECT + "/vehicles";
	}
}
