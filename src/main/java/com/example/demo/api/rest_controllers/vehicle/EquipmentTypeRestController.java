package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
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
@RequestMapping(Constants.JSON_API + "/equipment_type")
public class EquipmentTypeRestController {

	private final String ENTITY = "equipment_type";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final EquipmentTypeService equipmentTypeService;



	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> postList(@RequestBody List<EquipmentType> equipmentTypes) {

		if (equipmentTypes == null || equipmentTypes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

				if (equipmentTypeFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(equipmentTypeFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("create " + ENTITY, ENTITY + " created:\n" + equipmentTypeFromDatabase);
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
	public ResponseEntity<RestResponse<EquipmentType>> post(@RequestBody EquipmentType equipmentType) {

		RestResponse<EquipmentType> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentType);
		
		ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

		if (equipmentTypeFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentTypeFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("create " + ENTITY, ENTITY + " created:\n" + equipmentTypeFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody EquipmentType equipment, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<EquipmentType> getAll() {
		return equipmentTypeService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EquipmentType getByID(@PathVariable Long id) {
		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.getById(id);

		if (equipmentTypeFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return equipmentTypeFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> putList(@RequestBody List<EquipmentType> equipmentTypes) {

		if (equipmentTypes == null || equipmentTypes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldEquipmentTypeFromDatabase = equipmentTypeService.getById(equipmentType.getId()).toString();
				EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

				if (equipmentTypeFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(equipmentTypeFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentTypeFromDatabase + "\nto:\n" + equipmentTypeFromDatabase);
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
	public ResponseEntity<RestResponse<EquipmentType>> putById(@RequestBody EquipmentType equipmentType, @PathVariable Long id) {

		RestResponse<EquipmentType> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentType);
		
		ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldEquipmentTypeFromDatabase = equipmentTypeService.getById(equipmentType.getId()).toString();
		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

		if (equipmentTypeFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentTypeFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentTypeFromDatabase + "\nto:\n" + equipmentTypeFromDatabase);

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

					String oldEquipmentTypeFromDatabase = equipmentTypeService.getById(idLong).toString();
					EquipmentType equipmentTypeFromDatabase;

					try {
						equipmentTypeFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<EquipmentType> restResponse = new RestResponse<>();
					restResponse.setBody(equipmentTypeFromDatabase);
					
					ValidationResponse response = equipmentTypeService.validate(equipmentTypeFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						EquipmentType updatedEquipmentTypeFromDatabase = equipmentTypeService.save(equipmentTypeFromDatabase);

						if (updatedEquipmentTypeFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedEquipmentTypeFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentTypeFromDatabase + "\nto:\n" + updatedEquipmentTypeFromDatabase);
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
	public ResponseEntity<RestResponse<EquipmentType>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.getById(id);

		if (equipmentTypeFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldEquipmentTypeFromDatabase = equipmentTypeFromDatabase.toString();

		changes.remove("id");

		RestResponse<EquipmentType> restResponse = new RestResponse<>();

		try {
			equipmentTypeFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(equipmentTypeFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(equipmentTypeFromDatabase);
		
		ValidationResponse response = equipmentTypeService.validate(equipmentTypeFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		EquipmentType patchedEquipmentType = equipmentTypeService.save(equipmentTypeFromDatabase);
		restResponse.setBody(patchedEquipmentType);

		if (patchedEquipmentType == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEquipmentTypeFromDatabase + "\nto:\n" + patchedEquipmentType);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> deleteList(@RequestBody List<EquipmentType> equipmentTypes) {

		if (equipmentTypes == null || equipmentTypes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					equipmentTypeService.delete(equipmentType);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					addLog("delete " + ENTITY, ENTITY + " deleted:\n" + equipmentType);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());
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
	public ResponseEntity<RestResponse<EquipmentType>> deleteById(@PathVariable Long id) {
		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.getById(id);

		if (equipmentTypeFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = equipmentTypeService.validate(equipmentTypeFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			equipmentTypeService.delete(equipmentTypeFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<EquipmentType> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentTypeFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		addLog("delete " + ENTITY, ENTITY + " deleted:\n" + equipmentTypeFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForEquipmentTypes()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}

	private EquipmentType handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		EquipmentType entity = equipmentTypeService.getById(id);

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

						}
					}
				}
			});
		}

		return entity;
	}
}
