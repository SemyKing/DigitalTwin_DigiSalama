package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.services.vehicle.EventService;
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
@RequestMapping(StringUtils.JSON_API + "/events")
public class EventRestController {

	private final String ENTITY = "event";

	@Autowired
	private final EventService eventService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<VehicleEvent>>> postList(@RequestBody List<VehicleEvent> events) {

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			ValidationResponse response = eventService.validate(event, Mapping.POST);

			if (!response.isValid()) {
				RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
				responseHandler.setBody(event);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				VehicleEvent eventFromDatabase = eventService.save(event);

				RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
				responseHandler.setBody(event);

				if (eventFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(eventFromDatabase);
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
	public ResponseEntity<RestResponse<VehicleEvent>> post(@RequestBody VehicleEvent event) {

		ValidationResponse response = eventService.validate(event, Mapping.POST);

		RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
		responseHandler.setBody(event);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		VehicleEvent eventFromDatabase = eventService.save(event);

		if (eventFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(eventFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody VehicleEvent event, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<VehicleEvent> getAll() {
		List<VehicleEvent> events = eventService.getAll();

		// TODO: MAYBE REMOVE
		events.sort(Comparator.comparing(VehicleEvent::getId));

		return events;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public VehicleEvent getByID(@PathVariable Long id) {
		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return eventFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<VehicleEvent>>> putList(@RequestBody List<VehicleEvent> events) {

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			ValidationResponse response = eventService.validate(event, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
				responseHandler.setBody(event);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				VehicleEvent eventFromDatabase = eventService.save(event);

				RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
				responseHandler.setBody(event);

				if (eventFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(eventFromDatabase);
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
	public ResponseEntity<VehicleEvent> putById(@RequestBody VehicleEvent event, @PathVariable Long id) {

		if (event == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = eventService.validate(event, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		event.setId(eventFromDatabase.getId());

		eventService.save(event);

		return ResponseEntity.status(HttpStatus.OK).body(event);
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

					VehicleEvent eventFromDatabase = eventService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(VehicleEvent.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, eventFromDatabase, value);
						}
					});

					ValidationResponse response = eventService.validate(eventFromDatabase, Mapping.PATCH);

					RestResponse<VehicleEvent> userResponse = new RestResponse<>();
					userResponse.setBody(eventFromDatabase);

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
	public ResponseEntity<RestResponse<VehicleEvent>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(VehicleEvent.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, eventFromDatabase, value);
			}
		});

		RestResponse<VehicleEvent> responseHandler = new RestResponse<>();

		ValidationResponse response = eventService.validate(eventFromDatabase, Mapping.PATCH);
		responseHandler.setBody(eventFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		VehicleEvent patchedVehicleEvent = eventService.save(eventFromDatabase);
		responseHandler.setBody(patchedVehicleEvent);

		if (patchedVehicleEvent == null) {
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
	public ResponseEntity<List<RestResponse<VehicleEvent>>> deleteList(@RequestBody List<VehicleEvent> events) {

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			ValidationResponse response = eventService.validate(event, Mapping.DELETE);

			RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
			responseHandler.setBody(event);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					eventService.delete(event);

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
	public ResponseEntity<RestResponse<VehicleEvent>> deleteById(@PathVariable Long id) {
		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			eventService.delete(eventFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<VehicleEvent> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(eventFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 5; i++) {
			VehicleEvent event = new VehicleEvent();
			event.setName("VehicleEvent_" + i);
			event.setDescription("Description for VehicleEvent_" + i);

			eventService.save(event);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		eventService.deleteAll();
	}
}
