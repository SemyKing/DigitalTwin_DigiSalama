package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.database.services.vehicle.RefuelService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.FieldReflectionUtils;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"refuel"})
@RequestMapping(StringUtils.UI_API + "/refuels")
public class RefuelController {

	private final String ENTITY = "refuel";

	@Autowired
	private final RefuelService refuelService;

	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final FileService fileService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Refuel> refuels = refuelService.getAll();
		model.addAttribute("refuels", refuels);

		return "vehicle/refuels/refuels_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, refuelFromDatabase);

		return "vehicle/refuels/refuel_details_page";
	}


	@GetMapping("/{id}/files")
	public String getFilesByRefuelId(@PathVariable Long id, Model model) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, refuelFromDatabase);


		List<FileDB> files = fileService.getAllByRefuelId(id);
		model.addAttribute("files", files);

		return "vehicle/refuels/refuel_files_list_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Refuel());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<FileDB> files = fileService.getAll();
		model.addAttribute("files", files);

		return "vehicle/refuels/new_refuel_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, refuelFromDatabase);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		List<FileDB> files = fileService.getAll();
		model.addAttribute("files", files);

		return "vehicle/refuels/edit_refuel_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Refuel refuel, Model model) {
		refuel = new FieldReflectionUtils<Refuel>().getObjectWithEmptyStringValuesAsNull(refuel);

		ValidationResponse response = refuelService.validate(refuel, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(ENTITY, refuel);
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			List<Vehicle> vehicles = vehicleService.getAll();
			model.addAttribute("vehicles", vehicles);

			List<FileDB> files = fileService.getAll();
			model.addAttribute("files", files);

			return "vehicle/refuels/new_refuel_page";
		}

		Refuel refuelFromDatabase = refuelService.save(refuel);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/refuels";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Refuel refuel, Model model, HttpServletRequest request) {
		refuel = new FieldReflectionUtils<Refuel>().getObjectWithEmptyStringValuesAsNull(refuel);

		ValidationResponse response = refuelService.validate(refuel, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			String referer = request.getHeader("Referer");

			if (referer.contains("/edit")) {
				model.addAttribute(ENTITY, refuel);

				List<Vehicle> vehicles = vehicleService.getAll();
				model.addAttribute("vehicles", vehicles);

				List<FileDB> files = fileService.getAll();
				model.addAttribute("files", files);

				return "vehicle/refuels/edit_refuel_page";
			}

			return StringUtils.ERROR_PAGE;
		}

		Refuel refuelFromDatabase = refuelService.save(refuel);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/refuels/" + refuelFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		refuelService.delete(refuelFromDatabase);

		return StringUtils.REDIRECT + StringUtils.UI_API + "/refuels";
	}
}
