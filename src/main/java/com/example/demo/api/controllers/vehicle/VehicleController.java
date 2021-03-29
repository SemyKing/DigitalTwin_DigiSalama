package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.services.*;
import com.example.demo.database.services.vehicle.*;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.database.models.utils.ValidationResponse;
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
	private final EventService eventService;

	@Autowired
	private final EquipmentService equipmentService;

	@Autowired
	private final OrganisationService organisationService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Vehicle> vehicles = vehicleService.getAll();

		//TODO: MAYBE REMOVE
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		model.addAttribute("vehicles", vehicles);

		return "vehicle/vehicles_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);

		return "vehicle/vehicle_details_page";
	}


	@GetMapping("/{id}/equipment")
	public String getEquipmentByVehicleId(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);

		List<Equipment> equipment = equipmentService.getAllByVehicleId(id);
		model.addAttribute("equipment", equipment);

		return "vehicle/vehicle_equipment_list_page";
	}


	@GetMapping("/{id}/trips")
	public String getTripsByVehicleId(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);

		List<Trip> trips = tripService.getAllByVehicleId(id);
		model.addAttribute("trips", trips);

		return "vehicle/vehicle_trips_list_page";
	}


	@GetMapping("/{id}/files")
	public String getFilesByVehicleId(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);


		List<FileDB> files = fileService.getAllByVehicleId(id);
		model.addAttribute("files", files);

		return "vehicle/vehicle_files_list_page";
	}

	@GetMapping("/{id}/events")
	public String getEventsByVehicleId(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);


		List<VehicleEvent> events = eventService.getAllByVehicleId(id);
		model.addAttribute("events", events);

		return "vehicle/vehicle_events_list_page";
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

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST);

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
	@PostMapping("/update")
	public String put(@ModelAttribute Vehicle vehicle, Model model) {
		if (vehicle.getId() == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Missing parameter");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT);

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
