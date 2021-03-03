package com.example.demo.api.controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.UserPassword;
import com.example.demo.database.models.Role;
import com.example.demo.database.models.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class UserController {

	private final String ENTITY =       "user";
	private final String ENTITY_LIST =  "users";

	private final String GET_ALL_URL =      StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY_LIST;
	private final String GET_BY_ID_URL =    StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String NEW_ENTITY_URL =   StringUtils.UI_API + StringUtils.NEW +      StringUtils.FORWARD_SLASH + ENTITY;
	private final String SAVE_URL =         StringUtils.UI_API + StringUtils.SAVE +     StringUtils.FORWARD_SLASH + ENTITY;
	private final String EDIT_URL =         StringUtils.UI_API + StringUtils.EDIT +     StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String UPDATE_URL =       StringUtils.UI_API + StringUtils.UPDATE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;
	private final String DELETE_URL =       StringUtils.UI_API + StringUtils.DELETE +   StringUtils.FORWARD_SLASH + ENTITY + StringUtils.ID;

	private final String CHANGE_PASSWORD_URL =       StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + "/change_password" + StringUtils.ID;
	private final String UPDATE_PASSWORD_URL =       StringUtils.UI_API + StringUtils.FORWARD_SLASH + ENTITY + "/update_password" + StringUtils.ID;


	private final String CHANGE_PASSWORD_PAGE = "user/change_password_page";
	private final String EDIT_ENTITY_PAGE =     "user/edit_user_page";
	private final String NEW_ENTITY_PAGE =      "user/new_user_page";
	private final String ENTITY_DETAILS_PAGE =  "user/user_details_page";
	private final String ENTITY_LIST_PAGE =     "user/users_list_page";


	@Autowired
	private final UserService userService;

	@Autowired
	private final OrganisationService organisationService;

	@Autowired
	private final RoleRepository roleRepository;


	@GetMapping(GET_ALL_URL)
	public String getAllUsers(Model model) {

		System.out.println("GET ALL USERS");

		List<User> users = userService.getAll();
		users.sort(Comparator.comparing(User::getId));

		model.addAttribute(ENTITY_LIST, users);
		return ENTITY_LIST_PAGE;
	}


	@GetMapping(GET_BY_ID_URL)
	public String getUserById(@PathVariable Long id, Model model) {
		User user = userService.getById(id);

		if (user == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

//		boolean changesAllowed = userService.isUserOrganisationOrSystemAdmin(getCurrentUserId());

//		System.out.println("changesAllowed: " + changesAllowed);

		model.addAttribute(ENTITY, user);
//		model.addAttribute(StringUtils.CHANGES_ALLOWED_ATTRIBUTE, changesAllowed);

		return ENTITY_DETAILS_PAGE;
	}


	@GetMapping(NEW_ENTITY_URL)
	public String newUserForm(Model model) {

		System.out.println("NEW USER");

		List<Organisation> organisations = organisationService.getAll();
		organisations.sort(Comparator.comparing(Organisation::getName));

		model.addAttribute("organisations", organisations);
		model.addAttribute(ENTITY, new User());

		return NEW_ENTITY_PAGE;
	}


	@PostMapping(SAVE_URL)
	public String saveUserFromUI(@ModelAttribute User user, Model model) {

		System.out.println("SAVE USER");

		if (user == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, ENTITY + " object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " object is required");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println(user);

		userService.save(user);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}

	@PostMapping(CHANGE_PASSWORD_URL)
	public String changePassword(@PathVariable Long id, Model model) {

		System.out.println("CHANGE PASSWORD");

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "ID object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID object is required");
			return StringUtils.ERROR_PAGE;
		}

		User user = userService.getById(id);

		if (user == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + "  with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println(user);

		model.addAttribute("password", new UserPassword());
		model.addAttribute("userId", id);

		return CHANGE_PASSWORD_PAGE;
	}


	@PostMapping(UPDATE_PASSWORD_URL)
	public String updatePassword(@ModelAttribute UserPassword password, @PathVariable Long id, Model model) {

		model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "");
		model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "");

		if (password == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Password object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Password object is required");
			return CHANGE_PASSWORD_PAGE;
		}

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "ID object is null");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID object is required");
			return StringUtils.ERROR_PAGE;
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + "  with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		String currentHashedPassword = StringUtils.generateHashFromString(password.getCurrentPassword());
		String newHashedPassword = StringUtils.generateHashFromString(password.getNewPassword1());

		if (!currentHashedPassword.equals(userFromDatabase.getPasswordHash())) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Current Password is incorrect");
			model.addAttribute("password", password);
			model.addAttribute("userId", id);
			return CHANGE_PASSWORD_PAGE;
		}

		if (!password.getNewPassword1().equals(password.getNewPassword2())) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Passwords do not match");
			model.addAttribute("password", password);
			model.addAttribute("userId", id);
			return CHANGE_PASSWORD_PAGE;
		}

		userFromDatabase.setPasswordHash(newHashedPassword);

		userService.save(userFromDatabase);

		return StringUtils.REDIRECT_URL + "/api1/user/" + userFromDatabase.getId();
	}


	@GetMapping(EDIT_URL)
	public String editUserForm(@PathVariable Long id, Model model) {
		System.out.println("EDIT USER");

		User user = userService.getById(id);

		if (user == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + "  with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		List<Organisation> organisations = organisationService.getAll();
		organisations.sort(Comparator.comparing(Organisation::getName));

		List<Role> roles = roleRepository.findAll();

		System.out.println(user);

		model.addAttribute(ENTITY, user);
		model.addAttribute("organisations", organisations);
		model.addAttribute("roles", roles);

		return EDIT_ENTITY_PAGE;
	}


	@PostMapping(UPDATE_URL)
	public String updateUserFromUI(@ModelAttribute User user, @PathVariable Long id, Model model) {
		System.out.println("UPDATE USER");

		if (id == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "ID missing");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		System.out.println(user);

		userService.save(user);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}


	@PostMapping(DELETE_URL)
	public String deleteUserFromUI(@PathVariable Long id, Model model) {
		User userFromDatabase = userService.getById(id);

		if (userFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Invalid ID");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID: '" + id + "' not found");
			return StringUtils.ERROR_PAGE;
		}

		userService.delete(userFromDatabase);

		return StringUtils.REDIRECT_URL + GET_ALL_URL;
	}
}
