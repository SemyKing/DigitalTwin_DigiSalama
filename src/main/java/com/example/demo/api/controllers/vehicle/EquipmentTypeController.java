package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
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
@SessionAttributes("equipment_type")
@RequestMapping(StringUtils.UI_API + "/equipment_types")
public class EquipmentTypeController {

	private static final String ENTITY = "equipment_type";

	@Autowired
	private final EquipmentTypeService typeService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<EquipmentType> types = typeService.getAll();

		//TODO: MAYBE REMOVE
		types.sort(Comparator.comparing(EquipmentType::getId));

		model.addAttribute("equipment_types", types);

		return "vehicle/equipment_type/equipment_type_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		EquipmentType type = typeService.getById(id);

		if (type == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such element");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, type);

		return "vehicle/equipment_type/equipment_type_details_page";
	}


	// NEW EQUIPMENT FORM
	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new EquipmentType());

		return "vehicle/equipment_type/new_equipment_type_page";
	}


	// EDIT EQUIPMENT FORM
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		EquipmentType type = typeService.getById(id);

		if (type == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, type);

		return "vehicle/equipment_type/edit_equipment_type_page";
	}


	// POST EQUIPMENT TYPE
	@PostMapping({"", "/"})
	public String post(@ModelAttribute EquipmentType type, Model model) {
		ValidationResponse response = typeService.validate(type, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		EquipmentType typeFromDatabase = typeService.save(type);

		if (typeFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/equipment_types";
		}
	}


	// UPDATE EQUIPMENT TYPE
	@PostMapping("/update")
	public String put(@ModelAttribute EquipmentType type, Model model) {

		ValidationResponse response = typeService.validate(type, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		EquipmentType typeFromDatabase = typeService.save(type);

		if (typeFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/equipment_types/" + typeFromDatabase.getId();
		}
	}


	// DELETE EQUIPMENT
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		EquipmentType typeFromDatabase = typeService.getById(id);

		if (typeFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		typeService.delete(typeFromDatabase);

		return StringUtils.REDIRECT + "/equipment_types";
	}
}
