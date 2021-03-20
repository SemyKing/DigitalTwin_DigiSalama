package com.example.demo.api.rest_controllers;

import com.example.demo.api.handlers.RestResponseHandler;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.OrganisationService;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api2/vehicles")
public class VehicleRestController {

	private final String ENTITY =       "vehicle";
	private final String ENTITY_LIST =  "vehicles";

	private final String ENTITY_URL =  			StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String ENTITY_URL_WITH_ID =  	StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID;


	@Autowired
	private final VehicleService vehicleService;

	@Autowired
	private final OrganisationService organisationService;



	@PostMapping(value = {"/batch"}, consumes = StringUtils.APPLICATION_JSON, produces = StringUtils.APPLICATION_JSON)
	public ResponseEntity<List<RestResponseHandler<Vehicle>>> postEntityList(@RequestBody List<Vehicle> vehicles) {

		boolean errorOccurred = false;

		List<RestResponseHandler<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST_API);

			if (!response.isValid()) {
				RestResponseHandler<Vehicle> responseHandler = new RestResponseHandler<>();
				responseHandler.setBody(vehicle);
				responseHandler.setHttpStatus(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				RestResponseHandler<Vehicle> responseHandler = new RestResponseHandler<>();
				responseHandler.setBody(vehicle);

				if (vehicleFromDatabase == null) {
					responseHandler.setHttpStatus(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setHttpStatus(HttpStatus.OK);
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


	@PostMapping(value = {"", "/"}, consumes = StringUtils.APPLICATION_JSON, produces = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> postEntity(@RequestBody Vehicle vehicle) {

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.POST_API);

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

//	@PostMapping(value = "/{id}", consumes = StringUtils.APPLICATION_JSON)
//	public void postEntityByID(@RequestBody Vehicle vehicle, @PathVariable Long id) {
//		Vehicle vehicleFromDatabase = vehicleService.getById(id);
//
//		if (vehicleFromDatabase == null) {
//			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
//		} else {
//			throw new ResponseStatusException(HttpStatus.CONFLICT, "resource already exists");
//		}
//	}



	@GetMapping(value = {"", "/"}, produces = StringUtils.APPLICATION_JSON)
	public List<Vehicle> getEntityList() {
		List<Vehicle> vehicles = vehicleService.getAll();
		vehicles.sort(Comparator.comparing(Vehicle::getId));

		return vehicles;
	}

	@GetMapping(value = "/{id}", produces = StringUtils.APPLICATION_JSON)
	public Vehicle getEntityByID(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return vehicleFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<List<RestResponseHandler<Vehicle>>> putEntityList(@RequestBody List<Vehicle> vehicles) {

		boolean errorOccurred = false;

		List<RestResponseHandler<Vehicle>> responseList = new ArrayList<>();

		for (Vehicle vehicle : vehicles) {
			ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT_API);

			if (!response.isValid()) {
				RestResponseHandler<Vehicle> responseHandler = new RestResponseHandler<>();
				responseHandler.setBody(vehicle);
				responseHandler.setHttpStatus(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				Vehicle vehicleFromDatabase = vehicleService.save(vehicle);

				RestResponseHandler<Vehicle> responseHandler = new RestResponseHandler<>();
				responseHandler.setBody(vehicle);

				if (vehicleFromDatabase == null) {
					responseHandler.setHttpStatus(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setHttpStatus(HttpStatus.OK);
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

	@PutMapping(value = "/{id}", consumes = StringUtils.APPLICATION_JSON, produces = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> putEntityById(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PUT_API);

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



	@PatchMapping(value = {"", "/"}, consumes = StringUtils.APPLICATION_JSON)
	public void patchEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = "/{id}", consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<Vehicle> patchEntityById(@RequestBody Vehicle vehicle, @PathVariable Long id) {

		if (vehicle == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = vehicleService.validate(vehicle, Mapping.PATCH_API);

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



	@DeleteMapping(value = {"", "/"}, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<Vehicle> vehicles) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}


	@DeleteMapping(value = "/{id}")
	public ResponseEntity<RestResponseHandler<Vehicle>> deleteEntityById(@PathVariable Long id) {
		Vehicle vehicleFromDatabase = vehicleService.getById(id);

		if (vehicleFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			vehicleService.delete(vehicleFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponseHandler<Vehicle> responseHandler = new RestResponseHandler<>();
		responseHandler.setMessage("vehicle deleted successfully");
		responseHandler.setBody(vehicleFromDatabase);
		responseHandler.setHttpStatus(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}




	// FOR TESTING
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			Vehicle vehicle = new Vehicle();
			vehicle.setName("vehicle_" + i);
			vehicle.setRegistrationNumber("ABC-00" + i);
			vehicle.setVin(i + "_HD1KHM16DB613457");

			List<Organisation> organisations = organisationService.getAll();
			if (organisations.size() > 0) {
				vehicle.setOrganisation(organisations.get(0));
			}

			vehicleService.save(vehicle);
		}
	}

	// FOR TESTING
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
