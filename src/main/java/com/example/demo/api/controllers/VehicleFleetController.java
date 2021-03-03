package com.example.demo.api.controllers;

import com.example.demo.database.models.Vehicle;
import com.example.demo.database.models.VehicleFleet;
import com.example.demo.database.services.VehicleFleetService;
import com.example.demo.database.services.VehicleService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class VehicleFleetController {

	private final String ENTITY =       "fleet";
	private final String ENTITY_LIST =  "fleets";

	private final String GET_ALL_URL =      StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String GET_BY_ID_URL =    StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String NEW_ENTITY_URL =   StringUtils.UI_API + StringUtils.NEW +      StringUtils.FORWARD_SLASH + ENTITY;
	private final String SAVE_URL =         StringUtils.UI_API + StringUtils.SAVE +     StringUtils.FORWARD_SLASH + ENTITY;
	private final String EDIT_URL =         StringUtils.UI_API + StringUtils.EDIT +     StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String UPDATE_URL =       StringUtils.UI_API + StringUtils.UPDATE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String DELETE_URL =       StringUtils.UI_API + StringUtils.DELETE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;

	private final String ENTITY_LIST_PAGE =   "vehicle/fleets/fleets_list_page";
	private final String ENTITY_DETAILS_PAGE = "vehicle/fleets/fleet_details_page";
	private final String EDIT_ENTITY_PAGE =    "vehicle/fleets/edit_fleet_page";
	private final String NEW_ENTITY_PAGE =     "vehicle/fleets/new_fleet_page";


	@Autowired
	private final VehicleFleetService vehicleFleetService;


	@GetMapping(GET_ALL_URL)
	public String getAllFleets(Model model) {
		List<VehicleFleet> fleets = vehicleFleetService.getAll();
		fleets.sort(Comparator.comparing(VehicleFleet::getId));

		model.addAttribute(ENTITY_LIST, fleets);
		return ENTITY_LIST_PAGE;
	}


	@GetMapping(GET_BY_ID_URL)
	public String getFleetById(@PathVariable Long id, Model model) {
		VehicleFleet fleet = vehicleFleetService.getById(id);

		if (fleet == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleet);
		return ENTITY_DETAILS_PAGE;
	}


	@GetMapping(NEW_ENTITY_URL)
	public String newFleetForm(Model model) {
		model.addAttribute(ENTITY, new VehicleFleet());
		return NEW_ENTITY_PAGE;
	}


	@GetMapping(EDIT_URL)
	public String editFleetForm(@PathVariable Long id, Model model) {
		VehicleFleet fleet = vehicleFleetService.getById(id);

		if (fleet == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fleet);

		return EDIT_ENTITY_PAGE;
	}


	@PostMapping(SAVE_URL)
	public String saveFleetFromUI(@ModelAttribute VehicleFleet fleet, Model model) {

		if (fleet.getName().length() <= 0) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " name is required");
			model.addAttribute(ENTITY, fleet);
			return EDIT_ENTITY_PAGE;
		}

		vehicleFleetService.save(fleet);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@PostMapping(UPDATE_URL)
	public String updateFleetFromUI(@ModelAttribute VehicleFleet fleet, @PathVariable Long id, Model model) {

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, ENTITY + " ID missing");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		VehicleFleet fleetFromDatabase = vehicleFleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		vehicleFleetService.save(fleet);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@PostMapping(DELETE_URL)
	public String deleteFleetFromUI(@PathVariable Long id, Model model) {
		VehicleFleet fleetFromDatabase = vehicleFleetService.getById(id);

		if (fleetFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		vehicleFleetService.delete(fleetFromDatabase);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}
}
