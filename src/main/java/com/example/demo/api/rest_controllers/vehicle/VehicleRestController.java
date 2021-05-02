package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/vehicles")
public class VehicleRestController {

	private final String ENTITY = "vehicle";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private ObjectMapper objectMapper;



	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Vehicle>>> postList(@RequestBody List<Vehicle> vehicles) {

		if (vehicles == null || vehicles.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			RestResponse<Vehicle> restResponse = new RestResponse<>();
			restResponse.setBody(vehicle);
			
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				if (vehicleFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(vehicleFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("create " + ENTITY, ENTITY + " created:\n" + vehicleFromDatabase);
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
	public ResponseEntity<RestResponse<Vehicle>> post(@RequestBody Vehicle vehicle) {
		RestResponse<Vehicle> restResponse = new RestResponse<>();
		restResponse.setBody(vehicle);
		
		ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(vehicleFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("create " + ENTITY, ENTITY + " created:\n" + vehicleFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postByID(@RequestBody Vehicle vehicle, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Vehicle> getAll() {
		return vehicleService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Vehicle getByID(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return vehicleFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Vehicle>>> putList(@RequestBody List<Vehicle> vehicles) {

		if (vehicles == null || vehicles.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			RestResponse<Vehicle> restResponse = new RestResponse<>();
			restResponse.setBody(vehicle);
			
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldVehicleFromDatabase = vehicleService.getById(vehicle.getId()).toString();
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				if (vehicleFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldVehicleFromDatabase + "\nto:\n" + vehicleFromDatabase);
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
	public ResponseEntity<RestResponse<Vehicle>> putById(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		RestResponse<Vehicle> restResponse = new RestResponse<>();
		restResponse.setBody(vehicle);
		
		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldVehicleFromDatabase = vehicleService.getById(vehicle.getId()).toString();
		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(vehicleFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldVehicleFromDatabase + "\nto:\n" + vehicleFromDatabase);

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

				errorOccurred = true;
			} else {
				if (!changes.containsKey("id")) {
					mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
					mapResponse.setMessage("ID parameter is required");

					errorOccurred = true;
				} else {
					Object idObj = changes.get("id");

					if (!(idObj instanceof Integer)) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage("ID parameter is invalid");

						errorOccurred = true;
					} else {
						long idLong = (long) ((Integer) idObj);
						changes.remove("id");

						String oldVehicleFromDatabase = vehicleService.getById(idLong).toString();

						Vehicle vehicleFromDatabase;

						try {
							vehicleFromDatabase = handlePatchChanges(idLong, changes);
						} catch (JsonParseException jsonParseException) {
							mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
							mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
							responseList.add(mapResponse);
							continue;
						}

						RestResponse<Vehicle> restResponse = new RestResponse<>();
						restResponse.setBody(vehicleFromDatabase);

						ValidationResponse response = vehicleService.validate(vehicleFromDatabase, Mapping.PATCH);

						if (!response.isValid()) {
							restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
							restResponse.setMessage(response.getMessage());

							errorOccurred = true;
						} else {
							Vehicle updatedVehicleFromDatabase = vehicleService.save(vehicleFromDatabase);

							if (updatedVehicleFromDatabase == null) {
								restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
								restResponse.setMessage("failed to save " + ENTITY + " in database");

								errorOccurred = true;
							} else {
								restResponse.setBody(updatedVehicleFromDatabase);
								restResponse.setHttp_status(HttpStatus.OK);
								restResponse.setMessage(ENTITY + " patched successfully");

								addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldVehicleFromDatabase + "\nto:\n" + updatedVehicleFromDatabase);
							}
						}

						responseList.add(restResponse);
					}
				}
			}

			responseList.add(mapResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Vehicle>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldVehicleFromDatabase = vehicleFromDatabase.toString();

		changes.remove("id");

		RestResponse<Vehicle> restResponse = new RestResponse<>();

		try {
			vehicleFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(vehicleFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(vehicleFromDatabase);

		ValidationResponse response = vehicleService.validate(vehicleFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Vehicle patchedVehicle = vehicleService.save(vehicleFromDatabase);
		restResponse.setBody(patchedVehicle);

		if (patchedVehicle == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " patched successfully");

			addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldVehicleFromDatabase + "\nto:\n" + patchedVehicle);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Vehicle>>> deleteList(@RequestBody List<Vehicle> vehicles) {

		if (vehicles == null || vehicles.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			RestResponse<Vehicle> restResponse = new RestResponse<>();
			restResponse.setBody(vehicle);
			
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					vehicleService.delete(vehicle);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					addLog("delete " + ENTITY, ENTITY + " deleted:\n" + vehicle);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database " + e.getMessage());

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
	public ResponseEntity<RestResponse<Vehicle>> deleteById(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = vehicleService.validate(vehicleFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.getMessage());
		}

		try {
			vehicleService.delete(vehicleFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database " + e.getMessage());
		}

		RestResponse<Vehicle> restResponse = new RestResponse<>();
		restResponse.setBody(vehicleFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		addLog("delete " + ENTITY, ENTITY + " deleted:\n" + vehicleFromDatabase.toString());

		return ResponseEntity.ok(restResponse);
	}


	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForVehicles()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}

	private Vehicle handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		Vehicle entity = vehicleService.getById(id);

		if (entity != null) {
				changes.forEach((key, value) -> {
				Field field = ReflectionUtils.findField(entity.getClass(), key);

				if (field != null) {
					field.setAccessible(true);

					String json = value == null ? null : value.toString();

					if (json == null) {
						ReflectionUtils.setField(field, entity, null);
					} else {
						if (field.getType().equals(String.class)) {
							ReflectionUtils.setField(field, entity, json);
						} else {

							if (field.getType().equals(LocalDateTime.class)) {
								LocalDateTime localDateTime = null;

								try {
									localDateTime = DateUtils.stringToLocalDateTime(json);
								} catch (Exception e) {
									throw new JsonParseException(new Throwable(e.getMessage()));
								}

								ReflectionUtils.setField(field, entity, localDateTime);
							}

							if (field.getType().equals(Organisation.class)) {
								try {
									objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
									entity.setOrganisation(objectMapper.readValue(json, Organisation.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Organisation json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(Boolean.class)) {
								try {
									ReflectionUtils.setField(field, entity, Boolean.valueOf(json));
								} catch (Exception e) {
									throw new JsonParseException(new Throwable("Boolean json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(Set.class) && field.getName().equals("fleets")) {
								try {
									Set<Fleet> fleetsFromPatch = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(HashSet.class, Fleet.class));

									// ADD FLEET TO VEHICLE IF ALREADY NOT THERE
									fleetsFromPatch.forEach(fleet -> entity.getFleets().add(fleet));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Fleets Set: '" + value + "' json parsing error: " + e.getMessage()));
								}
							}

						}
					}
				}
			});
		}

		return entity;
	}
}