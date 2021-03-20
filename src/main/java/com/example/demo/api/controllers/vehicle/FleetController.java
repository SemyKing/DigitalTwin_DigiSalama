package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleListWrapper;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.FleetService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"fleet", "vehicle"})
@RequestMapping(StringUtils.UI_API + "/fleets")
public class FleetController {

	private final String ENTITY =       "fleet";

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
		Fleet fleet = fleetService.getById(id);

		if (fleet == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleet);

		List<Vehicle> vehiclesInFleet = vehicleService.getAllByFleetId(id);
		model.addAttribute("vehicles", vehiclesInFleet);

		return "vehicle/fleets/fleet_details_page";
	}


	// NEW FLEET FORM
	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Fleet());

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "vehicle/fleets/new_fleet_page";
	}


	// EDIT FLEET FORM
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleetFromDatabase);

		return "vehicle/fleets/edit_fleet_page";
	}


	// VEHICLES TO BE ADDED TO FLEET LIST
	@GetMapping("/{id}/add_vehicles")
	public String vehiclesListForm(@ModelAttribute Fleet fleet, Model model) {
		List<Vehicle> vehicles = vehicleService.getAll();

		VehicleListWrapper vehicleListWrapper = new VehicleListWrapper();
		vehicleListWrapper.getVehicles().addAll(vehicles);

		model.addAttribute("vehicleList", vehicleListWrapper);
		model.addAttribute(ENTITY, fleet);

		return "vehicle/fleets/add_vehicles_to_fleet_page";
	}


	// ADD VEHICLES TO FLEET
	@PostMapping("/{id}/add_vehicles")
	public String addVehiclesToFleet(@ModelAttribute VehicleListWrapper vehicleListWrapper, @PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		for (Vehicle vehicle : vehicleListWrapper.getVehicles()) {
			if (vehicle.getIsSelected()) {

				vehicle.setFleet(fleetFromDatabase);
				vehicle.setIsSelected(false);

				vehicleService.save(vehicle);
			}
		}

		return StringUtils.REDIRECT + "/fleets";
	}


	// POST FLEET
	@PostMapping({"", "/"})
	public String post(@ModelAttribute Fleet fleet, Model model) {
		ValidationResponse response = fleetService.validate(fleet, Mapping.POST_UI);

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


	// UPDATE FLEET
	@PostMapping("/update")
	public String put(@ModelAttribute Fleet fleet, Model model) {
		if (fleet.getId() == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Missing parameter");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		ValidationResponse response = fleetService.validate(fleet, Mapping.PUT_UI);

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


	// DELETE FLEET
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
