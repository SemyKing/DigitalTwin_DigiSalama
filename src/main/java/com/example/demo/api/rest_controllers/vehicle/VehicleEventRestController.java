package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.VehicleEventService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/vehicle_events")
public class VehicleEventRestController {

	private final String ENTITY = "vehicle_event";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final VehicleEventService vehicleEventService;

	@Autowired
	private ObjectMapper objectMapper;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<VehicleEvent>>> postList(@RequestBody List<VehicleEvent> events) {

		if (events == null || events.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = vehicleEventService.validate(event, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				VehicleEvent eventFromDatabase = vehicleEventService.save(event);

				if (eventFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(eventFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					eventHistoryLogService.addVehicleEventLog("create " + ENTITY, ENTITY + " created:\n" + eventFromDatabase);
				}
			}

			responseList.add(restResponse);
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
		
		ValidationResponse response = vehicleEventService.validate(event, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		VehicleEvent eventFromDatabase = vehicleEventService.save(event);

		if (eventFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(eventFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addVehicleEventLog("create " + ENTITY, ENTITY + " created:\n" + eventFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody VehicleEvent event, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<VehicleEvent> getAll() {
		return vehicleEventService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public VehicleEvent getByID(@PathVariable Long id) {
		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return eventFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<VehicleEvent>>> putList(@RequestBody List<VehicleEvent> events) {

		if (events == null || events.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = vehicleEventService.validate(event, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldEventFromDatabase = vehicleEventService.getById(event.getId()).toString();
				VehicleEvent eventFromDatabase = vehicleEventService.save(event);

				if (eventFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(eventFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					eventHistoryLogService.addVehicleEventLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEventFromDatabase + "\nto:\n" + eventFromDatabase);
				}
			}

			responseList.add(restResponse);
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
		
		ValidationResponse response = vehicleEventService.validate(event, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldEventFromDatabase = vehicleEventService.getById(event.getId()).toString();
		VehicleEvent eventFromDatabase = vehicleEventService.save(event);

		if (eventFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(eventFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addVehicleEventLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldEventFromDatabase + "\nto:\n" + eventFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		if (changesList == null || changesList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (changes == null) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("NULL array element was provided");
				responseList.add(mapResponse);
				errorOccurred = true;
				continue;
			}

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
					long idLong = (long) ((Integer) idObj);
					changes.remove("id");

					String oldEventFromDatabase = vehicleEventService.getById(idLong).toString();
					VehicleEvent eventFromDatabase;

					try {
						eventFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<VehicleEvent> restResponse = new RestResponse<>();
					restResponse.setBody(eventFromDatabase);
					
					ValidationResponse response = vehicleEventService.validate(eventFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						VehicleEvent updatedEventFromDatabase = vehicleEventService.save(eventFromDatabase);

						if (updatedEventFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedEventFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							eventHistoryLogService.addVehicleEventLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEventFromDatabase + "\nto:\n" + updatedEventFromDatabase);
						}
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

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldEventFromDatabase = eventFromDatabase.toString();

		changes.remove("id");

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();

		try {
			eventFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(eventFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(eventFromDatabase);
		
		ValidationResponse response = vehicleEventService.validate(eventFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		VehicleEvent patchedVehicleEvent = vehicleEventService.save(eventFromDatabase);
		restResponse.setBody(patchedVehicleEvent);

		if (patchedVehicleEvent == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addVehicleEventLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldEventFromDatabase + "\nto:\n" + patchedVehicleEvent);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<VehicleEvent>>> deleteList(@RequestBody List<VehicleEvent> events) {

		if (events == null || events.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}
		boolean errorOccurred = false;

		List<RestResponse<VehicleEvent>> responseList = new ArrayList<>();

		for (VehicleEvent event : events) {
			RestResponse<VehicleEvent> restResponse = new RestResponse<>();
			restResponse.setBody(event);
			
			ValidationResponse response = vehicleEventService.validate(event, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					vehicleEventService.delete(event);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					eventHistoryLogService.addVehicleEventLog("delete " + ENTITY, ENTITY + " deleted:\n" + event);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<VehicleEvent>> deleteById(@PathVariable Long id) {
		VehicleEvent eventFromDatabase = vehicleEventService.getById(id);

		if (eventFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = vehicleEventService.validate(eventFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			vehicleEventService.delete(eventFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<VehicleEvent> restResponse = new RestResponse<>();
		restResponse.setBody(eventFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		eventHistoryLogService.addVehicleEventLog("delete " + ENTITY, ENTITY + " deleted:\n" + eventFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private VehicleEvent handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		VehicleEvent entity = vehicleEventService.getById(id);

		if (entity != null) {
			changes.forEach((key, value) -> {
				Field field = ReflectionUtils.findField(entity.getClass(), key);

				if (field != null) {
					field.setAccessible(true);

					String json = value == null ? null : value.toString();

					if (json == null) {
						ReflectionUtils.setField(field, entity, null);
					} else {
						if (field.getType().equals(String.class)) {
							ReflectionUtils.setField(field, entity, json);
						} else {

							if (field.getType().equals(LocalDateTime.class)) {
								LocalDateTime localDateTime = null;

								try {
									localDateTime = DateUtils.stringToLocalDateTime((String) value);
								} catch (Exception e) {
									throw new StringIndexOutOfBoundsException(e.getMessage());
								}

								ReflectionUtils.setField(field, entity, localDateTime);
							}

							if (field.getType().equals(Vehicle.class)) {
								try {
									entity.setVehicle(objectMapper.readValue(json, Vehicle.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Vehicle json parsing error: " + e.getMessage()));
								}
							}

						}
					}
				}
			});
		}

		return entity;
	}
}
