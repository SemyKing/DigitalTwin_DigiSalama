package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.ListWrapper;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.*;
import com.example.demo.utils.FieldReflectionUtils;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"vehicle, fleet"})
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

	@Autowired
	private final DistanceService distanceService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Vehicle> vehicles = vehicleService.getAll();
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


		List<Distance> distances = distanceService.getAllByVehicleId(id);

		int totalKmDriven = 0;

		if (distances.size() > 0) {
			totalKmDriven = distances.stream().mapToInt(Distance::getKilometres).sum();
		}

		model.addAttribute("totalKmDriven", totalKmDriven);

		return "vehicle/vehicle_details_page";
	}


	@GetMapping("/{id}/fleets")
	public String getFleetsByVehicleId(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicleFromDatabase);

		Set<Fleet> fleets = vehicleFromDatabase.getFleets();
		model.addAttribute("fleets", fleets);

		return "vehicle/vehicle_fleets_list_page";
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


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Vehicle());

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "vehicle/new_vehicle_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Vehicle vehicle = vehicleService.getById(id);

		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println("EDIT VEHICLE: " + vehicle);

		model.addAttribute(ENTITY, vehicle);

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		List<Fleet> fleets = fleetService.getAll();
		model.addAttribute("fleets", fleets);

		return "vehicle/edit_vehicle_page";
	}


	// FLEETS LIST
	@GetMapping("/{id}/set_fleets")
	public String fleetsListForm(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		List<Fleet> vehicleFleets = new ArrayList<>(vehicleFromDatabase.getFleets());
		for (Fleet fleet : vehicleFleets) {
			fleet.setIsSelected(true);
		}

		List<Fleet> allOtherFleets = fleetService.getFleetsNotContainingVehicle(id);
		vehicleFleets.addAll(allOtherFleets);

		ListWrapper fleetsWrapper = new ListWrapper();
		fleetsWrapper.getFleets().addAll(vehicleFleets);

		model.addAttribute("fleetsWrapper", fleetsWrapper);
		model.addAttribute(ENTITY, vehicleFromDatabase);

		return "vehicle/add_fleets_to_vehicle_page";
	}


	// SET FLEETS FOR VEHICLE
	@PostMapping("/{id}/set_fleets")
	public String addFleetsToVehicle(@ModelAttribute ListWrapper fleetsWrapper, @PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		if (fleetsWrapper.getFleets().size() > 0) {
			Set<Fleet> fleets = new HashSet<>();

			for (Fleet fleet : fleetsWrapper.getFleets()) {
				if (fleet.getIsSelected()) {
					Fleet fleetFromDatabase = fleetService.getById(fleet.getId());
					fleets.add(fleetFromDatabase);
				}
			}

			vehicleFromDatabase.setFleets(fleets);

			vehicleService.save(vehicleFromDatabase);
		}

		return StringUtils.REDIRECT + StringUtils.UI_API + "/vehicles" + id + "/edit";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Vehicle vehicle, Model model) {
		vehicle = new FieldReflectionUtils<Vehicle>().getObjectWithEmptyStringValuesAsNull(vehicle);

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


	@PostMapping("/update")
	public String put(@ModelAttribute Vehicle vehicle, Model model) {
		FieldReflectionUtils<Vehicle> util = new FieldReflectionUtils<>();
		vehicle = util.getObjectWithEmptyStringValuesAsNull(vehicle);

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


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		vehicleService.delete(vehicleFromDatabase);

		return StringUtils.REDIRECT + StringUtils.UI_API + "/vehicles";
	}
}
