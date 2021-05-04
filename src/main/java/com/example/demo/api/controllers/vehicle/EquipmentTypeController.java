package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
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
@SessionAttributes("equipment_type")
@RequestMapping(Constants.UI_API + "/equipment_types")
public class EquipmentTypeController {

	private final String ENTITY = "equipment_type";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final EquipmentTypeService typeService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<EquipmentType> types = typeService.getAll();
		model.addAttribute("equipment_types", types);

		return "vehicle/equipment_type/equipment_type_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		EquipmentType type = typeService.getById(id);

		if (type == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such element");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, type);

		return "vehicle/equipment_type/equipment_type_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new EquipmentType());

		return "vehicle/equipment_type/new_equipment_type_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		EquipmentType type = typeService.getById(id);

		if (type == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, type);

		return "vehicle/equipment_type/edit_equipment_type_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute EquipmentType type, Model model) {
		type = new FieldReflectionUtils<EquipmentType>().getEntityWithEmptyStringValuesAsNull(type);

		ValidationResponse response = typeService.validate(type, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}


		EquipmentType typeFromDatabase = typeService.save(type);

		if (typeFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addEquipmentTypeLog("create " + ENTITY, ENTITY + " created:\n" + typeFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/equipment_types";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute EquipmentType type, Model model) {
		String oldTypeFromDatabase = typeService.getById(type.getId()).toString();

		type = new FieldReflectionUtils<EquipmentType>().getEntityWithEmptyStringValuesAsNull(type);

		ValidationResponse response = typeService.validate(type, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		EquipmentType typeFromDatabase = typeService.save(type);

		if (typeFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addEquipmentTypeLog("update " + ENTITY, ENTITY + " updated from:\n" + oldTypeFromDatabase + "\nto:\n" + typeFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/equipment_types/" + typeFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		EquipmentType typeFromDatabase = typeService.getById(id);

		if (typeFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		typeService.delete(typeFromDatabase);

		eventHistoryLogService.addEquipmentTypeLog("delete " + ENTITY, ENTITY + " deleted:\n" + typeFromDatabase);

		return Constants.REDIRECT + "/equipment_types";
	}
}
