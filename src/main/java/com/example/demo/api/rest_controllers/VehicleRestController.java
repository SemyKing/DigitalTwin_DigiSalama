package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Vehicle;
import com.example.demo.database.services.VehicleService;
import com.example.demo.utils.StringUtils;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class VehicleRestController {

	private final String ENTITY =       "vehicle";
	private final String ENTITY_LIST =  "vehicles";

	// CREATE
	private final String POST_ENTITY_URL =  StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// GET
	private final String GET_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/REPLACE
	private final String PUT_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/MODIFY
	private final String PATCH_ENTITY_URL = StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// DELETE
	private final String DELETE_ENTITY_URL =StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;


	@Autowired
	private final VehicleService vehicleService;



	@PostMapping(value = POST_ENTITY_URL, consumes = StringUtils.APPLICATION_JSON, produces = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> postEntity(@RequestBody Vehicle vehicle) {

		ValidationResponse response = vehicleService.validateVehicle(vehicle);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to save " + ENTITY + " in database");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(vehicleFromDatabase);
		}

//		return ResponseEntity.status(HttpStatus.OK).body(vehicleService.save(vehicle));
	}

	@PostMapping(value = POST_ENTITY_URL + StringUtils.ID, consumes = StringUtils.APPLICATION_JSON)
	public void postEntityWithID(@RequestBody Vehicle vehicle, @PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "resource already exists");
		}
	}



	@GetMapping(value = GET_ENTITY_URL, produces = StringUtils.APPLICATION_JSON)
	public List<Vehicle> getEntityList() {
		List<Vehicle> vehicles = vehicleService.getAll();

		// SORT etc...
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		return vehicles;
	}

	@GetMapping(value = GET_ENTITY_URL + StringUtils.ID, produces = StringUtils.APPLICATION_JSON)
	public Vehicle getEntityWithID(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return vehicleFromDatabase;
	}



	@PutMapping(value = PUT_ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateReplaceEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed"); // UNLESS UPDATE EVERY RESOURCE
	}

	@PutMapping(value = PUT_ENTITY_URL + StringUtils.ID, consumes = StringUtils.APPLICATION_JSON, produces = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> updateReplaceEntity(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		// EMPTY BODY
		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = vehicleService.validateVehicle(vehicle);

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



	@PatchMapping(value = PATCH_ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateModifyEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = PATCH_ENTITY_URL + StringUtils.ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> updateModifyEntity(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		// EMPTY BODY
		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = vehicleService.validateVehicle(vehicle);

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



	@DeleteMapping(value = DELETE_ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@DeleteMapping(value = DELETE_ENTITY_URL + StringUtils.ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		vehicleService.delete(vehicleFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).build();
	}



	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			Vehicle vehicle = new Vehicle();
			vehicle.setName("vehicle_" + i);
			vehicle.setRegistrationPlate("ABC-00" + i);
			vehicle.setVin(i + "_HD1KHM16DB613457");
			vehicleService.save(vehicle);
		}
	}

	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		vehicleService.deleteAll();
	}
}
