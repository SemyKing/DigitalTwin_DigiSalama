package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.EquipmentService;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
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
@SessionAttributes("equipment")
@RequestMapping(Constants.UI_API + "/equipment")
public class EquipmentController {

	private static final String ENTITY = "equipment";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final EquipmentService equipmentService;

	@Autowired
	private final EquipmentTypeService typeService;

	@Autowired
	private final VehicleService vehicleService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Equipment> equipment = equipmentService.getAll();
		model.addAttribute(ENTITY, equipment);

		return "vehicle/equipment/equipment_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Equipment equipment = equipmentService.getById(id);

		if (equipment == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, equipment);

		return "vehicle/equipment/equipment_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Equipment());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<EquipmentType> types = typeService.getAll();
		model.addAttribute("equipment_types", types);

		return "vehicle/equipment/new_equipment_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Equipment equipment = equipmentService.getById(id);

		if (equipment == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, equipment);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<EquipmentType> types = typeService.getAll();
		model.addAttribute("equipment_types", types);

		return "vehicle/equipment/edit_equipment_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Equipment equipment, Model model) {
		equipment = new FieldReflectionUtils<Equipment>().getEntityWithEmptyStringValuesAsNull(equipment);

		ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"crate " + ENTITY,
					ENTITY + " created:\n" + equipmentFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/equipment";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Equipment equipment, Model model) {
		String oldEquipmentFromDatabase = equipmentService.getById(equipment.getId()).toString();

		equipment = new FieldReflectionUtils<Equipment>().getEntityWithEmptyStringValuesAsNull(equipment);

		ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			addLog(
					"update " + ENTITY,
					ENTITY + " updated from:\n" + oldEquipmentFromDatabase + "\nto:\n" + equipmentFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/equipment/" + equipmentFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {

		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		equipmentService.delete(equipmentFromDatabase);

		addLog(
				"delete " + ENTITY,
				ENTITY + " deleted:\n" + equipmentFromDatabase);

		return Constants.REDIRECT + "/equipment";
	}

	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForEquipment()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}
}
