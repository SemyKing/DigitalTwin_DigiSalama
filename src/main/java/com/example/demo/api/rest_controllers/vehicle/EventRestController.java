package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.Organisation;
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
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = eventService.validate(event, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				VehicleEvent eventFromDatabase = eventService.save(event);

				if (eventFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");
				} else {
					restResponse.setBody(eventFromDatabase);
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
	public ResponseEntity<RestResponse<VehicleEvent>> post(@RequestBody VehicleEvent event) {

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();
		restResponse.setBody(event);
		
		ValidationResponse response = eventService.validate(event, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		VehicleEvent eventFromDatabase = eventService.save(event);

		if (eventFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(eventFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody VehicleEvent event, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<VehicleEvent> getAll() {
		return eventService.getAll();
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
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = eventService.validate(event, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				VehicleEvent eventFromDatabase = eventService.save(event);

				if (eventFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");
				} else {
					restResponse.setBody(eventFromDatabase);
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
	public ResponseEntity<RestResponse<VehicleEvent>> putById(@RequestBody VehicleEvent event, @PathVariable Long id) {

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();
		restResponse.setBody(event);
		
		ValidationResponse response = eventService.validate(event, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		VehicleEvent eventFromDatabase = eventService.save(event);

		if (eventFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(eventFromDatabase);
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

					VehicleEvent eventFromDatabase = eventService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(VehicleEvent.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, eventFromDatabase, value);
						}
					});

					RestResponse<VehicleEvent> restResponse = new RestResponse<>();
					restResponse.setBody(eventFromDatabase);
					
					ValidationResponse response = eventService.validate(eventFromDatabase, Mapping.PATCH);

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
	public ResponseEntity<RestResponse<VehicleEvent>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(VehicleEvent.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, eventFromDatabase, value);
			}
		});

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();
		restResponse.setBody(eventFromDatabase);
		
		ValidationResponse response = eventService.validate(eventFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		VehicleEvent patchedVehicleEvent = eventService.save(eventFromDatabase);
		restResponse.setBody(patchedVehicleEvent);

		if (patchedVehicleEvent == null) {
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
	public ResponseEntity<List<RestResponse<VehicleEvent>>> deleteList(@RequestBody List<VehicleEvent> events) {

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = eventService.validate(event, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				responseList.add(restResponse);

				errorOccurred = true;
			} else {
				try {
					eventService.delete(event);

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
	public ResponseEntity<RestResponse<VehicleEvent>> deleteById(@PathVariable Long id) {
		VehicleEvent eventFromDatabase = eventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			eventService.delete(eventFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();
		restResponse.setBody(eventFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		return ResponseEntity.ok(restResponse);
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
