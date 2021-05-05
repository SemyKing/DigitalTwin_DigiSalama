package com.example.demo.api.ui_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.Role;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.user.UserPassword;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
@RequestMapping(Constants.UI_API + "/users")
public class UserController {

	private final String ENTITY = "user";
	private boolean returnToProfile = false;

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;


	@Autowired
	private final RoleRepository roleRepository;

	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;



	@GetMapping({"", "/"})
	public String getAll(Model model) {
//		ResponseEntity<User[]> response = this.restTemplate.getForEntity(USER_JSON_URL, User[].class);
//
//		List<User> users = new ArrayList<>();
//
//		if (response.getStatusCode() == HttpStatus.OK) {
//			if (response.getBody() != null) {
//				users.addAll(Arrays.asList(response.getBody()));
//				users.sort(Comparator.comparing(User::getId));
//				model.addAttribute(ENTITY_LIST, users);
//			}
//		} else {
//			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, response.getStatusCode());
//			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getBody());
//			return StringUtils.ERROR_PAGE;
//		}

		List<User> users = userService.getAll();
		model.addAttribute("users", users);

		return "user/users_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
//		ResponseEntity<User> response = this.restTemplate.getForEntity(USER_JSON_URL + "/" + id, User.class);
//
//		User user = null;
//
//		if (response.getStatusCode() == HttpStatus.OK) {
//			if (response.getBody() != null) {
//				user = response.getBody();
//			}
//		} else {
//			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, response.getStatusCode());
//			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getBody());
//			return StringUtils.ERROR_PAGE;
//		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, userFromDatabase);
		return "user/user_details_page";
	}


	@GetMapping("/new")
	public String newForm(Model model) {
//		ResponseEntity<Organisation[]> response = this.restTemplate.getForEntity(ORGANISATION_JSON_URL, Organisation[].class);
//
//		List<Organisation> organisations = new ArrayList<>();
//
//		if (response.getStatusCode() == HttpStatus.OK) {
//			if (response.getBody() != null) {
//				organisations.addAll(Arrays.asList(response.getBody()));
//			}
//		} else {
//			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, response.getStatusCode());
//			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getBody());
//			return StringUtils.ERROR_PAGE;
//		}

		model.addAttribute(ENTITY, new User());

		List<Role> roles = roleRepository.findAll();
		model.addAttribute("roles", roles);

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "user/new_user_page";
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
//		if (!alreadySet) {
//			ResponseEntity<User> userResponseEntity = this.restTemplate.getForEntity(USER_JSON_URL + "/" + id, User.class);
//
//			User user = null;
//
//			if (userResponseEntity.getStatusCode() == HttpStatus.OK) {
//				if (userResponseEntity.getBody() != null) {
//					user = userResponseEntity.getBody();
//				}
//			} else {
//				model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, userResponseEntity.getStatusCode());
//				model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, userResponseEntity.getBody());
//				return StringUtils.ERROR_PAGE;
//			}
//
//			model.addAttribute(ENTITY, user);
//		}
//
//		ResponseEntity<Organisation[]> organisationsResponseEntity = this.restTemplate.getForEntity(ORGANISATION_JSON_URL, Organisation[].class);
//
//		List<Organisation> organisations = new ArrayList<>();
//
//		if (organisationsResponseEntity.getStatusCode() == HttpStatus.OK) {
//			if (organisationsResponseEntity.getBody() != null) {
//				organisations.addAll(Arrays.asList(organisationsResponseEntity.getBody()));
//			}
//		} else {
//			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, organisationsResponseEntity.getStatusCode());
//			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, organisationsResponseEntity.getBody());
//			return StringUtils.ERROR_PAGE;
//		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, userFromDatabase);

		List<Role> roles = roleRepository.findAll();
		model.addAttribute("roles", roles);

		List<Organisation> organisations = organisationService.getAll();
		model.addAttribute("organisations", organisations);

		return "user/edit_user_page";
	}


	// PASSWORD CHANGE FORM
	@GetMapping("/{id}/change_password")
	public String changePasswordForm(@PathVariable Long id, Model model, HttpServletRequest request) {
		model.addAttribute("userId", id);
		model.addAttribute("password", new UserPassword());

		if (request.getHeader("Referer").contains("/profile")) {
			returnToProfile = true;
		}

		return "user/change_password_page";
	}


	// UPDATE PASSWORD
	@PostMapping("/{id}/update_password")
	public String updatePassword(@ModelAttribute UserPassword userPassword, @PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			ResponseEntity<User> response = this.restTemplate.postForEntity(USER_JSON_URL + "/" + +id + "/update_password", userPassword, User.class);
//
//		} catch (HttpClientErrorException e) {
//			String responseString = e.getResponseBodyAsString();
//			ObjectMapper mapper = new ObjectMapper()
//					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//			try {
//				RestExceptionsHandler result = mapper.readValue(responseString, RestExceptionsHandler.class);
//
//				// METHOD NOT ACCEPTABLE
//				if (result.getStatus() == 406) {
//					model.addAttribute("password", userPassword);
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//
//					return changePasswordForm(id, model, true);
//				} else {
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//					return StringUtils.ERROR_PAGE;
//				}
//
//			} catch (JsonProcessingException jsonProcessingException) {
//				jsonProcessingException.printStackTrace();
//			}
//		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute("userId", id);
		model.addAttribute("password", userPassword);


		if (!userService.isPasswordCorrect(userFromDatabase, userPassword.getCurrent_password())) {
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Current password invalid");
			return "user/change_password_page";
		}

		if (!userPassword.getNew_password_1().equals(userPassword.getNew_password_2())) {
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Passwords don't match");
			return "user/change_password_page";
		}

		userFromDatabase.setPassword(userService.getBcryptEncoder().encode(userPassword.getNew_password_1()));
		userService.save(userFromDatabase);

		eventHistoryLogService.addUserLog("change password", "password changed for user:\n" + userFromDatabase);

		model.addAttribute(Constants.SUCCESS_MESSAGE_ATTRIBUTE, "Password changed");

		if (returnToProfile) {
			returnToProfile = false;
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE_ATTRIBUTE, "Password changed successfully");
			return Constants.REDIRECT + Constants.UI_API + "/profile";
		}

		return Constants.REDIRECT + Constants.UI_API + "/users/" + id + "/edit";
	}


	@PostMapping({"", "/"})
	public String post(@ModelAttribute User user, Model model) {
//		try {
//			ResponseEntity<User> response = this.restTemplate.postForEntity(USER_JSON_URL, user, User.class);
//
//			// IF ERROR -> ADD ERROR MESSAGE TO MODEL AND SHOW ERROR PAGE
//		} catch (HttpClientErrorException e) {
//			String responseString = e.getResponseBodyAsString();
//			ObjectMapper mapper = new ObjectMapper()
//					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//			try {
//				RestExceptionsHandler result = mapper.readValue(responseString, RestExceptionsHandler.class);
//
//				// METHOD NOT ALLOWED
//				if (result.getStatus() == 405) {
//					model.addAttribute(ENTITY, user);
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//
//					return newForm(model, true);
//				} else {
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//					return StringUtils.ERROR_PAGE;
//				}
//
//			} catch (JsonProcessingException jsonProcessingException) {
//				jsonProcessingException.printStackTrace();
//			}
//		}

		user = new FieldReflectionUtils<User>().getEntityWithEmptyStringValuesAsNull(user);

		ValidationResponse response = userService.validate(user, Mapping.POST);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addUserLog("create " + ENTITY, ENTITY + " created:\n" + userFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/users";
		}
	}


	@PostMapping("/update")
	public String put(@ModelAttribute User user, Model model, HttpServletRequest request) {
//		try {
//			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(USER_JSON_URL + "/" + user.getId());
//
//			HttpEntity<User> entity = new HttpEntity<>(user);
//
//			ResponseEntity<User> response = restTemplate.exchange(
//					builder.toUriString(),
//					HttpMethod.PUT,
//					entity,
//					User.class);
//
//		} catch (HttpClientErrorException e) {
//			String responseString = e.getResponseBodyAsString();
//			ObjectMapper mapper = new ObjectMapper()
//					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//			try {
//				RestExceptionsHandler result = mapper.readValue(responseString, RestExceptionsHandler.class);
//
//				// METHOD NOT ALLOWED
//				if (result.getStatus() == 405) {
//					model.addAttribute(ENTITY, user);
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//
//					return editForm(user.getId(), model, true);
//				} else {
//					model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, result.getMessage());
//					return StringUtils.ERROR_PAGE;
//				}
//
//			} catch (JsonProcessingException jsonProcessingException) {
//				jsonProcessingException.printStackTrace();
//			}
//		}

		String oldUser = userService.getById(user.getId()).toString();

		user = new FieldReflectionUtils<User>().getEntityWithEmptyStringValuesAsNull(user);

		ValidationResponse response = userService.validate(user, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}


		User userFromDatabase = userService.save(user);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addUserLog("update " + ENTITY, ENTITY + " update from:\n" + oldUser + "\nto:\n" + userFromDatabase);

			if (request.getHeader("Referer").contains("/profile")) {
				return Constants.REDIRECT + Constants.UI_API + "/";
			}

			return Constants.REDIRECT + Constants.UI_API + "/users/" + userFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		ValidationResponse response = userService.validate(userFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		userService.delete(userFromDatabase);

		eventHistoryLogService.addUserLog("delete " + ENTITY, ENTITY + " deleted:\n" + userFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/users";
	}
}