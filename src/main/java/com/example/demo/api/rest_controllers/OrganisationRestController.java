package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
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

	private final String ENTITY_URL =  			StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String ENTITY_URL_WITH_ID =	StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID;


	@Autowired
	private final OrganisationService service;


	@PostMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Organisation> postEntity(@RequestBody Organisation organisation) {
		System.out.println(organisation);

		// VALIDATE ENTITY



		Organisation organisationFromDatabase = service.save(organisation);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to save " + ENTITY + " in database");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(organisationFromDatabase);
		}
	}

	@PostMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public void postEntityWithID(@RequestBody Organisation organisation, @PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "resource already exists");
		}
	}



	@GetMapping(value = ENTITY_URL, produces = StringUtils.APPLICATION_JSON)
	public List<Organisation> getEntityList() {
		List<Organisation> organisations = service.getAll();

		// SORT etc...
		organisations.sort(Comparator.comparing(Organisation::getId));

		return organisations;
	}

	@GetMapping(value = ENTITY_URL_WITH_ID, produces = StringUtils.APPLICATION_JSON)
	public Organisation getEntityWithID(@PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return organisationFromDatabase;
	}



	@PutMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public Organisation updateReplaceEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PutMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		organisation.setId(organisationFromDatabase.getId());

		service.save(organisation);

		return ResponseEntity.status(HttpStatus.OK).body(organisation);
	}



	@PatchMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateModifyEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PatchMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Organisation> updateModifyEntity(@RequestBody Organisation organisation, @PathVariable Long id) {

		// VALIDATE ENTITY
		System.out.println(organisation);

		// EMPTY BODY
		if (organisation == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No content");
		}

		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		organisation.setId(organisationFromDatabase.getId());

		service.save(organisation);

		return ResponseEntity.status(HttpStatus.OK).body(organisation);
	}



	@DeleteMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<Organisation> organisations) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@DeleteMapping(value = ENTITY_URL_WITH_ID)
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		Organisation organisationFromDatabase = service.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			service.delete(organisationFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

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
