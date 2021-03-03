package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.User;
import com.example.demo.database.models.Vehicle;
import com.example.demo.database.services.VehicleService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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



	@PostMapping(value = POST_ENTITY_URL, consumes = "application/json")
	public ResponseEntity<Vehicle> postEntity(@RequestBody Vehicle vehicle) {

		// VALIDATE ENTITY

		Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to save entity");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(vehicleFromDatabase);
		}

//		return ResponseEntity.status(HttpStatus.OK).body(vehicleService.save(vehicle));
	}

	@PostMapping(value = POST_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public void postEntityWithID(@RequestBody Vehicle vehicle, @PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}
	}



	@GetMapping(value = GET_ENTITY_URL, produces = "application/json")
	public List<Vehicle> getEntityList() {
		List<Vehicle> vehicles = vehicleService.getAll();

		// SORT etc...
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		return vehicles;
	}

	@GetMapping(value = GET_ENTITY_URL + StringUtils.ID, produces = "application/json")
	public Vehicle getEntityWithID(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		return vehicleFromDatabase;
	}



	@PutMapping(value = PUT_ENTITY_URL, consumes = "application/json")
	public Vehicle updateReplaceEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
	}

	@PutMapping(value = PUT_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<Vehicle> updateReplaceEntity(@RequestBody Vehicle vehicle, @PathVariable Long id) {
		System.out.println("PUT ENTITY");

		// VALIDATE ENTITY
		System.out.println(vehicle);

		// EMPTY BODY
		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		}

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		vehicle.setId(vehicleFromDatabase.getId());

		vehicleService.save(vehicle);

		return ResponseEntity.status(HttpStatus.OK).body(vehicle);
	}



	@PatchMapping(value = PATCH_ENTITY_URL, consumes = "application/json")
	public void updateModifyEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = PATCH_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<Vehicle> updateModifyEntity(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		// VALIDATE ENTITY
		System.out.println(vehicle);

		// EMPTY BODY
		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No content");
		}

		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		vehicle.setId(vehicleFromDatabase.getId());

		vehicleService.save(vehicle);

		return ResponseEntity.status(HttpStatus.OK).body(vehicle);
	}



	@DeleteMapping(value = DELETE_ENTITY_URL, consumes = "application/json")
	public void deleteEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@DeleteMapping(value = DELETE_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		vehicleService.delete(vehicleFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).build();
	}




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

	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		vehicleService.deleteAll();
	}
}
