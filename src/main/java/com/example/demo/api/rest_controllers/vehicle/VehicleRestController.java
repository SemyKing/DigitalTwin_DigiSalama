package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.vehicle.VehicleService;
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
@RequestMapping(StringUtils.JSON_API + "/vehicles")
public class VehicleRestController {

	private final String ENTITY = "vehicle";


	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final OrganisationService organisationService;



	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Vehicle>>> postList(@RequestBody List<Vehicle> vehicles) {

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<Vehicle> responseHandler = new RestResponse<>();
				responseHandler.setBody(vehicle);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				RestResponse<Vehicle> responseHandler = new RestResponse<>();
				responseHandler.setBody(vehicle);

				if (vehicleFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(vehicleFromDatabase);
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
	public ResponseEntity<Vehicle> post(@RequestBody Vehicle vehicle) {

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to save " + ENTITY + " in database");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(vehicleFromDatabase);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postByID(@RequestBody Vehicle vehicle, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Vehicle> getAll() {
		List<Vehicle> vehicles = vehicleService.getAll();

		// TODO: MAYBE REMOVE
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		return vehicles;
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

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<Vehicle> responseHandler = new RestResponse<>();
				responseHandler.setBody(vehicle);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				RestResponse<Vehicle> responseHandler = new RestResponse<>();
				responseHandler.setBody(vehicle);

				if (vehicleFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
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



//		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Vehicle> putById(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		vehicle.setId(vehicleFromDatabase.getId());

		vehicleService.save(vehicle);

		return ResponseEntity.status(HttpStatus.OK).body(vehicle);
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

					Vehicle vehicleFromDatabase = vehicleService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(Vehicle.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, vehicleFromDatabase, value);
						}
					});

					ValidationResponse response = vehicleService.validate(vehicleFromDatabase, Mapping.PATCH);

					RestResponse<Vehicle> userResponse = new RestResponse<>();
					userResponse.setBody(vehicleFromDatabase);

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
	public ResponseEntity<RestResponse<Vehicle>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(Vehicle.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, vehicleFromDatabase, value);
			}
		});

		RestResponse<Vehicle> responseHandler = new RestResponse<>();

		ValidationResponse response = vehicleService.validate(vehicleFromDatabase, Mapping.PATCH);
		responseHandler.setBody(vehicleFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		Vehicle patchedVehicle = vehicleService.save(vehicleFromDatabase);
		responseHandler.setBody(patchedVehicle);

		if (patchedVehicle == null) {
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
	public ResponseEntity<List<RestResponse<Vehicle>>> deleteList(@RequestBody List<Vehicle> vehicles) {

		boolean errorOccurred = false;

		List<RestResponse<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.DELETE);

			RestResponse<Vehicle> responseHandler = new RestResponse<>();
			responseHandler.setBody(vehicle);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					vehicleService.delete(vehicle);

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
	public ResponseEntity<RestResponse<Vehicle>> deleteById(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			vehicleService.delete(vehicleFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<Vehicle> responseHandler = new RestResponse<>();
		responseHandler.setMessage("vehicle deleted successfully");
		responseHandler.setBody(vehicleFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			Vehicle vehicle = new Vehicle();
			vehicle.setName("vehicle_" + i);
			vehicle.setRegistration_number("ABC-00" + i);
			vehicle.setVin(i + "_HD1KHM16DB613457");

			List<Organisation> organisations = organisationService.getAll();
			if (organisations.size() > 0) {
				vehicle.setOrganisation(organisations.get(0));
			}

			vehicleService.save(vehicle);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public ResponseEntity<String> deleteAll() {
		try {
			vehicleService.deleteAll();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "could not delete all/some data");
		}

		return new ResponseEntity<>("all vehicles successfully deleted", HttpStatus.OK);
	}
}
