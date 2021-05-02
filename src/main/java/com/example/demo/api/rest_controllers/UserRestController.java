package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.PasswordUpdateRequest;
import com.example.demo.database.models.user.PasswordUpdateResponse;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/users")
public class UserRestController {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String ENTITY = "user";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;

	@Autowired
	private ObjectMapper objectMapper;



	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> postList(@RequestBody List<User> users) {

		if (users == null || users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			RestResponse<User> restResponse = new RestResponse<>();
			restResponse.setBody(user);

			ValidationResponse response = userService.validate(user, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				User userFromDatabase = userService.save(user);

				if (userFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(userFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("create " + ENTITY, ENTITY + " created:\n" + userFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.ok(responseList);
		}
	}

	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<User>> post(@RequestBody User user) {

		RestResponse<User> restResponse = new RestResponse<>();
		restResponse.setBody(user);

		ValidationResponse response = userService.validate(user, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(userFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("create " + ENTITY, ENTITY + " created:\n" + userFromDatabase);

			return ResponseEntity.ok(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postById(@RequestBody User user, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<User> getAll() {
		return userService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public User getById(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return userFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> putList(@RequestBody List<User> users) {

		if (users == null || users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			RestResponse<User> restResponse = new RestResponse<>();
			restResponse.setBody(user);

			ValidationResponse response = userService.validate(user, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldUserFromDatabase = userService.getById(user.getId()).toString();
				User userFromDatabase = userService.save(user);

				if (userFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(userFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldUserFromDatabase + "\nto:\n" + userFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.ok(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<User>> putById(@RequestBody User user, @PathVariable Long id) {

		RestResponse<User> restResponse = new RestResponse<>();
		restResponse.setBody(user);

		user.setId(id);

		ValidationResponse response = userService.validate(user, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldUserFromDatabase = userService.getById(user.getId()).toString();
		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(userFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			addLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldUserFromDatabase + "\nto:\n" + userFromDatabase);

			return ResponseEntity.ok(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		if (changesList == null || changesList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<?>> responseList = new ArrayList<>();

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

			changes.remove("password");

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
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
					changes.remove("id");
					long idLong = (long) ((Integer) idObj);

					String oldUserFromDatabase = userService.getById(idLong).toString();

					User userFromDatabase;

					try {
						userFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<User> userResponse = new RestResponse<>();
					userResponse.setBody(userFromDatabase);

					ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						userResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						userResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						User updatedUserFromDatabase = userService.save(userFromDatabase);

						if (updatedUserFromDatabase == null) {
							userResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							userResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							userResponse.setBody(updatedUserFromDatabase);
							userResponse.setHttp_status(HttpStatus.OK);
							userResponse.setMessage(ENTITY + "patched successfully");

							addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldUserFromDatabase + "\nto:\n" + updatedUserFromDatabase);
						}
					}

					responseList.add(userResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.ok(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<User>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldUser = userFromDatabase.toString();

		changes.remove("id");
		changes.remove("password");

		RestResponse<User> restResponse = new RestResponse<>();

		try {
			userFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(userFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(userFromDatabase);

		ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		User patchedUser = userService.save(userFromDatabase);
		restResponse.setBody(patchedUser);

		if (patchedUser == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " patched successfully");

			addLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldUser + "\nto:\n" + patchedUser);

			return ResponseEntity.ok(restResponse);
		}
	}


	@PostMapping(value = "/forgot_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> forgotPassword(@RequestBody User user, HttpServletRequest request) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no content was provided");
		}

		if (user.getEmail() == null || user.getEmail().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
		}

		User userFromDatabase = userService.getByEmail(user.getEmail());

		String ip = request.getRemoteAddr();
		logger.info("forgot password request was made by IP address: " + ip);

		if (userFromDatabase == null) {
			logger.info("user with email: " + user.getEmail() + " was not found");
		} else {
			String passwordUpdateToken = userService.generatePasswordUpdateToken(userFromDatabase);
			logger.info("user with email: " + user.getEmail() + " was found and password update token was created: " + passwordUpdateToken);

			addLog("forgot password", "request was made by IP address:\n" + ip + "\nuser:\n" + user);

			//TODO: SEND EMAIL WITH RESET LINK
			// CURRENTLY WILL WORK BY CREATING POST REQUEST TO /update_password WITH passwordUpdateToken and NEW PASSWORD
		}

		// DEFAULT RESPONSE DESPITE OF RESULT
		return ResponseEntity.ok("Password reset link was sent to provided email");
	}

	@PostMapping(value = "/request_update_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<PasswordUpdateResponse>> requestPasswordUpdate(@RequestBody User user) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no content was provided");
		}

		if (user.getUsername() == null || user.getUsername().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
		}

		if (user.getPassword() == null || user.getPassword().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
		}

		User userFromDatabase = userService.getByUsername(user.getUsername());

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "credentials invalid");
		}

		if (!userService.getBcryptEncoder().matches(user.getPassword(), userFromDatabase.getPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "credentials invalid");
		}

		ObjectMapper objectMapper = new ObjectMapper();

		String passwordUpdateToken = userService.generatePasswordUpdateToken(userFromDatabase);

		RestResponse<PasswordUpdateResponse> restResponse = new RestResponse<>();

		restResponse.setBody(new PasswordUpdateResponse(passwordUpdateToken));
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage("please create new POST request to: '/update_password' with 'password_update_token' and 'new_password'");

		return ResponseEntity.ok(restResponse);
	}

	@PostMapping(value = "/update_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest) {

		if (passwordUpdateRequest == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no content was provided");
		}

		if (passwordUpdateRequest.getPassword_update_token() == null || passwordUpdateRequest.getPassword_update_token().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is required");
		}

		if (passwordUpdateRequest.getNew_password() == null || passwordUpdateRequest.getNew_password().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
		}

		User userFromDatabase = userService.getByPasswordUpdateToken(passwordUpdateRequest.getPassword_update_token());
		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is invalid or not found");
		}

		userFromDatabase.setPassword(userService.getBcryptEncoder().encode(passwordUpdateRequest.getNew_password()));
		userFromDatabase.setPassword_update_token(null);
		userService.save(userFromDatabase);

		addLog("update password", "password updated for user:\n" + userFromDatabase);

		return ResponseEntity.ok("password updated successfully");
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> deleteList(@RequestBody List<User> users) {

		if (users == null || users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			ValidationResponse response = userService.validate(user, Mapping.DELETE);

			RestResponse<User> restResponse = new RestResponse<>();
			restResponse.setBody(user);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					userService.delete(user);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					addLog("delete " + ENTITY, ENTITY + " deleted:\n" + user);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());

					errorOccurred = true;
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.ok(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<User>> deleteById(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = userService.validate(userFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			userService.delete(userFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not delete " + ENTITY + " with ID: " + id);
		}

		RestResponse<User> restResponse = new RestResponse<>();

		restResponse.setBody(userFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		addLog("delete " + ENTITY, ENTITY + " deleted:\n" + userFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private void addLog(String action, String description) {
		if (eventHistoryLogService.isLoggingEnabledForUsers()) {
			EventHistoryLog log = new EventHistoryLog();
			log.setWho_did(eventHistoryLogService.getCurrentUser() == null ? "NULL" : eventHistoryLogService.getCurrentUser().toString());
			log.setAction(action);
			log.setDescription(description);

			eventHistoryLogService.save(log);
		}
	}


	private User  handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		User entity = userService.getById(id);

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

							if (field.getType().equals(Organisation.class)) {
								try {
									entity.setOrganisation(objectMapper.readValue((String) value, Organisation.class));
								} catch (JsonProcessingException e) {
									e.printStackTrace();
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
