package com.example.demo.api.ui_controllers;

import com.example.demo.database.models.ApplicationSettings;
import com.example.demo.database.services.ApplicationSettingsService;
import com.example.demo.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@SessionAttributes("settings")
@RequestMapping(Constants.UI_API + "/settings")
public class SettingsController {

	@Autowired
	private final ApplicationSettingsService settingsService;


	@GetMapping({"", "/"})
	public String get(Model model) {

		ApplicationSettings settings = settingsService.getApplicationSettings();
		String ENTITY = "application_settings";
		model.addAttribute(ENTITY, settings);

		return "settings/application_settings_page";
	}

	@PostMapping("/update")
	public String update(@ModelAttribute ApplicationSettings settings, Model model) {
		if (settings == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"provided NULL entity");
			return Constants.ERROR_PAGE;
		}

		settingsService.save(settings);

		return Constants.REDIRECT + Constants.UI_API + "/";
	}
}
