package com.example.demo.api.rest_controllers;

import com.example.demo.api.handlers.RestResponseHandler;
import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.user.UserPassword;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
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
public class UserRestController {

	private final String ENTITY =       "user";
	private final String ENTITY_LIST =  "users";

	private final String ENTITY_URL =  			StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String ENTITY_URL_WITH_ID =	StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID;
	private final String UPDATE_PASSWORD_URL =	StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID + "/update_password";


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;



	@PostMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> postEntity(@RequestBody User user) {
		ValidationResponse response = userService.validate(user, Mapping.POST_API);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "failed to save " + ENTITY + " in database");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(userFromDatabase);
		}
	}

	@PostMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public void postEntityWithID(@RequestBody User user, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}



	@GetMapping(value = ENTITY_URL, produces = StringUtils.APPLICATION_JSON)
	public List<User> getEntityList() {
		List<User> users = userService.getAll();
		users.sort(Comparator.comparing(User::getId));

		return users;
	}

	@GetMapping(value = ENTITY_URL_WITH_ID, produces = StringUtils.APPLICATION_JSON)
	public User getEntityWithID(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return userFromDatabase;
	}



	@PutMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public User putEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PutMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> putEntity(@RequestBody User user, @PathVariable Long id) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		user.setId(id);

		ValidationResponse response = userService.validate(user, Mapping.PUT_API);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		user.setId(userFromDatabase.getId());

		userService.save(user);

		return ResponseEntity.status(HttpStatus.OK).body(user);
	}



	@PatchMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateModifyEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PatchMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> updateModifyEntity(@RequestBody User user, @PathVariable Long id) {

		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content");
		}

		ValidationResponse response = userService.validate(user, Mapping.PATCH_API);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		user.setId(userFromDatabase.getId());

		userService.save(user);

		return ResponseEntity.status(HttpStatus.OK).body(user);
	}


	@PostMapping(value = UPDATE_PASSWORD_URL, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> updatePassword(@RequestBody UserPassword userPassword, @PathVariable Long id) {

		if (!userService.isPasswordCorrect(id, userPassword.getCurrentPassword())) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "provided data was incorrect");
		}

		User userFromDatabase = userService.getById(id);
		userFromDatabase.setPassword(StringUtils.generateHashFromString(userPassword.getNewPassword1()));

		userService.save(userFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).body(userFromDatabase);
	}



	@DeleteMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@DeleteMapping(value = ENTITY_URL_WITH_ID)
	public ResponseEntity<RestResponseHandler<User>> deleteEntity(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			userService.delete(userFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponseHandler<User> responseHandler = new RestResponseHandler<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(userFromDatabase);
		responseHandler.setHttpStatus(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}





	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			User user = new User();
			user.setUsername("username_" + i);
			user.setPassword(userService.getBcryptEncoder().encode("password"));
			user.setFirstName("First Name_" + i);
			user.setLastName("Last Name_" + i);
			user.setEmail("test@test" + i + ".com");
			user.setRole(roleRepository.findByName(StringUtils.ROLE_USER));

			List<Organisation> organisations = organisationService.getAll();

			if (organisations.size() > 0) {
				user.setOrganisation(organisations.get(0));
			}

			userService.save(user);
		}
	}

	// FOR TESTING
	@DeleteMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/delete_all")
	public void deleteAll() {
		userService.deleteAll();
	}
}
