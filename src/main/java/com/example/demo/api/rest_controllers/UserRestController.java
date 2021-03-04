package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
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


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;



	@PostMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> postEntity(@RequestBody User user) {
		ValidationResponse response = userService.validateUser(user);

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
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "resource already exists");
		}
	}



	@GetMapping(value = ENTITY_URL, produces = StringUtils.APPLICATION_JSON)
	public List<User> getEntityList() {
		List<User> users = userService.getAll();

		// SORT etc...
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
	public User updateReplaceEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PutMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> updateReplaceEntity(@RequestBody User user, @PathVariable Long id) {

		System.out.println("REST PUT");
		System.out.println(user);

		// EMPTY BODY
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = userService.validateUser(user);

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



	@PatchMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void updateModifyEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@PatchMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<User> updateModifyEntity(@RequestBody User user, @PathVariable Long id) {
		// EMPTY BODY
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content");
		}

		ValidationResponse response = userService.validateUser(user);

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



	@DeleteMapping(value = ENTITY_URL, consumes = StringUtils.APPLICATION_JSON)
	public void deleteEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
	}

	@DeleteMapping(value = ENTITY_URL_WITH_ID, consumes = StringUtils.APPLICATION_JSON)
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		userService.delete(userFromDatabase);

		return ResponseEntity.status(HttpStatus.OK).build();
	}





	// FOR TESTING
	@PostMapping(StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + "/populate_with_test_data")
	public void populateWithTestData() {
		for (int i = 1; i < 11; i++) {
			User user = new User();
			user.setUsername("username_" + i);
			user.setPasswordHash(StringUtils.generateHashFromString("password"));
			user.setFirstName("First Name_" + i);
			user.setLastName("Last Name_" + i);
			user.setEmail("test@test_" + i + ".com");
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
