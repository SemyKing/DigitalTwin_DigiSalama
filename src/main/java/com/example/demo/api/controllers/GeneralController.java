package com.example.demo.api.controllers;

import com.example.demo.database.models.User;
import com.example.demo.database.models.UserPassword;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class GeneralController {

	private final String LOGIN_URL =    StringUtils.UI_API + StringUtils.FORWARD_SLASH + "/login";

	private final String LOGIN_PAGE =   "login_page";
	private final String HOME_PAGE =    "home_page";


	@Autowired
	private final UserService userService;


	@GetMapping(StringUtils.FORWARD_SLASH)
	public String homePage(Model model) {

//		if (SessionData.getCurrentUserId() == null) {
//			model.addAttribute("userPassword", new UserPassword());
//			return LOGIN_PAGE;
//		}

		return HOME_PAGE;
	}


	@PostMapping(LOGIN_URL)
	public String loginFromUI(@ModelAttribute UserPassword userPassword, Model model) {

		model.addAttribute("userPassword", userPassword);

		if (userPassword.getUsername() == null || userPassword.getUsername().length() <= 0) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Username is required");
			return LOGIN_PAGE;
		}

		if (userPassword.getCurrentPassword() == null || userPassword.getCurrentPassword().length() <= 0) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Password is required");
			return LOGIN_PAGE;
		}

		User user = userService.getByUsername(userPassword.getUsername());

		if (user == null) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Invalid credentials");
			return LOGIN_PAGE;
		}

		if (!StringUtils.generateHashFromString(userPassword.getCurrentPassword()).equals(user.getPasswordHash())) {
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Invalid credentials");
			return LOGIN_PAGE;
		}

//		SessionData.setCurrentUserId(user.getId());

		return StringUtils.REDIRECT_URL + StringUtils.FORWARD_SLASH;
	}

	@ExceptionHandler(RuntimeException.class)
	public String showException(Exception ex, Model model) {
		System.out.println("-------GENERAL EXCEPTION HANDLER");
		model.addAttribute(StringUtils.EXCEPTION_ATTRIBUTE, ex.getMessage());
		return StringUtils.ERROR_PAGE;
	}
}
