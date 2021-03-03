package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.Role;
import com.example.demo.database.models.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
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

	// CREATE
	private final String POST_ENTITY_URL =  StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// GET
	private final String GET_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/REPLACE
	private final String PUT_ENTITY_URL =   StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// UPDATE/MODIFY
	private final String PATCH_ENTITY_URL = StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;

	// DELETE
	private final String DELETE_ENTITY_URL =StringUtils.JSON_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;





	@PostMapping(value = POST_ENTITY_URL, consumes = "application/json")
	public ResponseEntity<User> postEntity(@RequestBody User user) {

		// VALIDATE ENTITY

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to save entity");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(userFromDatabase);
		}
	}

	@PostMapping(value = POST_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public void postEntityWithID(@RequestBody User user, @PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}
	}



	@GetMapping(value = GET_ENTITY_URL, produces = "application/json")
	public List<User> getEntityList() {
		List<User> users = userService.getAll();

		// SORT etc...
		users.sort(Comparator.comparing(User::getId));

		return users;
	}

	@GetMapping(value = GET_ENTITY_URL + StringUtils.ID, produces = "application/json")
	public User getEntityWithID(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		return userFromDatabase;
	}



	@PutMapping(value = PUT_ENTITY_URL, consumes = "application/json")
	public User updateReplaceEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
	}

	@PutMapping(value = PUT_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<User> updateReplaceEntity(@RequestBody User user, @PathVariable Long id) {
		System.out.println("PUT ENTITY");

		// VALIDATE ENTITY
		System.out.println(user);

		// EMPTY BODY
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		user.setId(userFromDatabase.getId());

		userService.save(user);

		return ResponseEntity.status(HttpStatus.OK).body(user);
	}



	@PatchMapping(value = PATCH_ENTITY_URL, consumes = "application/json")
	public void updateModifyEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@PatchMapping(value = PATCH_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<User> updateModifyEntity(@RequestBody User user, @PathVariable Long id) {

		// VALIDATE ENTITY
		System.out.println(user);

		// EMPTY BODY
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No content");
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		user.setId(userFromDatabase.getId());

		userService.save(user);

		return ResponseEntity.status(HttpStatus.OK).body(user);
	}



	@DeleteMapping(value = DELETE_ENTITY_URL, consumes = "application/json")
	public void deleteEntityList(@RequestBody List<User> users) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@DeleteMapping(value = DELETE_ENTITY_URL + StringUtils.ID, consumes = "application/json")
	public ResponseEntity<String> deleteEntity(@PathVariable Long id) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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
