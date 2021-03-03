package com.example.demo.api.controllers;

import com.example.demo.database.models.Vehicle;
import com.example.demo.database.services.VehicleService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class VehicleController {

	private final String ENTITY =       "vehicle";
	private final String ENTITY_LIST =  "vehicles";

	private final String GET_ALL_URL =      StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String GET_BY_ID_URL =    StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String NEW_ENTITY_URL =   StringUtils.UI_API + StringUtils.NEW +      StringUtils.FORWARD_SLASH + ENTITY;
	private final String SAVE_URL =         StringUtils.UI_API + StringUtils.SAVE +     StringUtils.FORWARD_SLASH + ENTITY;
	private final String EDIT_URL =         StringUtils.UI_API + StringUtils.EDIT +     StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String UPDATE_URL =       StringUtils.UI_API + StringUtils.UPDATE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String DELETE_URL =       StringUtils.UI_API + StringUtils.DELETE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;

	private final String ENTITY_LIST_PAGE =   "vehicle/vehicles_list_page";
	private final String ENTITY_DETAILS_PAGE = "vehicle/vehicle_details_page";
	private final String EDIT_ENTITY_PAGE =    "vehicle/edit_vehicle_page";
	private final String NEW_ENTITY_PAGE =     "vehicle/new_vehicle_page";


	@Autowired
	private final VehicleService vehicleService;


	@GetMapping(GET_ALL_URL)
	public String getAllVehicles(Model model) {
		List<Vehicle> vehicles = vehicleService.getAll();
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		model.addAttribute(ENTITY_LIST, vehicles);
		return ENTITY_LIST_PAGE;
	}


	@GetMapping(GET_BY_ID_URL)
	public String getVehicleById(@PathVariable Long id, Model model) {
		Vehicle vehicle = vehicleService.getById(id);

		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println("GET VEHICLE");
		System.out.println(vehicle);

		model.addAttribute(ENTITY, vehicle);
		return ENTITY_DETAILS_PAGE;
	}


	@GetMapping(NEW_ENTITY_URL)
	public String newVehicleForm(Model model) {
		model.addAttribute(ENTITY, new Vehicle());
		return NEW_ENTITY_PAGE;
	}


	@PostMapping(SAVE_URL)
	public String saveVehicleFromUI(@ModelAttribute Vehicle vehicle, Model model) {
		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, ENTITY + " object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " object is required");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println("SAVE VEHICLE");
		System.out.println(vehicle);

		vehicleService.save(vehicle);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@GetMapping(EDIT_URL)
	public String editVehicleForm(@PathVariable Long id, Model model) {

		Vehicle vehicle = vehicleService.getById(id);

		if (vehicle == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, vehicle);

		return EDIT_ENTITY_PAGE;
	}


	@PostMapping(UPDATE_URL)
	public String updateVehicleFromUI(@ModelAttribute Vehicle vehicle, @PathVariable Long id, Model model) {

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "ID missing");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println("UPDATE VEHICLE");
		System.out.println(vehicle);

		vehicleService.save(vehicle);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@PostMapping(DELETE_URL)
	public String deleteVehicleFromUI(@PathVariable Long id, Model model) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		vehicleService.delete(vehicleFromDatabase);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}
}
