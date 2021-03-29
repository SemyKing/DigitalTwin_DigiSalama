package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.services.vehicle.FleetService;
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
@RequestMapping(StringUtils.JSON_API + "/fleets")
public class FleetRestController {

	private final String ENTITY = "fleet";

	@Autowired
	private final FleetService fleetService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Fleet>>> postList(@RequestBody List<Fleet> fleets) {

		boolean errorOccurred = false;

		List<RestResponse<Fleet>> responseList = new ArrayList<>();

		for (Fleet fleet : fleets) {
			ValidationResponse response = fleetService.validate(fleet, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<Fleet> responseHandler = new RestResponse<>();
				responseHandler.setBody(fleet);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Fleet fleetFromDatabase = fleetService.save(fleet);

				RestResponse<Fleet> responseHandler = new RestResponse<>();
				responseHandler.setBody(fleet);

				if (fleetFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(fleetFromDatabase);
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
	public ResponseEntity<RestResponse<Fleet>> post(@RequestBody Fleet fleet) {

		ValidationResponse response = fleetService.validate(fleet, Mapping.POST);

		RestResponse<Fleet> responseHandler = new RestResponse<>();
		responseHandler.setBody(fleet);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(fleetFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody Fleet fleet, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Fleet> getAll() {
		List<Fleet> fleets = fleetService.getAll();

		// TODO: MAYBE REMOVE
		fleets.sort(Comparator.comparing(Fleet::getId));

		return fleets;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Fleet getByID(@PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return fleetFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Fleet>>> putList(@RequestBody List<Fleet> fleets) {

		boolean errorOccurred = false;

		List<RestResponse<Fleet>> responseList = new ArrayList<>();

		for (Fleet fleet : fleets) {
			ValidationResponse response = fleetService.validate(fleet, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<Fleet> responseHandler = new RestResponse<>();
				responseHandler.setBody(fleet);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Fleet fleetFromDatabase = fleetService.save(fleet);

				RestResponse<Fleet> responseHandler = new RestResponse<>();
				responseHandler.setBody(fleet);

				if (fleetFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(fleetFromDatabase);
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
	public ResponseEntity<Fleet> putById(@RequestBody Fleet fleet, @PathVariable Long id) {

		if (fleet == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = fleetService.validate(fleet, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		fleet.setId(fleetFromDatabase.getId());

		fleetService.save(fleet);

		return ResponseEntity.status(HttpStatus.OK).body(fleet);
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

					Fleet fleetFromDatabase = fleetService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(Fleet.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, fleetFromDatabase, value);
						}
					});

					ValidationResponse response = fleetService.validate(fleetFromDatabase, Mapping.PATCH);

					RestResponse<Fleet> userResponse = new RestResponse<>();
					userResponse.setBody(fleetFromDatabase);

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
	public ResponseEntity<RestResponse<Fleet>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(Fleet.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, fleetFromDatabase, value);
			}
		});

		RestResponse<Fleet> responseHandler = new RestResponse<>();

		ValidationResponse response = fleetService.validate(fleetFromDatabase, Mapping.PATCH);
		responseHandler.setBody(fleetFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Fleet patchedFleet = fleetService.save(fleetFromDatabase);
		responseHandler.setBody(patchedFleet);

		if (patchedFleet == null) {
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
	public ResponseEntity<List<RestResponse<Fleet>>> deleteList(@RequestBody List<Fleet> fleets) {

		boolean errorOccurred = false;

		List<RestResponse<Fleet>> responseList = new ArrayList<>();

		for (Fleet fleet : fleets) {
			ValidationResponse response = fleetService.validate(fleet, Mapping.DELETE);

			RestResponse<Fleet> responseHandler = new RestResponse<>();
			responseHandler.setBody(fleet);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					fleetService.delete(fleet);

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
	public ResponseEntity<RestResponse<Fleet>> deleteById(@PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			fleetService.delete(fleetFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<Fleet> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(fleetFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			Fleet fleet = new Fleet();
			fleet.setName("Fleet_" + i);

			fleetService.save(fleet);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		fleetService.deleteAll();
	}
}
