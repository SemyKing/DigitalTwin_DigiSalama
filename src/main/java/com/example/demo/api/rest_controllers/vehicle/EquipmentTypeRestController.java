package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.services.vehicle.EquipmentTypeService;
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
@RequestMapping(StringUtils.JSON_API + "/equipment_type")
public class EquipmentTypeRestController {

	private final String ENTITY = "equipment_type";

	@Autowired
	private final EquipmentTypeService equipmentTypeService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> postList(@RequestBody List<EquipmentType> equipmentTypes) {

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<EquipmentType> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipmentType);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				EquipmentType equipmentFromDatabase = equipmentTypeService.save(equipmentType);

				RestResponse<EquipmentType> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipmentType);

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
	public ResponseEntity<RestResponse<EquipmentType>> post(@RequestBody EquipmentType equipmentType) {

		ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.POST);

		RestResponse<EquipmentType> responseHandler = new RestResponse<>();
		responseHandler.setBody(equipmentType);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		EquipmentType equipmentFromDatabase = equipmentTypeService.save(equipmentType);

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
	public void postByID(@RequestBody EquipmentType equipment, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<EquipmentType> getAll() {
		List<EquipmentType> equipmentTypes = equipmentTypeService.getAll();

		// TODO: MAYBE REMOVE
		equipmentTypes.sort(Comparator.comparing(EquipmentType::getId));

		return equipmentTypes;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EquipmentType getByID(@PathVariable Long id) {
		EquipmentType equipmentFromDatabase = equipmentTypeService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return equipmentFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> putList(@RequestBody List<EquipmentType> equipmentTypes) {

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<EquipmentType> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipmentType);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

				RestResponse<EquipmentType> responseHandler = new RestResponse<>();
				responseHandler.setBody(equipmentType);

				if (equipmentTypeFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(equipmentTypeFromDatabase);
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
	public ResponseEntity<EquipmentType> putById(@RequestBody EquipmentType equipmentType, @PathVariable Long id) {

		if (equipmentType == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.getById(id);

		if (equipmentTypeFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		equipmentType.setId(equipmentTypeFromDatabase.getId());

		equipmentTypeService.save(equipmentType);

		return ResponseEntity.status(HttpStatus.OK).body(equipmentType);
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

					EquipmentType equipmentFromDatabase = equipmentTypeService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(EquipmentType.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, equipmentFromDatabase, value);
						}
					});

					ValidationResponse response = equipmentTypeService.validate(equipmentFromDatabase, Mapping.PATCH);

					RestResponse<EquipmentType> userResponse = new RestResponse<>();
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
	public ResponseEntity<RestResponse<EquipmentType>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		EquipmentType equipmentFromDatabase = equipmentTypeService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(EquipmentType.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, equipmentFromDatabase, value);
			}
		});

		RestResponse<EquipmentType> responseHandler = new RestResponse<>();

		ValidationResponse response = equipmentTypeService.validate(equipmentFromDatabase, Mapping.PATCH);
		responseHandler.setBody(equipmentFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		EquipmentType patchedEquipmentType = equipmentTypeService.save(equipmentFromDatabase);
		responseHandler.setBody(patchedEquipmentType);

		if (patchedEquipmentType == null) {
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
	public ResponseEntity<List<RestResponse<EquipmentType>>> deleteList(@RequestBody List<EquipmentType> equipmentTypes) {

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.DELETE);

			RestResponse<EquipmentType> responseHandler = new RestResponse<>();
			responseHandler.setBody(equipmentType);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					equipmentTypeService.delete(equipmentType);

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
	public ResponseEntity<RestResponse<EquipmentType>> deleteById(@PathVariable Long id) {
		EquipmentType equipmentFromDatabase = equipmentTypeService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			equipmentTypeService.delete(equipmentFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<EquipmentType> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(equipmentFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			EquipmentType equipmentType = new EquipmentType();
			equipmentType.setName("EquipmentType_" + i);

			equipmentTypeService.save(equipmentType);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		equipmentTypeService.deleteAll();
	}
}
