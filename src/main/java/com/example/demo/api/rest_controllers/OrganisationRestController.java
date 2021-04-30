package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/organisations")
public class OrganisationRestController {

	private final String ENTITY = "organisation";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final OrganisationService organisationService;



	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Organisation>>> postList(@RequestBody List<Organisation> organisations) {

		if (organisations == null || organisations.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			RestResponse<Organisation> restResponse = new RestResponse<>();
			restResponse.setBody(organisation);
			
			ValidationResponse response = organisationService.validate(organisation, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				Organisation organisationFromDatabase = organisationService.save(organisation);

				if (organisationFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(organisationFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("create " + ENTITY, ENTITY + " created:\n" + organisationFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> post(@RequestBody Organisation organisation) {

		RestResponse<Organisation> restResponse = new RestResponse<>();
		restResponse.setBody(organisation);
		
		ValidationResponse response = organisationService.validate(organisation, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(organisationFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("create " + ENTITY, ENTITY + " created:\n" + organisationFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postById(@RequestBody Organisation organisation, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Organisation> getAll() {
		return organisationService.getAll();
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

		if (organisations == null || organisations.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			RestResponse<Organisation> restResponse = new RestResponse<>();
			restResponse.setBody(organisation);
			
			ValidationResponse response = organisationService.validate(organisation, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldOrganisationFromDatabase = organisationService.getById(organisation.getId()).toString();
				Organisation organisationFromDatabase = organisationService.save(organisation);

				if (organisationFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(organisationFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldOrganisationFromDatabase + "\nto:\n" + organisationFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> putById(@RequestBody Organisation organisation, @PathVariable Long id) {
		RestResponse<Organisation> restResponse = new RestResponse<>();
		restResponse.setBody(organisation);
		
		ValidationResponse response = organisationService.validate(organisation, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldOrganisationFromDatabase = organisationService.getById(organisation.getId()).toString();
		Organisation organisationFromDatabase = organisationService.save(organisation);

		if (organisationFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(organisationFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldOrganisationFromDatabase + "\nto:\n" + organisationFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		if (changesList == null || changesList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (changes == null) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("NULL array element was provided");
				responseList.add(mapResponse);
				errorOccurred = true;
				continue;
			}

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("ID parameter is required");

				responseList.add(mapResponse);

				errorOccurred = true;
			} else {
				Object idObj = changes.get("id");

				if (!(idObj instanceof Integer)) {
					mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
					mapResponse.setMessage("ID parameter is invalid");

					responseList.add(mapResponse);

					errorOccurred = true;
				} else {
					changes.remove("id");
					long idLong = (long) ((Integer) idObj);

					String oldOrganisationFromDatabase = organisationService.getById(idLong).toString();
					Organisation organisationFromDatabase;

					try {
						organisationFromDatabase = handlePatchChanges(idLong, changes);
					} catch (Exception e) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(e.getMessage());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<Organisation> restResponse = new RestResponse<>();
					restResponse.setBody(organisationFromDatabase);
					
					ValidationResponse response = organisationService.validate(organisationFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						Organisation updatedOrganisationFromDatabase = organisationService.save(organisationFromDatabase);

						if (updatedOrganisationFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedOrganisationFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldOrganisationFromDatabase + "\nto:\n" + updatedOrganisationFromDatabase);
						}
					}

					responseList.add(restResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldOrganisation = organisationFromDatabase.toString();

		changes.remove("id");

		RestResponse<Organisation> restResponse = new RestResponse<>();

		try {
			organisationFromDatabase = handlePatchChanges(id, changes);
		} catch (Exception e) {
			restResponse.setBody(organisationFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(e.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(organisationFromDatabase);
		
		ValidationResponse response = organisationService.validate(organisationFromDatabase, Mapping.PATCH);
		
		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Organisation patchedOrganisation = organisationService.save(organisationFromDatabase);
		restResponse.setBody(patchedOrganisation);

		if (patchedOrganisation == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldOrganisation + "\nto:\n" + patchedOrganisation);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Organisation>>> deleteList(@RequestBody List<Organisation> organisations) {

		if (organisations == null || organisations.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Organisation>> responseList = new ArrayList<>();

		for (Organisation organisation : organisations) {
			RestResponse<Organisation> restResponse = new RestResponse<>();
			restResponse.setBody(organisation);
			
			ValidationResponse response = organisationService.validate(organisation, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					organisationService.delete(organisation);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					addLog("delete " + ENTITY, ENTITY + " deleted:\n" + organisation);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());

					errorOccurred = true;
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Organisation>> deleteById(@PathVariable Long id) {
		Organisation organisationFromDatabase = organisationService.getById(id);

		if (organisationFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = organisationService.validate(organisationFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			organisationService.delete(organisationFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<Organisation> restResponse = new RestResponse<>();
		restResponse.setBody(organisationFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		addLog("delete " + ENTITY, ENTITY + " deleted:\n" + organisationFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForOrganisations()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}

	private Organisation  handlePatchChanges(Long id, Map<String, Object> changes) throws Exception {
		Organisation entity = organisationService.getById(id);

		if (entity != null) {
			changes.forEach((key, value) -> {
				Field field = ReflectionUtils.findField(entity.getClass(), key);

				if (field != null) {
					field.setAccessible(true);

					if (field.getType().equals(String.class)) {
						ReflectionUtils.setField(field, entity, value);
					} else {
						if (field.getType().equals(LocalDateTime.class)) {
							LocalDateTime localDateTime = null;

							try {
								localDateTime = DateUtils.stringToLocalDateTime((String) value);
							} catch (Exception e) {
								throw new StringIndexOutOfBoundsException(e.getMessage());
							}

							ReflectionUtils.setField(field, entity, localDateTime);
						}
					}
				}
			});
		}

		return entity;
	}
}