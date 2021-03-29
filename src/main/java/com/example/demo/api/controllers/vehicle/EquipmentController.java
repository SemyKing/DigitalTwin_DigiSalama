package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.vehicle.EquipmentService;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
import com.example.demo.database.services.vehicle.VehicleService;
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
@SessionAttributes("equipment")
@RequestMapping(StringUtils.UI_API + "/equipment")
public class EquipmentController {

	private static final String ENTITY = "equipment";

	@Autowired
	private final EquipmentService equipmentService;

	@Autowired
	private final EquipmentTypeService typeService;

	@Autowired
	private final VehicleService vehicleService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Equipment> equipment = equipmentService.getAll();

		//TODO: MAYBE REMOVE
		equipment.sort(Comparator.comparing(Equipment::getId));

		model.addAttribute(ENTITY, equipment);

		return "vehicle/equipment/equipment_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Equipment equipment = equipmentService.getById(id);

		if (equipment == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, equipment);

		return "vehicle/equipment/equipment_details_page";
	}



	// NEW EQUIPMENT FORM
	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Equipment());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<EquipmentType> types = typeService.getAll();
		model.addAttribute("equipment_types", types);

		return "vehicle/equipment/new_equipment_page";
	}


	// EDIT EQUIPMENT FORM
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Equipment equipment = equipmentService.getById(id);

		if (equipment == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, equipment);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<EquipmentType> types = typeService.getAll();
		model.addAttribute("equipment_types", types);

		return "vehicle/equipment/edit_equipment_page";
	}


	// POST EQUIPMENT
	@PostMapping({"", "/"})
	public String post(@ModelAttribute Equipment equipment, Model model) {
		ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/equipment";
		}
	}


	// UPDATE EQUIPMENT
	@PostMapping("/update")
	public String put(@ModelAttribute Equipment equipment, Model model) {
		if (equipment.getId() == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Missing parameter");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/equipment/" + equipmentFromDatabase.getId();
		}
	}


	// DELETE EQUIPMENT
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {

		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		equipmentService.delete(equipmentFromDatabase);

		return StringUtils.REDIRECT + "/equipment";
	}
}
