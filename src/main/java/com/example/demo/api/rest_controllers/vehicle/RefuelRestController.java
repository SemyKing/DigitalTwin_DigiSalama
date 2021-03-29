package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.services.vehicle.RefuelService;
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
@RequestMapping(StringUtils.JSON_API + "/refuels")
public class RefuelRestController {

	private final String ENTITY = "refuel";

	@Autowired
	private final RefuelService refuelService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Refuel>>> postList(@RequestBody List<Refuel> refuels) {

		boolean errorOccurred = false;

		List<RestResponse<Refuel>> responseList = new ArrayList<>();

		for (Refuel refuel : refuels) {
			ValidationResponse response = refuelService.validate(refuel, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<Refuel> responseHandler = new RestResponse<>();
				responseHandler.setBody(refuel);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Refuel refuelFromDatabase = refuelService.save(refuel);

				RestResponse<Refuel> responseHandler = new RestResponse<>();
				responseHandler.setBody(refuel);

				if (refuelFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(refuelFromDatabase);
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
	public ResponseEntity<RestResponse<Refuel>> post(@RequestBody Refuel refuel) {

		ValidationResponse response = refuelService.validate(refuel, Mapping.POST);

		RestResponse<Refuel> responseHandler = new RestResponse<>();
		responseHandler.setBody(refuel);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Refuel refuelFromDatabase = refuelService.save(refuel);

		if (refuelFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(refuelFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody Refuel refuel, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Refuel> getAll() {
		List<Refuel> refuels = refuelService.getAll();

		// TODO: MAYBE REMOVE
		refuels.sort(Comparator.comparing(Refuel::getId));

		return refuels;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Refuel getByID(@PathVariable Long id) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return refuelFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Refuel>>> putList(@RequestBody List<Refuel> refuels) {

		boolean errorOccurred = false;

		List<RestResponse<Refuel>> responseList = new ArrayList<>();

		for (Refuel refuel : refuels) {
			ValidationResponse response = refuelService.validate(refuel, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<Refuel> responseHandler = new RestResponse<>();
				responseHandler.setBody(refuel);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Refuel refuelFromDatabase = refuelService.save(refuel);

				RestResponse<Refuel> responseHandler = new RestResponse<>();
				responseHandler.setBody(refuel);

				if (refuelFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(refuelFromDatabase);
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
	public ResponseEntity<Refuel> putById(@RequestBody Refuel refuel, @PathVariable Long id) {

		if (refuel == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = refuelService.validate(refuel, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		refuel.setId(refuelFromDatabase.getId());

		refuelService.save(refuel);

		return ResponseEntity.status(HttpStatus.OK).body(refuel);
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

					Refuel refuelFromDatabase = refuelService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(Refuel.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, refuelFromDatabase, value);
						}
					});

					ValidationResponse response = refuelService.validate(refuelFromDatabase, Mapping.PATCH);

					RestResponse<Refuel> userResponse = new RestResponse<>();
					userResponse.setBody(refuelFromDatabase);

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
	public ResponseEntity<RestResponse<Refuel>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(Refuel.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, refuelFromDatabase, value);
			}
		});

		RestResponse<Refuel> responseHandler = new RestResponse<>();

		ValidationResponse response = refuelService.validate(refuelFromDatabase, Mapping.PATCH);
		responseHandler.setBody(refuelFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Refuel patchedRefuel = refuelService.save(refuelFromDatabase);
		responseHandler.setBody(patchedRefuel);

		if (patchedRefuel == null) {
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
	public ResponseEntity<List<RestResponse<Refuel>>> deleteList(@RequestBody List<Refuel> refuels) {

		boolean errorOccurred = false;

		List<RestResponse<Refuel>> responseList = new ArrayList<>();

		for (Refuel refuel : refuels) {
			ValidationResponse response = refuelService.validate(refuel, Mapping.DELETE);

			RestResponse<Refuel> responseHandler = new RestResponse<>();
			responseHandler.setBody(refuel);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					refuelService.delete(refuel);

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
	public ResponseEntity<RestResponse<Refuel>> deleteById(@PathVariable Long id) {
		Refuel refuelFromDatabase = refuelService.getById(id);

		if (refuelFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			refuelService.delete(refuelFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<Refuel> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(refuelFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			Refuel refuel = new Refuel();
			refuel.setPrice(14.96F);
			refuel.setFuel_name("Diesel");
			refuel.setLocation("Location_" + i);
			refuel.setDescription("Description for Refuel_" + i);

			refuelService.save(refuel);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		refuelService.deleteAll();
	}
}
