package com.example.demo.api.controllers;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("event_history_log")
@RequestMapping(Constants.UI_API + "/event_history_logs")
public class EventHistoryLogController {

	private final String ENTITY = "event_history_log";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<EventHistoryLog> eventHistoryLogList = eventHistoryLogService.getAll();
		eventHistoryLogList.sort(Comparator.comparing(EventHistoryLog::getId).reversed());

		model.addAttribute("event_history_log_list", eventHistoryLogList);

		return "event_history_log/event_history_log_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		EventHistoryLog eventHistoryLogFromDatabase = eventHistoryLogService.getById(id);

		if (eventHistoryLogFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, eventHistoryLogFromDatabase);

		return "event_history_log/event_history_log_details_page";
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		EventHistoryLog eventHistoryLogFromDatabase = eventHistoryLogService.getById(id);

		if (eventHistoryLogFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		eventHistoryLogService.delete(eventHistoryLogFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/event_history_logs";
	}
}
