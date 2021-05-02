package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.EquipmentService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/equipment")
public class EquipmentRestController {

	private final String ENTITY = "equipment";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final EquipmentService equipmentService;

	@Autowired
	private ObjectMapper objectMapper;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Equipment>>> postList(@RequestBody List<Equipment> equipmentList) {

		if (equipmentList == null || equipmentList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			RestResponse<Equipment> restResponse = new RestResponse<>();
			restResponse.setBody(equipment);
			
			ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				Equipment equipmentFromDatabase = equipmentService.save(equipment);

				if (equipmentFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(equipmentFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("create " + ENTITY, ENTITY + " created:\n" + equipmentFromDatabase);
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
	public ResponseEntity<RestResponse<Equipment>> post(@RequestBody Equipment equipment) {

		RestResponse<Equipment> restResponse = new RestResponse<>();
		restResponse.setBody(equipment);
		
		ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("create " + ENTITY, ENTITY + " created:\n" + equipmentFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody Equipment equipment, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Equipment> getAll() {
		return equipmentService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Equipment getByID(@PathVariable Long id) {
		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return equipmentFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Equipment>>> putList(@RequestBody List<Equipment> equipmentList) {

		if (equipmentList == null || equipmentList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			RestResponse<Equipment> restResponse = new RestResponse<>();
			restResponse.setBody(equipment);
			
			ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldEquipmentFromDatabase = equipmentService.getById(equipment.getId()).toString();
				Equipment equipmentFromDatabase = equipmentService.save(equipment);

				if (equipmentFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(equipmentFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentFromDatabase + "\nto:\n" + equipmentFromDatabase);
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
	public ResponseEntity<RestResponse<Equipment>> putById(@RequestBody Equipment equipment, @PathVariable Long id) {

		RestResponse<Equipment> restResponse = new RestResponse<>();
		restResponse.setBody(equipment);
		
		ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldEquipmentFromDatabase = equipmentService.getById(equipment.getId()).toString();
		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentFromDatabase + "\nto:\n" + equipmentFromDatabase);

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
				mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
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
					long idLong = (long) ((Integer) idObj);
					changes.remove("id");

					String oldEquipmentFromDatabase = equipmentService.getById(idLong).toString();
					Equipment equipmentFromDatabase;

					try {
						equipmentFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<Equipment> restResponse = new RestResponse<>();
					restResponse.setBody(equipmentFromDatabase);
					
					ValidationResponse response = equipmentService.validate(equipmentFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						Equipment updatedEquipmentFromDatabase = equipmentService.save(equipmentFromDatabase);

						if (updatedEquipmentFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedEquipmentFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentFromDatabase + "\nto:\n" + updatedEquipmentFromDatabase);
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
	public ResponseEntity<RestResponse<Equipment>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldEquipmentFromDatabase = equipmentFromDatabase.toString();

		changes.remove("id");

		RestResponse<Equipment> restResponse = new RestResponse<>();

		try {
			equipmentFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(equipmentFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(equipmentFromDatabase);
		
		ValidationResponse response = equipmentService.validate(equipmentFromDatabase, Mapping.PATCH);
		
		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Equipment patchedEquipment = equipmentService.save(equipmentFromDatabase);
		restResponse.setBody(patchedEquipment);

		if (patchedEquipment == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentFromDatabase + "\nto:\n" + patchedEquipment);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Equipment>>> deleteList(@RequestBody List<Equipment> equipmentList) {

		if (equipmentList == null || equipmentList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			RestResponse<Equipment> restResponse = new RestResponse<>();
			restResponse.setBody(equipment);
			
			ValidationResponse response = equipmentService.validate(equipment, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					equipmentService.delete(equipment);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					addLog("delete " + ENTITY, ENTITY + " deleted:\n" + equipment);
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
	public ResponseEntity<RestResponse<Equipment>> deleteById(@PathVariable Long id) {
		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = equipmentService.validate(equipmentFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			equipmentService.delete(equipmentFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<Equipment> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		addLog("delete " + ENTITY, ENTITY + " deleted:\n" + equipmentFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForEquipment()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}


	private Equipment handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		Equipment entity = equipmentService.getById(id);

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
									localDateTime = DateUtils.stringToLocalDateTime((String) value);
								} catch (Exception e) {
									throw new JsonParseException(new Throwable(e.getMessage()));
								}

								ReflectionUtils.setField(field, entity, localDateTime);
							}

							if (field.getType().equals(Vehicle.class)) {
								try {
									Vehicle vehicle = objectMapper.readValue((String) value, Vehicle.class);
									entity.setVehicle(vehicle);
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Vehicle json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(EquipmentType.class)) {
								try {
									EquipmentType equipmentType = objectMapper.readValue((String) value, EquipmentType.class);
									entity.setEquipment_type(equipmentType);
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("EquipmentType json parsing error: " + e.getMessage()));
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
