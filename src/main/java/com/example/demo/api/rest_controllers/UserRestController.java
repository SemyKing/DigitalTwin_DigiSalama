package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.PasswordUpdateRequest;
import com.example.demo.database.models.user.PasswordUpdateResponse;
import com.example.demo.database.models.user.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.database.models.utils.ValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(StringUtils.JSON_API + "/users")
public class UserRestController {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String ENTITY =  "user";


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;


	@Autowired
	private final UserRepository userRepository;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> postList(@RequestBody List<User> users) {

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			ValidationResponse response = userService.validate(user, Mapping.POST);

			RestResponse<User> responseHandler = new RestResponse<>();
			responseHandler.setBody(user);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				User userFromDatabase = userService.save(user);

				if (userFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(userFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
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

	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<User>> post(@RequestBody User user) {

		ValidationResponse response = userService.validate(user, Mapping.POST);

		RestResponse<User> responseHandler = new RestResponse<>();
		responseHandler.setBody(user);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(userFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}

	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void postById(@RequestBody User user, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<User> getAll() {
		List<User> users = userService.getAll();

		// TODO: MAYBE REMOVE
		users.sort(Comparator.comparing(User::getId));

		return users;
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

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			ValidationResponse response = userService.validate(user, Mapping.PUT);

			RestResponse<User> responseHandler = new RestResponse<>();
			responseHandler.setBody(user);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				User userFromDatabase = userService.save(user);

				if (userFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(userFromDatabase);
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
	public ResponseEntity<RestResponse<User>> putById(@RequestBody User user, @PathVariable Long id) {

		ValidationResponse response = userService.validate(user, Mapping.PUT);

		RestResponse<User> responseHandler = new RestResponse<>();
		responseHandler.setBody(user);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setBody(userFromDatabase);
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
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

					User userFromDatabase = userService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(User.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, userFromDatabase, value);
						}
					});

					ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);

					RestResponse<User> userResponse = new RestResponse<>();
					userResponse.setBody(userFromDatabase);

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
	public ResponseEntity<RestResponse<User>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(User.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, userFromDatabase, value);
			}
		});

		RestResponse<User> responseHandler = new RestResponse<>();

		ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);
		responseHandler.setBody(userFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		User patchedUser = userService.save(userFromDatabase);
		responseHandler.setBody(patchedUser);

		if (patchedUser == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}


	@PostMapping(value = "/forgot_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> forgotPassword(@RequestBody User user, HttpServletRequest request) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		if (user.getEmail() == null || user.getEmail().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "email is required");
		}

		User userFromDatabase = userService.getByEmail(user.getEmail());

		String ip = request.getRemoteAddr();
		logger.info("forgot password request was made by IP address: " + ip);

		if (userFromDatabase == null) {
			logger.info("user with email: " + user.getEmail() + " was not found");
		} else {
			String passwordUpdateToken = userService.generatePasswordUpdateToken(userFromDatabase);
			logger.info("user with email: " + user.getEmail() + " was found and password update token was created: " + passwordUpdateToken);

			//TODO: SEND EMAIL WITH RESET LINK
			// CURRENTLY WILL WORK BY CREATING POST REQUEST TO /update_password WITH passwordUpdateToken and NEW PASSWORD
		}

		return ResponseEntity.ok("Password reset link was sent to provided email");
	}

	@PostMapping(value = "/request_update_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<PasswordUpdateResponse>> requestPasswordUpdate(@RequestBody User user) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		if (user.getUsername() == null || user.getUsername().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "username is required");
		}

		if (user.getPassword() == null || user.getPassword().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "password is required");
		}

		User userFromDatabase = userService.getByUsername(user.getUsername());

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "credentials invalid");
		}

		if (!userService.getBcryptEncoder().matches(user.getPassword(), userFromDatabase.getPassword())) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "credentials invalid");
		}

		ObjectMapper objectMapper = new ObjectMapper();

		String passwordUpdateToken = userService.generatePasswordUpdateToken(userFromDatabase);

		RestResponse<PasswordUpdateResponse> responseHandler = new RestResponse<>();
		responseHandler.setBody(new PasswordUpdateResponse(passwordUpdateToken));
		responseHandler.setHttp_status(HttpStatus.OK);

		String jsonValue = "";
		try {
			jsonValue = objectMapper.writeValueAsString(new PasswordUpdateRequest());
			jsonValue = jsonValue.replaceAll("\\\\", "");
			jsonValue = ": " + jsonValue;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		responseHandler.setMessage("please create new POST request with following information and provided token" + jsonValue);

		return ResponseEntity.ok(responseHandler);
	}

	@PostMapping(value = "/update_password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest) {

		if (passwordUpdateRequest == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		if (passwordUpdateRequest.getPassword_update_token() == null || passwordUpdateRequest.getPassword_update_token().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "token is required");
		}

		if (passwordUpdateRequest.getNew_password() == null || passwordUpdateRequest.getNew_password().length() <= 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "password is required");
		}

		User userFromDatabase = userService.getByPasswordUpdateToken(passwordUpdateRequest.getPassword_update_token());
		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "token is invalid or not found");
		}

		userFromDatabase.setPassword(userService.getBcryptEncoder().encode(passwordUpdateRequest.getNew_password()));
		userFromDatabase.setPassword_update_token(null);
		userService.save(userFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).body("password updated successfully");
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> deleteList(@RequestBody List<User> users) {

		boolean errorOccurred = false;

		List<RestResponse<User>> responseList = new ArrayList<>();

		for (User user : users) {
			ValidationResponse response = userService.validate(user, Mapping.DELETE);

			RestResponse<User> responseHandler = new RestResponse<>();
			responseHandler.setBody(user);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					userService.delete(user);

					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " deleted successfully");
				} catch (Exception e) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());

					errorOccurred = true;
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
	public ResponseEntity<RestResponse<User>> deleteById(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			userService.delete(userFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<User> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(userFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			User user = new User();
			user.setUsername("username_" + i);
			user.setPassword(userService.getBcryptEncoder().encode("password"));
			user.setFirst_name("First Name_" + i);
			user.setLast_name("Last Name_" + i);
			user.setEmail("test@test" + i + ".com");
			user.setRole(roleRepository.findByName(StringUtils.ROLE_USER));

			List<Organisation> organisations = organisationService.getAll();

			if (organisations.size() > 0) {
				user.setOrganisation(organisations.get(0));
			}

			userService.save(user);
		}
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		userService.deleteAll();
	}
}
