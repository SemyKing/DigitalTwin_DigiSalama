package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.Organisation;
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
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				EquipmentType equipmentFromDatabase = equipmentTypeService.save(equipmentType);

				if (equipmentFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");
				} else {
					restResponse.setBody(equipmentFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(restResponse);
			}
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

		EquipmentType equipmentFromDatabase = equipmentTypeService.save(equipmentType);

		if (equipmentFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

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
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

				if (equipmentTypeFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");
				} else {
					restResponse.setBody(equipmentTypeFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(restResponse);
			}
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

		EquipmentType equipmentTypeFromDatabase = equipmentTypeService.save(equipmentType);

		if (equipmentTypeFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(equipmentTypeFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

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
					mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
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
					RestResponse<EquipmentType> restResponse = new RestResponse<>();
					restResponse.setBody(equipmentFromDatabase);
					
					ValidationResponse response = equipmentTypeService.validate(equipmentFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {
						restResponse.setHttp_status(HttpStatus.OK);
						restResponse.setMessage(ENTITY + "patched successfully");
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

		EquipmentType equipmentFromDatabase = equipmentTypeService.getById(id);

		if (equipmentFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(EquipmentType.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, equipmentFromDatabase, value);
			}
		});
		
		RestResponse<EquipmentType> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentFromDatabase);
		
		ValidationResponse response = equipmentTypeService.validate(equipmentFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		EquipmentType patchedEquipmentType = equipmentTypeService.save(equipmentFromDatabase);
		restResponse.setBody(patchedEquipmentType);

		if (patchedEquipmentType == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<EquipmentType>>> deleteList(@RequestBody List<EquipmentType> equipmentTypes) {

		boolean errorOccurred = false;

		List<RestResponse<EquipmentType>> responseList = new ArrayList<>();

		for (EquipmentType equipmentType : equipmentTypes) {
			RestResponse<EquipmentType> restResponse = new RestResponse<>();
			restResponse.setBody(equipmentType);
			
			ValidationResponse response = equipmentTypeService.validate(equipmentType, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				try {
					equipmentTypeService.delete(equipmentType);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());
				}

				responseList.add(restResponse);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
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
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<EquipmentType> restResponse = new RestResponse<>();
		restResponse.setBody(equipmentFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		return ResponseEntity.ok(restResponse);
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
