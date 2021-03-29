package com.example.demo.api.controllers.vehicle;

import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.database.models.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	private final String ENTITY = "file";

	@Autowired
	private final FileService fileService;

	@Autowired
	private final VehicleService vehicleService;


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
		model.addAttribute("fileType", fileFromDatabase.getFile_type());


		if (fileFromDatabase.getFile_type() != null && fileFromDatabase.getFile_type().contains("image")) {
			model.addAttribute("isImage", true);
			model.addAttribute("image", fileService.getImageData(fileFromDatabase.getData()));
		}

		return "vehicle/files/file_details_page";
	}


	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile multipartFile, Model model, RedirectAttributes redirectAttributes) {

		if (multipartFile.isEmpty()) {
			redirectAttributes.addFlashAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Please select a file to upload.");
			return StringUtils.REDIRECT + StringUtils.UI_API + "/files";
		}

		try {
			FileDB fileFromDatabase = fileService.save(multipartFile);

			if (fileFromDatabase == null) {
				model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
				model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
				return StringUtils.ERROR_PAGE;
			}

			redirectAttributes.addFlashAttribute(StringUtils.SUCCESS_MESSAGE_ATTRIBUTE, "File successfully uploaded");
			return StringUtils.REDIRECT + StringUtils.UI_API + "/files/" + fileFromDatabase.getId() + "/edit";

		} catch (FileUploadException e) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "File upload error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, e.getMessage());
			return StringUtils.ERROR_PAGE;

		} catch (IOException e) {
			System.err.println("FILE SAVE ERROR");
			e.printStackTrace();

			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Error occurred while uploading file");
			return StringUtils.ERROR_PAGE;
		}
	}


	@GetMapping("/{id}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
		FileDB fileFromDatabase = fileService.getById(id);

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(fileFromDatabase.getFile_type()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFromDatabase.getFile_name() + "\"")
				.body(new ByteArrayResource(fileFromDatabase.getData()));
	}


	// EDIT FILE FORM
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return StringUtils.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fileFromDatabase);

		List<Vehicle> vehicles = vehicleService.getAll();
		model.addAttribute("vehicles", vehicles);

		return "vehicle/files/edit_file_page";
	}


	// UPDATE FILE
	@PostMapping("/update")
	public String put(@ModelAttribute FileDB fileDB, Model model) {

		if (fileDB.getId() == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Missing parameter");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "ID parameter is required");
			return StringUtils.ERROR_PAGE;
		}

		ValidationResponse response = fileService.validate(fileDB, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return StringUtils.ERROR_PAGE;
		}

		FileDB fileFromDatabase = fileService.save(fileDB);

		if (fileFromDatabase == null) {
			model.addAttribute(StringUtils.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return StringUtils.ERROR_PAGE;
		} else {
			return StringUtils.REDIRECT + StringUtils.UI_API + "/files/" + fileFromDatabase.getId();
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
