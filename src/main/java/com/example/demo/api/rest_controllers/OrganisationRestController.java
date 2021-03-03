package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class OrganisationRestController {

	private final String ENTITY =       "organisation";
	private final String ENTITY_LIST =  "organisations";

	// CREATE
	private final String POST_ENTITY_URL =  StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// GET
	private final String GET_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/REPLACE
	private final String PUT_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/MODIFY
	private final String PATCH_ENTITY_URL = StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// DELETE
	private final String DELETE_ENTITY_URL =StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;


	@Autowired
	private final OrganisationService service;


	@PostMapping(value = POST_ENTITY_URL, consumes = "application/json")
	public ResponseEntity<Organisation> postEntity(@RequestBody Organisation organisation) {
		System.out.println(organisation);

		// VALIDATE ENTITY

		Organisation organisationFromDatabase = service.save(organisation);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to save entity");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(organisationFromDatabase);
		}
	}

	@PostMapping(value = POST_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public void postEntityWithID(@RequestBody Organisation organisation, @PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}
	}



	@GetMapping(value = GET_ENTITY_URL, produces = "application/json")
	public List<Organisation> getEntityList() {
		List<Organisation> organisations = service.getAll();

		// SORT etc...
		organisations.sort(Comparator.comparing(Organisation::getId));

		return organisations;
	}

	@GetMapping(value = GET_ENTITY_URL + StringUtils.ID, produces = "application/json")
	public Organisation getEntityWithID(@PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		return organisationFromDatabase;
	}



	@PutMapping(value = PUT_ENTITY_URL, consumes = "application/json")
	public Organisation updateReplaceEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
	}

	@PutMapping(value = PUT_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<Organisation> updateReplaceEntity(@RequestBody Organisation organisation, @PathVariable Long id) {
		System.out.println("PUT ENTITY");

		// VALIDATE ENTITY
		System.out.println(organisation);

		// EMPTY BODY
		if (organisation == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		}

		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		organisation.setId(organisationFromDatabase.getId());

		service.save(organisation);

		return ResponseEntity.status(HttpStatus.OK).body(organisation);
	}



	@PatchMapping(value = PATCH_ENTITY_URL, consumes = "application/json")
	public void updateModifyEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = PATCH_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<Organisation> updateModifyEntity(@RequestBody Organisation organisation, @PathVariable Long id) {

		// VALIDATE ENTITY
		System.out.println(organisation);

		// EMPTY BODY
		if (organisation == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No content");
		}

		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		organisation.setId(organisationFromDatabase.getId());

		service.save(organisation);

		return ResponseEntity.status(HttpStatus.OK).body(organisation);
	}



	@DeleteMapping(value = DELETE_ENTITY_URL, consumes = "application/json")
	public void deleteEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@DeleteMapping(value = DELETE_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		service.delete(organisationFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).build();
	}



	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			Organisation organisation = new Organisation();
			organisation.setName("Organisation_" + i);
			service.save(organisation);
		}
	}

	// FOR TESTING
	@DeleteMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		service.deleteAll();
	}
}
