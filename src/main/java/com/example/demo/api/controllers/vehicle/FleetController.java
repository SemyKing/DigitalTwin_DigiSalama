package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.ListWrapper;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.FleetService;
import com.example.demo.database.services.vehicle.VehicleService;
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
@SessionAttributes({"fleet", "vehicle"})
@RequestMapping(StringUtils.UI_API + "/fleets")
public class FleetController {

	private final String ENTITY = "fleet";

	@Autowired
	private final FleetService fleetService;

	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final OrganisationService organisationService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Fleet> fleets = fleetService.getAll();
		model.addAttribute("fleets", fleets);

		return "vehicle/fleets/fleets_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleetFromDatabase);

		Set<Vehicle> vehiclesInFleet = fleetFromDatabase.getVehicles();
		model.addAttribute("vehicles_count", vehiclesInFleet.size());

		return "vehicle/fleets/fleet_details_page";
	}


	@GetMapping("/{id}/vehicles")
	public String getVehiclesByFleetsId(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleetFromDatabase);

		Set<Vehicle> vehicles = fleetFromDatabase.getVehicles();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/fleets/fleet_vehicles_list_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Fleet());

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "vehicle/fleets/new_fleet_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleetFromDatabase);

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "vehicle/fleets/edit_fleet_page";
	}


	// VEHICLES LIST
	@GetMapping("/{id}/set_vehicles")
	public String vehiclesListForm(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		List<Vehicle> fleetVehicles = new ArrayList<>(fleetFromDatabase.getVehicles());
		for (Vehicle vehicle : fleetVehicles) {
			vehicle.setIsSelected(true);
		}

		List<Vehicle> vehiclesNotInFleet = vehicleService.getVehiclesNotInFleet(id);
		fleetVehicles.addAll(vehiclesNotInFleet);

		ListWrapper vehiclesWrapper = new ListWrapper();
		vehiclesWrapper.getVehicles().addAll(fleetVehicles);

		model.addAttribute("vehiclesWrapper", vehiclesWrapper);
		model.addAttribute(ENTITY, fleetFromDatabase);

		return "vehicle/fleets/add_vehicles_to_fleet_page";
	}


	// SET VEHICLES FOR FLEET
	@PostMapping("/{id}/set_vehicles")
	public String addVehiclesToFleet(@ModelAttribute ListWrapper vehiclesWrapper, @PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		if (vehiclesWrapper.getVehicles().size() > 0) {
			Set<Vehicle> vehicles = new HashSet<>();

			for (Vehicle vehicle : vehiclesWrapper.getVehicles()) {
				if (vehicle.getIsSelected()) {
					Vehicle vehicleFromDatabase = vehicleService.getById(vehicle.getId());
					vehicles.add(vehicleFromDatabase);
				}
			}

			fleetFromDatabase.setVehicles(vehicles);
			fleetService.save(fleetFromDatabase);
		}

		return StringUtils.REDIRECT + StringUtils.UI_API + "/fleets";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Fleet fleet, Model model) {
		fleet = new FieldReflectionUtils<Fleet>().getObjectWithEmptyStringValuesAsNull(fleet);

		ValidationResponse response = fleetService.validate(fleet, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/fleets";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Fleet fleet, Model model) {
		fleet = new FieldReflectionUtils<Fleet>().getObjectWithEmptyStringValuesAsNull(fleet);

		ValidationResponse response = fleetService.validate(fleet, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/fleets/" + fleetFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		fleetService.delete(fleetFromDatabase);

		return StringUtils.REDIRECT + StringUtils.UI_API + "/fleets";
	}
}
