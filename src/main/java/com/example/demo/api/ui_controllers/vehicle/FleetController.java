package com.example.demo.api.ui_controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.ListWrapper;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.FleetService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"fleet", "vehicle"})
@RequestMapping(Constants.UI_API + "/fleets")
public class FleetController {

	private final String ENTITY = "fleet";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

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
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
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
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleetFromDatabase);
		model.addAttribute("vehicles", fleetFromDatabase.getVehicles());

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
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
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
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		Set<Vehicle> fleetVehicles = fleetFromDatabase.getVehicles();
		for (Vehicle vehicle : fleetVehicles) {
			vehicle.setIsSelected(true);
		}

		fleetVehicles.addAll(vehicleService.getAll());

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
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		String oldFleetFromDatabase = fleetFromDatabase.toString();

		if (vehiclesWrapper.getVehicles().size() > 0) {

			Set<Vehicle> vehicles = new HashSet<>();

			for (Vehicle vehicle : vehiclesWrapper.getVehicles()) {

				Vehicle vehicleFromDatabase = vehicleService.getById(vehicle.getId());

				Set<Fleet> vehicleFromDatabaseFleets = vehicleFromDatabase.getFleets();

				if (vehicle.getIsSelected()) {
					vehicles.add(vehicleFromDatabase);
					vehicleFromDatabaseFleets.add(fleetFromDatabase);
				} else {
					vehicleFromDatabaseFleets.remove(fleetFromDatabase);
				}

				vehicleFromDatabase.setFleets(vehicleFromDatabaseFleets);
			}

			fleetFromDatabase.setVehicles(vehicles);
			fleetFromDatabase = fleetService.save(fleetFromDatabase);

			eventHistoryLogService.addFleetLog("add/remove vehicles to/from " + ENTITY, ENTITY + " updated from:\n" + oldFleetFromDatabase + "\nto:\n" + fleetFromDatabase);
		}

		return Constants.REDIRECT + Constants.UI_API + "/fleets";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Fleet fleet, Model model) {
		fleet = new FieldReflectionUtils<Fleet>().getEntityWithEmptyStringValuesAsNull(fleet);

		ValidationResponse response = fleetService.validate(fleet, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addFleetLog("create " + ENTITY, ENTITY + " created:\n" + fleetFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/fleets";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Fleet fleet, Model model) {
		String oldFleetFromDatabase = fleetService.getById(fleet.getId()).toString();

		fleet = new FieldReflectionUtils<Fleet>().getEntityWithEmptyStringValuesAsNull(fleet);

		ValidationResponse response = fleetService.validate(fleet, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addFleetLog("update " + ENTITY, ENTITY + " updated from:\n" + oldFleetFromDatabase + "\nto:\n" + fleetFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/fleets/" + fleetFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		fleetService.delete(fleetFromDatabase);

		eventHistoryLogService.addFleetLog("delete " + ENTITY, ENTITY + " deleted:\n" + fleetFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/fleets";
	}
}
