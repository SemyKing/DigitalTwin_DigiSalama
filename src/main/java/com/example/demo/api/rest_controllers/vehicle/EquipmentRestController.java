package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.services.vehicle.EquipmentService;
import com.example.demo.utils.StringUtils;
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
@RequestMapping(StringUtils.JSON_API + "/equipment")
public class EquipmentRestController {

	private final String ENTITY = "equipment";

	@Autowired
	private final EquipmentService equipmentService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Equipment>>> postList(@RequestBody List<Equipment> equipmentList) {

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<Equipment> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipment);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Equipment equipmentFromDatabase = equipmentService.save(equipment);

				RestResponse<Equipment> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipment);

				if (equipmentFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(equipmentFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}


	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Equipment>> post(@RequestBody Equipment equipment) {

		ValidationResponse response = equipmentService.validate(equipment, Mapping.POST);

		RestResponse<Equipment> responseHandler = new RestResponse<>();
		responseHandler.setBody(equipment);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Equipment equipmentFromDatabase = equipmentService.save(equipment);

		if (equipmentFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(equipmentFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody Equipment equipment, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Equipment> getAll() {
		List<Equipment> equipment = equipmentService.getAll();

		// TODO: MAYBE REMOVE
		equipment.sort(Comparator.comparing(Equipment::getId));

		return equipment;
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

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<Equipment> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipment);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Equipment equipmentFromDatabase = equipmentService.save(equipment);

				RestResponse<Equipment> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipment);

				if (equipmentFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(equipmentFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Equipment> putById(@RequestBody Equipment equipment, @PathVariable Long id) {

		if (equipment == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = equipmentService.validate(equipment, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		equipment.setId(equipmentFromDatabase.getId());

		equipmentService.save(equipment);

		return ResponseEntity.status(HttpStatus.OK).body(equipment);
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

					Equipment equipmentFromDatabase = equipmentService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(Equipment.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, equipmentFromDatabase, value);
						}
					});

					ValidationResponse response = equipmentService.validate(equipmentFromDatabase, Mapping.PATCH);

					RestResponse<Equipment> userResponse = new RestResponse<>();
					userResponse.setBody(equipmentFromDatabase);

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
	public ResponseEntity<RestResponse<Equipment>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(Equipment.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, equipmentFromDatabase, value);
			}
		});

		RestResponse<Equipment> responseHandler = new RestResponse<>();

		ValidationResponse response = equipmentService.validate(equipmentFromDatabase, Mapping.PATCH);
		responseHandler.setBody(equipmentFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Equipment patchedEquipment = equipmentService.save(equipmentFromDatabase);
		responseHandler.setBody(patchedEquipment);

		if (patchedEquipment == null) {
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
	public ResponseEntity<List<RestResponse<Equipment>>> deleteList(@RequestBody List<Equipment> equipmentList) {

		boolean errorOccurred = false;

		List<RestResponse<Equipment>> responseList = new ArrayList<>();

		for (Equipment equipment : equipmentList) {
			ValidationResponse response = equipmentService.validate(equipment, Mapping.DELETE);

			RestResponse<Equipment> responseHandler = new RestResponse<>();
			responseHandler.setBody(equipment);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					equipmentService.delete(equipment);

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
	public ResponseEntity<RestResponse<Equipment>> deleteById(@PathVariable Long id) {
		Equipment equipmentFromDatabase = equipmentService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			equipmentService.delete(equipmentFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<Equipment> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(equipmentFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			Equipment equipment = new Equipment();
			equipment.setDescription("Description for Equipment_" + i);

			equipmentService.save(equipment);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		equipmentService.deleteAll();
	}
}
