package com.example.demo.api.controllers;

import com.example.demo.api.controllers.exceptions.RestExceptionsHandler;
import com.example.demo.database.models.*;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class UserController {

	private final String ENTITY =       "user";
	private final String ENTITY_LIST =  "users";

	// CREATE
	private final String ENTITY_UI_URL =  		StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String ENTITY_UI_URL_WITH_ID =StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID;
	private final String USER_JSON_URL =  		StringUtils.LOCAL_HOST + StringUtils.JSON_API + "/" + ENTITY_LIST;
	private final String ORGANISATION_JSON_URL =StringUtils.LOCAL_HOST + StringUtils.JSON_API + "/organisations";


	private final String CHANGE_PASSWORD_URL = StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID + "/change_password";
	private final String UPDATE_PASSWORD_URL = StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST + StringUtils.ID + "/update_password";


	private final String CHANGE_PASSWORD_PAGE = "user/change_password_page";
	private final String EDIT_ENTITY_PAGE =     "user/edit_user_page";
	private final String NEW_ENTITY_PAGE =      "user/new_user_page";
	private final String ENTITY_DETAILS_PAGE =  "user/user_details_page";
	private final String ENTITY_LIST_PAGE =     "user/users_list_page";


	@Autowired
	private final RestTemplate restTemplate;

	@Autowired
	private final RoleRepository roleRepository;


	@GetMapping(ENTITY_UI_URL)
	public String getAllUsers(Model model) {
		ResponseEntity<User[]> response = this.restTemplate.getForEntity(USER_JSON_URL, User[].class);

		List<User> users = new ArrayList<>();

		if (response.getStatusCode() == HttpStatus.OK) {
			if (response.getBody() != null) {
				users.addAll(Arrays.asList(response.getBody()));
			}
		}

		users.sort(Comparator.comparing(User::getId));

		model.addAttribute(ENTITY_LIST, users);
		return ENTITY_LIST_PAGE;
	}

	@GetMapping(ENTITY_UI_URL_WITH_ID)
	public String getUserById(@PathVariable Long id, Model model) {
		ResponseEntity<User> response = this.restTemplate.getForEntity(USER_JSON_URL + "/" + id, User.class);

		User user = null;

		if (response.getStatusCode() == HttpStatus.OK) {
			if (response.getBody() != null) {
				user = response.getBody();
			}
		}

		model.addAttribute(ENTITY, user);
		return ENTITY_DETAILS_PAGE;
	}

	// NEW USER FORM
	@GetMapping(ENTITY_UI_URL + StringUtils.NEW)
	public String newUserForm(Model model, boolean userAlreadySet) {
		ResponseEntity<Organisation[]> response = this.restTemplate.getForEntity(ORGANISATION_JSON_URL, Organisation[].class);

		List<Organisation> organisations = new ArrayList<>();

		if (response.getStatusCode() == HttpStatus.OK) {
			if (response.getBody() != null) {
				organisations.addAll(Arrays.asList(response.getBody()));
			}
		}

		List<Role> roles = roleRepository.findAll();

		if (!userAlreadySet) {
			model.addAttribute(ENTITY, new User());
		}

		model.addAttribute("organisations", organisations);
		model.addAttribute("roles", roles);
		return NEW_ENTITY_PAGE;
	}

	// EDIT USER FORM
	@GetMapping(ENTITY_UI_URL_WITH_ID + StringUtils.EDIT)
	public String editUserForm(@PathVariable Long id, Model model, boolean userAlreadySet) {

		if (!userAlreadySet) {
			ResponseEntity<User> userResponseEntity = this.restTemplate.getForEntity(USER_JSON_URL + "/" + id, User.class);

			User user = null;

			if (userResponseEntity.getStatusCode() == HttpStatus.OK) {
				if (userResponseEntity.getBody() != null) {
					user = userResponseEntity.getBody();
				}
			} else {
				System.out.println("GOT STATUS CODE: " + userResponseEntity.getStatusCode());
			}

			model.addAttribute(ENTITY, user);
		}

		ResponseEntity<Organisation[]> organisationsResponseEntity = this.restTemplate.getForEntity(ORGANISATION_JSON_URL, Organisation[].class);

		List<Organisation> organisations = new ArrayList<>();

		if (organisationsResponseEntity.getStatusCode() == HttpStatus.OK) {
			if (organisationsResponseEntity.getBody() != null) {
				organisations.addAll(Arrays.asList(organisationsResponseEntity.getBody()));
			}
		}

		List<Role> roles = roleRepository.findAll();

		model.addAttribute("organisations", organisations);
		model.addAttribute("roles", roles);
		return EDIT_ENTITY_PAGE;
	}

	// PASSWORD CHANGE FORM
	@GetMapping(CHANGE_PASSWORD_URL)
	public String changeUserPasswordForm(@PathVariable Long id, Model model, boolean dataAlreadySet) {

		if (!dataAlreadySet) {
			ResponseEntity<User> userResponseEntity = this.restTemplate.getForEntity(USER_JSON_URL + "/" + id, User.class);

			User user = null;

			if (userResponseEntity.getStatusCode() == HttpStatus.OK) {
				if (userResponseEntity.getBody() != null) {
					user = userResponseEntity.getBody();
				}
			} else {
				System.out.println("GOT NON 'OK' STATUS CODE: " + userResponseEntity.getStatusCode());
			}

			model.addAttribute(ENTITY, user);
		}

		return EDIT_ENTITY_PAGE;
	}


	// POST USER
	@PostMapping(ENTITY_UI_URL)
	public String postUser(@ModelAttribute User user, Model model) {
		try {
			ResponseEntity<User> response = this.restTemplate.postForEntity(USER_JSON_URL, user, User.class);

			// IF ERROR -> ADD ERROR MESSAGE TO MODEL AND SHOW ERROR PAGE
		} catch (HttpClientErrorException e) {
			String responseString = e.getResponseBodyAsString();
			ObjectMapper mapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			try {
				RestExceptionsHandler result = mapper.readValue(responseString, RestExceptionsHandler.class);

				// METHOD NOT ALLOWED
				if (result.getStatus() == 405) {
					model.addAttribute(ENTITY, user);
					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());

					return newUserForm(model, true);
				}

			} catch (JsonProcessingException jsonProcessingException) {
				jsonProcessingException.printStackTrace();
			}
		}

		// -> IF OK
		return StringUtils.REDIRECT_URL + ENTITY_UI_URL; // -> USERS LIST VIEW
	}

	// UPDATE USER
	@PostMapping(ENTITY_UI_URL_WITH_ID + StringUtils.UPDATE)
	public String putUser(@ModelAttribute User user, Model model) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(USER_JSON_URL + "/" + user.getId());

			HttpEntity<User> entity = new HttpEntity<>(user);

			ResponseEntity<User> response = restTemplate.exchange(
					builder.toUriString(),
					HttpMethod.PUT,
					entity,
					User.class);

			System.out.println(response.getStatusCode());

		} catch (HttpClientErrorException e) {
			String responseString = e.getResponseBodyAsString();
			ObjectMapper mapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			try {
				RestExceptionsHandler result = mapper.readValue(responseString, RestExceptionsHandler.class);

				// METHOD NOT ALLOWED
				if (result.getStatus() == 405) {
					model.addAttribute(ENTITY, user);
					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());

					return editUserForm(user.getId(), model, true);
				}

			} catch (JsonProcessingException jsonProcessingException) {
				jsonProcessingException.printStackTrace();
			}
		}

		return StringUtils.REDIRECT_URL + ENTITY_UI_URL; // -> USERS LIST VIEW
	}

}
