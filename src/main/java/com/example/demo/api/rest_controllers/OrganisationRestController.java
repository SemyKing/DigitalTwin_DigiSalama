package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.database.models.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(StringUtils.JSON_API + "/organisations")
public class OrganisationRestController {

	private final String ENTITY = "organisation";

	@Autowired
	private final OrganisationService organisationService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Organisation>>> postList(@RequestBody List<Organisation> organisations) {

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			ValidationResponse response = organisationService.validate(organisation, Mapping.POST);

			RestResponse<Organisation> responseHandler = new RestResponse<>();
			responseHandler.setBody(organisation);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Organisation organisationFromDatabase = organisationService.save(organisation);

				if (organisationFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(organisationFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}


	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> post(@RequestBody Organisation organisation) {

		ValidationResponse response = organisationService.validate(organisation, Mapping.POST);

		RestResponse<Organisation> responseHandler = new RestResponse<>();
		responseHandler.setBody(organisation);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(organisationFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postWithId(@RequestBody Organisation organisation, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Organisation> getAll() {
		List<Organisation> organisations = organisationService.getAll();

		// TODO: MAYBE REMOVE
		organisations.sort(Comparator.comparing(Organisation::getId));

		return organisations;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Organisation getById(@PathVariable Long id) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return organisationFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Organisation>>> putList(@RequestBody List<Organisation> organisations) {

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			ValidationResponse response = organisationService.validate(organisation, Mapping.PUT);

			RestResponse<Organisation> responseHandler = new RestResponse<>();
			responseHandler.setBody(organisation);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Organisation organisationFromDatabase = organisationService.save(organisation);

				if (organisationFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(organisationFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> putById(@RequestBody Organisation organisation, @PathVariable Long id) {

		ValidationResponse response = organisationService.validate(organisation, Mapping.PUT);

		RestResponse<Organisation> responseHandler = new RestResponse<>();
		responseHandler.setBody(organisation);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(organisationFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			changes.remove("password");

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
				mapResponse.setMessage("ID parameter is required");

				responseList.add(mapResponse);

				errorOccurred = true;
			} else {
				Object idObj = changes.get("id");

				if (!(idObj instanceof Integer)) {
					mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
					mapResponse.setMessage("ID parameter is invalid");

					responseList.add(mapResponse);

					errorOccurred = true;
				} else {
					Integer id = (Integer) idObj;

					Organisation organisationFromDatabase = organisationService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(Organisation.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, organisationFromDatabase, value);
						}
					});

					ValidationResponse response = organisationService.validate(organisationFromDatabase, Mapping.PATCH);

					RestResponse<Organisation> userResponse = new RestResponse<>();
					userResponse.setBody(organisationFromDatabase);

					if (!response.isValid()) {
						userResponse.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
						userResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {
						userResponse.setHttp_status(HttpStatus.OK);
						userResponse.setMessage(ENTITY + "patched successfully");
					}

					responseList.add(userResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(Organisation.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, organisationFromDatabase, value);
			}
		});

		RestResponse<Organisation> responseHandler = new RestResponse<>();

		ValidationResponse response = organisationService.validate(organisationFromDatabase, Mapping.PATCH);
		responseHandler.setBody(organisationFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Organisation patchedOrganisation = organisationService.save(organisationFromDatabase);
		responseHandler.setBody(patchedOrganisation);

		if (patchedOrganisation == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Organisation>>> deleteList(@RequestBody List<Organisation> organisations) {

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			ValidationResponse response = organisationService.validate(organisation, Mapping.DELETE);

			RestResponse<Organisation> responseHandler = new RestResponse<>();
			responseHandler.setBody(organisation);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					organisationService.delete(organisation);

					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " deleted successfully");
				} catch (Exception e) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> deleteById(@PathVariable Long id) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		RestResponse<Organisation> responseHandler = new RestResponse<>();


		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			organisationService.delete(organisationFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		// TODO: MAYBE REMOVE
		responseHandler.setBody(organisationFromDatabase);

		responseHandler.setHttp_status(HttpStatus.OK);
		responseHandler.setMessage(ENTITY + "successfully deleted");

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			Organisation organisation = new Organisation();
			organisation.setName("Organisation_" + i);
			organisationService.save(organisation);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		organisationService.deleteAll();
	}
}
