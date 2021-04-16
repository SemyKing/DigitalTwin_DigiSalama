package com.example.demo.api.controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.utils.FieldReflectionUtils;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("organisation")
@RequestMapping(StringUtils.UI_API + "/organisations")
public class OrganisationController {

	private final String ENTITY = "organisation";

	@Autowired
	private final OrganisationService organisationService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "organisation/organisations_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, organisationFromDatabase);

		return "organisation/organisation_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute(ENTITY, new Organisation());
		return "organisation/new_organisation_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, organisationFromDatabase);

		return "organisation/edit_organisation_page";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute Organisation organisation, Model model) {
		organisation = new FieldReflectionUtils<Organisation>().getObjectWithEmptyStringValuesAsNull(organisation);

		ValidationResponse response = organisationService.validate(organisation, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/organisations";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute Organisation organisation, Model model) {
		organisation = new FieldReflectionUtils<Organisation>().getObjectWithEmptyStringValuesAsNull(organisation);

		ValidationResponse response = organisationService.validate(organisation, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/organisations/" + organisationFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		organisationService.delete(organisationFromDatabase);

		return StringUtils.REDIRECT + StringUtils.UI_API + "/organisations";
	}
}
