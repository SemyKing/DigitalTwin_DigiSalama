package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.VehicleFleet;
import com.example.demo.database.services.VehicleFleetService;
import com.example.demo.utils.StringUtils;
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
public class VehicleFleetRestController {

	private final String ENTITY =       "vehicle_fleet";
	private final String ENTITY_LIST =  "vehicle_fleets";

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
	private final VehicleFleetService vehicleFleetService;



	@PostMapping(value = POST_ENTITY_URL, consumes = "application/json")
	public ResponseEntity<VehicleFleet> postEntity(@RequestBody VehicleFleet vehicleFleet) {

		// VALIDATE ENTITY

		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.save(vehicleFleet);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to save entity");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(vehicleFleetFromDatabase);
		}
	}

	@PostMapping(value = POST_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public void postEntityWithID(@RequestBody VehicleFleet vehicleFleet, @PathVariable Long id) {
		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.getById(id);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}
	}



	@GetMapping(value = GET_ENTITY_URL, produces = "application/json")
	public List<VehicleFleet> getEntityList() {
		List<VehicleFleet> vehicleFleets = vehicleFleetService.getAll();

		// SORT etc...
		vehicleFleets.sort(Comparator.comparing(VehicleFleet::getId));

		return vehicleFleets;
	}

	@GetMapping(value = GET_ENTITY_URL + StringUtils.ID, produces = "application/json")
	public VehicleFleet getEntityWithID(@PathVariable Long id) {
		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.getById(id);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		return vehicleFleetFromDatabase;
	}



	@PutMapping(value = PUT_ENTITY_URL, consumes = "application/json")
	public VehicleFleet updateReplaceEntityList(@RequestBody List<VehicleFleet> vehicleFleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
	}

	@PutMapping(value = PUT_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<VehicleFleet> updateReplaceEntity(@RequestBody VehicleFleet vehicleFleet, @PathVariable Long id) {
		System.out.println("PUT ENTITY");

		// VALIDATE ENTITY
		System.out.println(vehicleFleet);

		// EMPTY BODY
		if (vehicleFleet == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		}

		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.getById(id);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		vehicleFleet.setId(vehicleFleetFromDatabase.getId());

		vehicleFleetService.save(vehicleFleet);

		return ResponseEntity.status(HttpStatus.OK).body(vehicleFleet);
	}



	@PatchMapping(value = PATCH_ENTITY_URL, consumes = "application/json")
	public void updateModifyEntityList(@RequestBody List<VehicleFleet> vehicleFleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = PATCH_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<VehicleFleet> updateModifyEntity(@RequestBody VehicleFleet vehicleFleet, @PathVariable Long id) {

		// VALIDATE ENTITY
		System.out.println(vehicleFleet);

		// EMPTY BODY
		if (vehicleFleet == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No content");
		}

		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.getById(id);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		vehicleFleet.setId(vehicleFleetFromDatabase.getId());

		vehicleFleetService.save(vehicleFleet);

		return ResponseEntity.status(HttpStatus.OK).body(vehicleFleet);
	}



	@DeleteMapping(value = DELETE_ENTITY_URL, consumes = "application/json")
	public void deleteEntityList(@RequestBody List<VehicleFleet> vehicleFleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@DeleteMapping(value = DELETE_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		VehicleFleet vehicleFleetFromDatabase = vehicleFleetService.getById(id);

		if (vehicleFleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		vehicleFleetService.delete(vehicleFleetFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).build();
	}



	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			VehicleFleet vehicleFleet = new VehicleFleet();
			vehicleFleet.setName("FleetName_" + i);

			vehicleFleetService.save(vehicleFleet);
		}
	}

	// FOR TESTING
	@DeleteMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		vehicleFleetService.deleteAll();
	}
}
