package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("file")
@RequestMapping(StringUtils.UI_API + "/files")
public class FileController {

	private final String ENTITY =       "file";

	@Autowired
	private final FileService fileService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<FileDB> fileDBS = fileService.getAll();
		model.addAttribute("files", fileDBS);

		return "vehicle/files/files_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}
		model.addAttribute(ENTITY, fileFromDatabase);
		model.addAttribute("fileType", fileFromDatabase.getType());

		if (fileFromDatabase.getType().contains("image")) {
			model.addAttribute("isImage", true);
			model.addAttribute("image", fileService.getImageData(fileFromDatabase.getData()));
		}

		return "vehicle/files/file_details_page";
	}


	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Please select a file to upload.");
			return StringUtils.REDIRECT + StringUtils.UI_API + "/files";
		}

		try {
			fileService.save(file);
			redirectAttributes.addFlashAttribute(StringUtils.SUCCESS_MESSAGE_ATTRIBUTE, "File successfully uploaded");
			return StringUtils.REDIRECT + StringUtils.UI_API + "/files";

		} catch (IOException e) {
			System.err.println("FILE SAVE ERROR");
			e.printStackTrace();

			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Error occurred while uploading file");
			return StringUtils.ERROR_PAGE;
		}
	}


	// DELETE FILE
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		fileService.delete(fileFromDatabase);

		return StringUtils.REDIRECT + StringUtils.UI_API + "/files";
	}
}
