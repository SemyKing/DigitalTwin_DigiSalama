package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.PasswordUpdateRequest;
import com.example.demo.database.models.user.PasswordUpdateResponse;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.FieldReflectionUtils;
import com.example.demo.utils.StringUtils;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(StringUtils.JSON_API + "/users")
public class UserRestController {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String ENTITY = "user";


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> postList(@RequestBody List<User> users) {

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
				User userFromDatabase = userService.save(user);

				if (userFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(userFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");
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

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(userFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			return ResponseEntity.ok(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		boolean errorOccurred = false;

		List<RestResponse<?>> responseList = new ArrayList<>();

		for (Map<String, Object> changes : changesList) {

			changes.remove("password");

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

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

					RestResponse<User> userResponse = new RestResponse<>();
					userResponse.setBody(userFromDatabase);

					ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);


					if (!response.isValid()) {
						userResponse.setHttp_status(HttpStatus.BAD_REQUEST);
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
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.ok(responseList);
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

		RestResponse<User> restResponse = new RestResponse<>();

		ValidationResponse response = userService.validate(userFromDatabase, Mapping.PATCH);
		restResponse.setBody(userFromDatabase);

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

		return ResponseEntity.ok("password updated successfully");
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<User>>> deleteList(@RequestBody List<User> users) {

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

		try {
			userService.delete(userFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not delete " + ENTITY + " with ID: " + id);
		}

		RestResponse<User> restResponse = new RestResponse<>();

		restResponse.setBody(userFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		return ResponseEntity.ok(restResponse);
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
