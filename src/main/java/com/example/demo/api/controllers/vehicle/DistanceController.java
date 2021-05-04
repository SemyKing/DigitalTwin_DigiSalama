package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Distance;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.DistanceService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"distance"})
@RequestMapping(Constants.UI_API + "/distances")
public class DistanceController {

	private final String ENTITY = "distance";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final DistanceService distanceService;

	@Autowired
	private final VehicleService vehicleService;



	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Distance> distances = distanceService.getAll();
		distances.sort(Comparator.comparing(Distance::getTimestamp).reversed());
		model.addAttribute("distances", distances);

		return "vehicle/distance/distances_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, distanceFromDatabase);

		return "vehicle/distance/distance_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Distance());

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/distance/new_distance_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, distanceFromDatabase);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/distance/edit_distance_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Distance distance, Model model) {
		distance = new FieldReflectionUtils<Distance>().getEntityWithEmptyStringValuesAsNull(distance);

		ValidationResponse response = distanceService.validate(distance, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(ENTITY, distance);
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			List<Vehicle> vehicles = vehicleService.getAll();
			model.addAttribute("vehicles", vehicles);

			return "vehicle/distance/new_distance_page";
		}

		Distance distanceFromDatabase = distanceService.save(distance);

		if (distanceFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addDistanceLog("create " + ENTITY, ENTITY + " created:\n" + distanceFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/distances";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Distance distance, Model model, HttpServletRequest request) {
		String oldDistanceFromDatabase = distanceService.getById(distance.getId()).toString();

		distance = new FieldReflectionUtils<Distance>().getEntityWithEmptyStringValuesAsNull(distance);

		ValidationResponse response = distanceService.validate(distance, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());

			String referer = request.getHeader("Referer");

			if (referer.contains("/edit")) {
				model.addAttribute(ENTITY, distance);

				List<Vehicle> vehicles = vehicleService.getAll();
				model.addAttribute("vehicles", vehicles);

				return "vehicle/distance/edit_distance_page";
			}

			return Constants.ERROR_PAGE;
		}
		Distance distanceFromDatabase = distanceService.save(distance);

		if (distanceFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addDistanceLog("update " + ENTITY, ENTITY + " updated from:\n" + oldDistanceFromDatabase + "\nto:\n" + distanceFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/distances/" + distanceFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		distanceService.delete(distanceFromDatabase);

		eventHistoryLogService.addDistanceLog("delete " + ENTITY, ENTITY + " deleted:\n" + distanceFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/distances";
	}
}
