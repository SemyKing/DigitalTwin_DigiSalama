package com.example.demo.api.ui_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.database.services.vehicle.RefuelService;
import com.example.demo.database.services.vehicle.VehicleEventService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("file")
@RequestMapping(Constants.UI_API + "/files")
public class FileController {

	private final String ENTITY = "file";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final FileService fileService;

	@Autowired
	private final VehicleService vehicleService;
	@Autowired
	private final RefuelService refuelService;
	@Autowired
	private final VehicleEventService vehicleEventService;


	@GetMapping({"", "/"})
	public String getAll(Model model) {
		List<FileMetaData> fileMetaData = fileService.getAll();
		model.addAttribute("files", fileMetaData);

		return "vehicle/files/files_list_page";
	}


	@GetMapping("/{id}")
	public String getById(@PathVariable Long id, Model model) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fileFromDatabase);

		FileByteData fileByteData = fileFromDatabase.getFile_byte_data();

		if (fileByteData != null) {
			model.addAttribute("fileType", fileByteData.getFile_content_type());

			if (fileByteData.getFile_content_type().contains("image")) {
				model.addAttribute("isImage", true);
				model.addAttribute("image", fileService.getImageData(fileByteData.getData()));
			}
		}

		return "vehicle/files/file_details_page";
	}


	@GetMapping("/upload")
	public String uploadForm(Model model) {
		model.addAttribute("fileMetaData", new FileMetaData());
		model.addAttribute("vehicles",  vehicleService.getAll());

		List<Refuel> refuelList = refuelService.getAll();
		refuelList.sort(Comparator.comparing(Refuel::getTimestamp).reversed());

		model.addAttribute("refuels", refuelList);

		List<VehicleEvent> vehicleEventList = vehicleEventService.getAll();
		vehicleEventList.sort(Comparator.comparing(VehicleEvent::getTimestamp).reversed());

		model.addAttribute("vehicle_events", vehicleEventList);

		return "vehicle/files/upload_file_page";
	}


	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile multipartFile, @ModelAttribute("fileMetaData") FileMetaData fileMeta, Model model, RedirectAttributes redirectAttributes) {

		if (multipartFile == null || multipartFile.isEmpty()) {
			redirectAttributes.addFlashAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Please select a file to upload.");
			redirectAttributes.addFlashAttribute("fileMetaData", fileMeta);
			return Constants.REDIRECT + Constants.UI_API + "/upload";
		}

		try {
			FileByteData fileByteDataFromDatabase = fileService.save(multipartFile);

			if (fileByteDataFromDatabase == null) {
				model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
				model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
				return Constants.ERROR_PAGE;
			}

			FileMetaData fileMetaData = new FileMetaData();
			fileMetaData.setFile_byte_data(fileByteDataFromDatabase);
			fileMetaData.setVehicle(fileMeta.getVehicle());
			fileMetaData.setRefuel(fileMeta.getRefuel());
			fileMetaData.setVehicle_event(fileMeta.getVehicle_event());
			fileMetaData.setDescription(fileMeta.getDescription());

			fileMetaData = fileService.save(fileMetaData);

			eventHistoryLogService.addFileLog("create " + ENTITY, ENTITY + " created:\n" + fileMetaData);

			return Constants.REDIRECT + Constants.UI_API + "/files";

		} catch (FileUploadException e) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "File upload error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, e.getMessage());

			return Constants.ERROR_PAGE;
		} catch (IOException e) {
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Error occurred while uploading file");

			return Constants.ERROR_PAGE;
		}
	}


//	@PostMapping("/upload")
//	public String uploadFile(@RequestParam("file") MultipartFile multipartFile, Model model, RedirectAttributes redirectAttributes) {
//
//		if (multipartFile.isEmpty()) {
//			redirectAttributes.addFlashAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Please select a file to upload.");
//			return Constants.REDIRECT + Constants.UI_API + "/files";
//		}
//
//		try {
//			FileMetaData fileFromDatabase = fileService.save(multipartFile);
//
//			if (fileFromDatabase == null) {
//				model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
//				model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
//				return Constants.ERROR_PAGE;
//			}
//
//			addLog(
//					"create " + ENTITY,
//					ENTITY + " created:\n" + fileFromDatabase);
//
//			redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE_ATTRIBUTE, "File successfully uploaded");
//			return Constants.REDIRECT + Constants.UI_API + "/files/" + fileFromDatabase.getId() + "/edit";
//
//		} catch (FileUploadException e) {
//			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "File upload error");
//			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, e.getMessage());
//			return Constants.ERROR_PAGE;
//
//		} catch (IOException e) {
//			System.err.println("FILE SAVE ERROR");
//			e.printStackTrace();
//
//			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, "Error occurred while uploading file");
//			return Constants.ERROR_PAGE;
//		}
//	}


	@GetMapping("/{id}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase.getFile_byte_data() == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "no data was found");
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(fileFromDatabase.getFile_byte_data().getFile_content_type()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFromDatabase.getFile_byte_data().getFile_name() + "\"")
				.body(new ByteArrayResource(fileFromDatabase.getFile_byte_data().getData()));
	}


	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "No such entity");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with id: " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		model.addAttribute(ENTITY, fileFromDatabase);
		model.addAttribute("vehicles", vehicleService.getAll());

		List<Refuel> refuelList = refuelService.getAll();
		refuelList.sort(Comparator.comparing(Refuel::getTimestamp).reversed());

		model.addAttribute("refuels", refuelList);

		List<VehicleEvent> vehicleEventList = vehicleEventService.getAll();
		vehicleEventList.sort(Comparator.comparing(VehicleEvent::getTimestamp).reversed());

		model.addAttribute("vehicle_events", vehicleEventList);

		return "vehicle/files/edit_file_page";
	}


	@PostMapping("/update")
	public String put(@ModelAttribute FileMetaData fileMetaData, Model model) {
		String oldFileFromDatabase = fileService.getFileMetaDataById(fileMetaData.getId()).toString();

		fileMetaData = new FieldReflectionUtils<FileMetaData>().getEntityWithEmptyStringValuesAsNull(fileMetaData);

		ValidationResponse response = fileService.validate(fileMetaData, Mapping.PUT);

		if (!response.isValid()) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Validation error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, response.getMessage());
			return Constants.ERROR_PAGE;
		}

		FileMetaData fileFromDatabase = fileService.save(fileMetaData);

		if (fileFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Database error");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE,"failed to save " + ENTITY + " in database");
			return Constants.ERROR_PAGE;
		} else {

			eventHistoryLogService.addFileLog("update " + ENTITY, ENTITY + " updated from:\n" + oldFileFromDatabase + "\nto:\n" + fileFromDatabase);

			return Constants.REDIRECT + Constants.UI_API + "/files/" + fileFromDatabase.getId();
		}
	}


	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			model.addAttribute(Constants.ERROR_TITLE_ATTRIBUTE, "Not found");
			model.addAttribute(Constants.ERROR_MESSAGE_ATTRIBUTE, ENTITY + " with ID " + id + " not found");
			return Constants.ERROR_PAGE;
		}

		fileService.delete(fileFromDatabase);

		eventHistoryLogService.addFileLog("delete " + ENTITY, ENTITY + " deleted:\n" + fileFromDatabase);

		return Constants.REDIRECT + Constants.UI_API + "/files";
	}
}
