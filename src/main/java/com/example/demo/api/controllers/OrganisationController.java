package com.example.demo.api.controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrganisationController {

	private final String ENTITY =       "organisation";
	private final String ENTITY_LIST =  "organisations";

	private final String GET_ALL_URL =      StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String GET_BY_ID_URL =    StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String NEW_ENTITY_URL =   StringUtils.UI_API + StringUtils.NEW +      StringUtils.FORWARD_SLASH + ENTITY;
	private final String SAVE_URL =         StringUtils.UI_API + StringUtils.SAVE +     StringUtils.FORWARD_SLASH + ENTITY;
	private final String EDIT_URL =         StringUtils.UI_API + StringUtils.EDIT +     StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String UPDATE_URL =       StringUtils.UI_API + StringUtils.UPDATE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String DELETE_URL =       StringUtils.UI_API + StringUtils.DELETE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;

	private final String ENTITY_LIST_PAGE =     "organisation/organisations_list_page";
	private final String ENTITY_DETAILS_PAGE =  "organisation/organisation_details_page";
	private final String EDIT_ENTITY_PAGE =     "organisation/edit_organisation_page";
	private final String NEW_ENTITY_PAGE =      "organisation/new_organisation_page";


	@Autowired
	private final OrganisationService service;

	@GetMapping(GET_ALL_URL)
	public String getAllOrganisations(Model model) {
		List<Organisation> organisations = service.getAll();
		organisations.sort(Comparator.comparing(Organisation::getId));

		model.addAttribute(ENTITY_LIST, organisations);
		return ENTITY_LIST_PAGE;
	}


	@GetMapping(GET_BY_ID_URL)
	public String getOrganisationById(@PathVariable Long id, Model model) {
		Organisation organisation = service.getById(id);

		if (organisation == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, organisation);
		return ENTITY_DETAILS_PAGE;
	}


	@GetMapping(NEW_ENTITY_URL)
	public String newOrganisationForm(Model model) {
		model.addAttribute(ENTITY, new Organisation());
		return NEW_ENTITY_PAGE;
	}


	@PostMapping(SAVE_URL)
	public String saveOrganisationFromUI(@ModelAttribute Organisation organisation, Model model) {
		if (organisation == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, ENTITY + " object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " object is required");
			return StringUtils.ERROR_PAGE;
		}

		service.save(organisation);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@GetMapping(EDIT_URL)
	public String editOrganisationForm(@PathVariable Long id, Model model) {

		Organisation organisation = service.getById(id);

		if (organisation == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Vehicle with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, organisation);

		return EDIT_ENTITY_PAGE;
	}


	@PostMapping(UPDATE_URL)
	public String updateOrganisationFromUI(@ModelAttribute Organisation organisation, @PathVariable Long id, Model model) {

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "ID missing");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Vehicle with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		service.save(organisation);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@PostMapping(DELETE_URL)
	public String deleteOrganisationFromUI(@PathVariable Long id, Model model) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Vehicle with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		service.delete(organisationFromDatabase);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}
}
