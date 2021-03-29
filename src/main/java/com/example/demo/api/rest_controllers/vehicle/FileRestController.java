package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(StringUtils.JSON_API + "/files")
public class FileRestController {

	private final String ENTITY = "file";

	@Autowired
	private final FileService fileService;


	@PostMapping(value = {"/batch"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<String>>> postList(@RequestBody MultipartFile[] files) {

		boolean errorOccurred = false;

		List<RestResponse<String>> responseList = new ArrayList<>();

		for (MultipartFile file : files) {

			RestResponse<String> response = new RestResponse<>();

			String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

			if (fileName.length() <= 0) {
				response.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
				response.setBody(file.getName());
				response.setMessage("file name could not be resolved");

				errorOccurred = true;
			} else {

				FileDB newFile = new FileDB();

				try {
					newFile.setFile_name(fileName);
					newFile.setFile_type(file.getContentType());
					newFile.setData(file.getBytes());

					FileDB fileFromDatabase = fileService.save(newFile);

					response.setBody(fileName);

					if (fileFromDatabase == null) {
						response.setHttp_status(HttpStatus.EXPECTATION_FAILED);
						response.setMessage("failed to save " + ENTITY + " in database");
					} else {
						response.setHttp_status(HttpStatus.OK);
						response.setMessage("file uploaded and saved successfully");
					}
				} catch (IOException e) {
					System.out.println("FILE UPLOAD ERROR");

					response.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
					response.setBody(file.getName());
					response.setMessage("file name could not be resolved");

				}
			}

			responseList.add(response);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}


	@PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<String>> post(@RequestBody MultipartFile file) {

		RestResponse<String> response = new RestResponse<>();

		String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (fileName.length() <= 0) {
			response.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
			response.setBody(file.getName());
			response.setMessage("file name could not be resolved");

			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
		} else {
			FileDB newFile = new FileDB();

			try {
				newFile.setFile_name(fileName);
				newFile.setFile_type(file.getContentType());
				newFile.setData(file.getBytes());

				FileDB fileFromDatabase = fileService.save(newFile);

				response.setBody(fileName);

				if (fileFromDatabase == null) {
					response.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					response.setMessage("failed to save " + ENTITY + " in database");
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
				} else {
					response.setHttp_status(HttpStatus.OK);
					response.setMessage("file uploaded and saved successfully");
					return ResponseEntity.status(HttpStatus.OK).body(response);
				}

			} catch (IOException e) {
				System.out.println("FILE UPLOAD ERROR");

				response.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
				response.setBody(file.getName());
				response.setMessage("file name could not be resolved");

				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
			}
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody FileDB file, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FileDB> getAll() {
		List<FileDB> files = fileService.getAll();

		// TODO: MAYBE REMOVE
		files.sort(Comparator.comparing(FileDB::getId));

		return files;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public FileDB getByID(@PathVariable Long id) {
		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return fileFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<FileDB>>> putList(@RequestBody List<FileDB> files) {

		boolean errorOccurred = false;

		List<RestResponse<FileDB>> responseList = new ArrayList<>();

		for (FileDB file : files) {
			ValidationResponse response = fileService.validate(file, Mapping.PUT);

			if (!response.isValid()) {
				RestResponse<FileDB> responseHandler = new RestResponse<>();
				responseHandler.setBody(file);
				responseHandler.setHttp_status(HttpStatus.BAD_REQUEST);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				FileDB fileFromDatabase = fileService.save(file);

				RestResponse<FileDB> responseHandler = new RestResponse<>();
				responseHandler.setBody(file);

				if (fileFromDatabase == null) {
					responseHandler.setHttp_status(HttpStatus.EXPECTATION_FAILED);
					responseHandler.setMessage("failed to save " + ENTITY + " in database");
				} else {
					responseHandler.setBody(fileFromDatabase);
					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " saved successfully");
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FileDB> putById(@RequestBody FileDB file, @PathVariable Long id) {

		if (file == null) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no content was provided");
		}

		ValidationResponse response = fileService.validate(file, Mapping.PUT);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		file.setId(fileFromDatabase.getId());

		fileService.save(file);

		return ResponseEntity.status(HttpStatus.OK).body(file);
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			changes.remove("password");

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
				mapResponse.setMessage("ID parameter is required");

				responseList.add(mapResponse);

				errorOccurred = true;
			} else {
				Object idObj = changes.get("id");

				if (!(idObj instanceof Integer)) {
					mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
					mapResponse.setMessage("ID parameter is invalid");

					responseList.add(mapResponse);

					errorOccurred = true;
				} else {
					Integer id = (Integer) idObj;

					FileDB fileFromDatabase = fileService.getById(Long.valueOf(id));

					changes.remove("id");

					changes.forEach((key, value) -> {
						Field field = ReflectionUtils.findField(FileDB.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, fileFromDatabase, value);
						}
					});

					ValidationResponse response = fileService.validate(fileFromDatabase, Mapping.PATCH);

					RestResponse<FileDB> userResponse = new RestResponse<>();
					userResponse.setBody(fileFromDatabase);

					if (!response.isValid()) {
						userResponse.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
						userResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {
						userResponse.setHttp_status(HttpStatus.OK);
						userResponse.setMessage(ENTITY + "patched successfully");
					}

					responseList.add(userResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<FileDB>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		changes.remove("id");
		changes.remove("password");

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(FileDB.class, key);
			if (field != null) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, fileFromDatabase, value);
			}
		});

		RestResponse<FileDB> responseHandler = new RestResponse<>();

		ValidationResponse response = fileService.validate(fileFromDatabase, Mapping.PATCH);
		responseHandler.setBody(fileFromDatabase);

		if (!response.isValid()) {
			responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
			responseHandler.setMessage(response.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseHandler);
		}

		FileDB patchedFileDB = fileService.save(fileFromDatabase);
		responseHandler.setBody(patchedFileDB);

		if (patchedFileDB == null) {
			responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
			responseHandler.setMessage("failed to save " + ENTITY + " in database");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseHandler);
		} else {
			responseHandler.setHttp_status(HttpStatus.OK);
			responseHandler.setMessage(ENTITY + " saved successfully");
			return ResponseEntity.status(HttpStatus.OK).body(responseHandler);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<FileDB>>> deleteList(@RequestBody List<FileDB> files) {

		boolean errorOccurred = false;

		List<RestResponse<FileDB>> responseList = new ArrayList<>();

		for (FileDB file : files) {
			ValidationResponse response = fileService.validate(file, Mapping.DELETE);

			RestResponse<FileDB> responseHandler = new RestResponse<>();
			responseHandler.setBody(file);

			if (!response.isValid()) {
				responseHandler.setHttp_status(HttpStatus.NOT_ACCEPTABLE);
				responseHandler.setMessage(response.getMessage());

				responseList.add(responseHandler);

				errorOccurred = true;
			} else {
				try {
					fileService.delete(file);

					responseHandler.setHttp_status(HttpStatus.OK);
					responseHandler.setMessage(ENTITY + " deleted successfully");
				} catch (Exception e) {
					responseHandler.setHttp_status(HttpStatus.UNPROCESSABLE_ENTITY);
					responseHandler.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());
				}

				responseList.add(responseHandler);
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<FileDB>> deleteById(@PathVariable Long id) {
		FileDB fileFromDatabase = fileService.getById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		try {
			fileService.delete(fileFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed");
		}

		RestResponse<FileDB> responseHandler = new RestResponse<>();
		responseHandler.setMessage(ENTITY + " deleted successfully");
		responseHandler.setBody(fileFromDatabase);
		responseHandler.setHttp_status(HttpStatus.OK);

		return ResponseEntity.ok(responseHandler);
	}



	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@PostMapping("/populate_with_test_data")
	public void populateWithTestData() {
	}

	//TODO FOR TESTING -> REMOVE IN PRODUCTION
	@DeleteMapping("/delete_all")
	public void deleteAll() {
		fileService.deleteAll();
	}
}
