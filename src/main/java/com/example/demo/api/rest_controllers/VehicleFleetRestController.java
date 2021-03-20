package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.services.vehicle.FleetService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Mapping;
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
public class VehicleFleetRestController {

	private final String ENTITY =       "fleet";
	private final String ENTITY_LIST =  "fleets";

	private final String ENTITY_URL =  			StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String ENTITY_URL_WITH_ID =	StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID;


	@Autowired
	private final FleetService fleetService;

	@Autowired
	private final VehicleService vehicleService;



	@PostMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Fleet> postEntity(@RequestBody Fleet fleet) {
		ValidationResponse response = fleetService.validate(fleet, Mapping.POST_API);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		Fleet fleetFromDatabase = fleetService.save(fleet);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to save " + ENTITY + " in database");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(fleetFromDatabase);
		}
	}

	@PostMapping(value = ENTITY_URL_WITH_ID, consumes = "application/json")
	public void postEntityWithID(@RequestBody Fleet fleet, @PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "resource already exists");
		}
	}



	@GetMapping(value = ENTITY_URL, produces = StringUtils.APPLICATION_JSON)
	public List<Fleet> getEntityList() {
		List<Fleet> fleets = fleetService.getAll();
		fleets.sort(Comparator.comparing(Fleet::getId));

		return fleets;
	}

	@GetMapping(value = ENTITY_URL_WITH_ID, produces = StringUtils.APPLICATION_JSON)
	public Fleet getEntityWithID(@PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return fleetFromDatabase;
	}



	@PutMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public Fleet updateReplaceEntityList(@RequestBody List<Fleet> fleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PutMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Fleet> updateReplaceEntity(@RequestBody Fleet fleet, @PathVariable Long id) {

		if (fleet == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = fleetService.validate(fleet, Mapping.PUT_API);

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



	@PatchMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateModifyEntityList(@RequestBody List<Fleet> fleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PatchMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Fleet> updateModifyEntity(@RequestBody Fleet fleet, @PathVariable Long id) {

		if (fleet == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = fleetService.validate(fleet, Mapping.PATCH_API);

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



	@DeleteMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<Fleet> fleets) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@DeleteMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		Fleet fleetFromDatabase = fleetService.getById(id);

		if (fleetFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			fleetService.delete(fleetFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		return ResponseEntity.status(HttpStatus.OK).build();
	}



	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			Fleet fleet = new Fleet();
			fleet.setName("FleetName_" + i);

			fleetService.save(fleet);
		}
	}

	// FOR TESTING
	@DeleteMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		fleetService.deleteAll();
	}
}
