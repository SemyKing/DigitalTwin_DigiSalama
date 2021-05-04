package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.FileService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/files")
public class FileRestController {

	private final String ENTITY = "file";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final FileService fileService;

	@Autowired
	private ObjectMapper objectMapper;


//	@PostMapping(value = {"/batch"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<List<RestResponse<String>>> postList(@RequestBody MultipartFile[] files) {
//
//		boolean errorOccurred = false;
//
//		List<RestResponse<String>> responseList = new ArrayList<>();
//
//		for (MultipartFile file : files) {
//
//			RestResponse<String> response = new RestResponse<>();
//
//			String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
//
//			if (fileName.length() <= 0) {
//				response.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
//				response.setBody(file.getName());
//				response.setMessage("file name could not be resolved");
//
//				errorOccurred = true;
//			} else {
//
//				FileMetaData newFile = new FileMetaData();
//
//				try {
//					newFile.setFile_name(fileName);
//					newFile.setFile_type(file.getContentType());
//					newFile.setData(file.getBytes());
//
//					FileMetaData fileFromDatabase = fileService.save(newFile);
//
//					response.setBody(fileName);
//
//					if (fileFromDatabase == null) {
//						response.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
//						response.setMessage("failed to save " + ENTITY + " in database");
//
//						errorOccurred = true;
//					} else {
//						response.setHttp_status(HttpStatus.OK);
//						response.setMessage("file uploaded and saved successfully");
//
//						eventHistoryLogService.addFileLog("create " + ENTITY, ENTITY + " created:\n" + fileFromDatabase);
//					}
//				} catch (IOException e) {
//					System.out.println("FILE UPLOAD ERROR");
//
//					response.setHttp_status(HttpStatus.BAD_REQUEST);
//					response.setBody(file.getName());
//					response.setMessage("file name could not be resolved");
//				}
//			}
//
//			responseList.add(response);
//		}
//
//		if (errorOccurred) {
//			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
//		} else {
//			return ResponseEntity.status(HttpStatus.OK).body(responseList);
//		}
//	}

	@PostMapping(value = {"/batch"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> postList(@RequestBody MultipartFile[] files) {

		if (files == null || files.length <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<?>> restResponseList = new ArrayList<>();

		for (MultipartFile file : files) {
			RestResponse<Object> restResponse = new RestResponse<>();
			restResponse.setBody(file);

			if (file == null) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage("provided NULL MultiPartFile");

				errorOccurred = true;
			} else {
				String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

				if (fileName.length() <= 0) {
					restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
					restResponse.setMessage("MultiPartFile name could not be resolved");

					errorOccurred = true;
				} else {
					FileByteData fileByteData = new FileByteData();

					try {
						fileByteData.setFile_name(fileName);
						fileByteData.setFile_content_type(file.getContentType());
						fileByteData.setData(file.getBytes());

						FileByteData fileByteDataFromDatabase = fileService.save(fileByteData);

						if (fileByteDataFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							fileByteData = fileService.save(fileByteData);

							FileMetaData fileMetaData = new FileMetaData();
							fileMetaData.setFile_byte_data(fileByteData);

							fileMetaData = fileService.save(fileMetaData);

							restResponse.setBody(fileMetaData);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage("file uploaded and saved successfully");

							eventHistoryLogService.addFileLog("create " + ENTITY, ENTITY + " created:\n" + fileByteData);
						}
					} catch (IOException e) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage("file upload error: " + e.getMessage());

						errorOccurred = true;
					}
				}
			}

			restResponseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(restResponseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(restResponseList);
		}
	}

//	@PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<RestResponse<String>> post(@RequestBody MultipartFile file) {
//
//		RestResponse<String> response = new RestResponse<>();
//
//		String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
//
//		if (fileName.length() <= 0) {
//			response.setHttp_status(HttpStatus.BAD_REQUEST);
//			response.setBody(file.getName());
//			response.setMessage("file name could not be resolved");
//
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//		} else {
//			FileByteData newFile = new FileByteData();
//
//			try {
//				newFile.setFile_name(fileName);
//				newFile.setFile_content_type(file.getContentType());
//				newFile.setData(file.getBytes());
//
//				FileMetaData fileFromDatabase = fileService.save(newFile);
//
//				response.setBody(fileName);
//
//				if (fileFromDatabase == null) {
//					response.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
//					response.setMessage("failed to save " + ENTITY + " in database");
//					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//				} else {
//					response.setHttp_status(HttpStatus.OK);
//					response.setMessage("file uploaded and saved successfully");
//
//					eventHistoryLogService.addFileLog("create " + ENTITY, ENTITY + " created:\n" + fileFromDatabase);
//
//					return ResponseEntity.status(HttpStatus.OK).body(response);
//				}
//
//			} catch (IOException e) {
//				System.out.println("FILE UPLOAD ERROR");
//
//				response.setHttp_status(HttpStatus.BAD_REQUEST);
//				response.setBody(file.getName());
//				response.setMessage("file upload error");
//
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//			}
//		}
//	}

	@PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<?>> post(@RequestBody MultipartFile file) {

		if (file == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL entity provided");
		}

		RestResponse<Object> restResponse = new RestResponse<>();
		restResponse.setBody(file);

		String fileName = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (fileName.length() <= 0) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage("MultiPartFile name could not be resolved");
		} else {
			FileByteData fileByteData = new FileByteData();

			try {
				fileByteData.setFile_name(fileName);
				fileByteData.setFile_content_type(file.getContentType());
				fileByteData.setData(file.getBytes());

				FileByteData fileByteDataFromDatabase = fileService.save(fileByteData);

				if (fileByteDataFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

				} else {
					fileByteData = fileService.save(fileByteData);

					FileMetaData fileMetaData = new FileMetaData();
					fileMetaData.setFile_byte_data(fileByteData);

					fileMetaData = fileService.save(fileMetaData);

					restResponse.setBody(fileMetaData);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage("file uploaded and saved successfully");

					eventHistoryLogService.addFileLog("create " + ENTITY, ENTITY + " created:\n" + fileByteData);
				}
			} catch (IOException e) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage("file upload error");
			}
		}

		return ResponseEntity.status(restResponse.getHttp_status()).body(restResponse);
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody FileMetaData file, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FileMetaData> getAll() {
		return fileService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public FileMetaData getByID(@PathVariable Long id) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return fileFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<FileMetaData>>> putList(@RequestBody List<FileMetaData> files) {

		if (files == null || files.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<FileMetaData>> responseList = new ArrayList<>();

		for (FileMetaData file : files) {
			RestResponse<FileMetaData> restResponse = new RestResponse<>();
			restResponse.setBody(file);
			
			ValidationResponse response = fileService.validate(file, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldFileFromDatabase = fileService.getFileMetaDataById(file.getId()).toString();
				FileMetaData fileFromDatabase = fileService.save(file);

				if (fileFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(fileFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					eventHistoryLogService.addFileLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldFileFromDatabase + "\nto:\n" + fileFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<FileMetaData>> putById(@RequestBody FileMetaData file, @PathVariable Long id) {

		RestResponse<FileMetaData> restResponse = new RestResponse<>();
		restResponse.setBody(file);
		
		ValidationResponse response = fileService.validate(file, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldFileFromDatabase = fileService.getFileMetaDataById(file.getId()).toString();
		FileMetaData fileFromDatabase = fileService.save(file);

		if (fileFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(fileFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addFileLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldFileFromDatabase + "\nto:\n" + fileFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		if (changesList == null || changesList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (changes == null) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("NULL array element was provided");
				responseList.add(mapResponse);
				errorOccurred = true;
				continue;
			}

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("ID parameter is required");

				responseList.add(mapResponse);

				errorOccurred = true;
			} else {
				Object idObj = changes.get("id");

				if (!(idObj instanceof Integer)) {
					mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
					mapResponse.setMessage("ID parameter is invalid");

					responseList.add(mapResponse);

					errorOccurred = true;
				} else {
					long idLong = (long) ((Integer) idObj);
					changes.remove("id");

					String oldFileFromDatabase = fileService.getFileMetaDataById(idLong).toString();
					FileMetaData fileFromDatabase;

					try {
						fileFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					ValidationResponse response = fileService.validate(fileFromDatabase, Mapping.PATCH);

					RestResponse<FileMetaData> restResponse = new RestResponse<>();
					restResponse.setBody(fileFromDatabase);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {

						FileMetaData updatedFileFromDatabase = fileService.save(fileFromDatabase);

						if (updatedFileFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedFileFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							eventHistoryLogService.addFileLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldFileFromDatabase + "\nto:\n" + updatedFileFromDatabase);
						}
					}

					responseList.add(restResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<FileMetaData>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldFileFromDatabase = fileFromDatabase.toString();

		changes.remove("id");

		RestResponse<FileMetaData> restResponse = new RestResponse<>();

		try {
			fileFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(fileFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(fileFromDatabase);
		
		ValidationResponse response = fileService.validate(fileFromDatabase, Mapping.PATCH);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		FileMetaData patchedFileMetaData = fileService.save(fileFromDatabase);
		restResponse.setBody(patchedFileMetaData);

		if (patchedFileMetaData == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addFileLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldFileFromDatabase + "\nto:\n" + patchedFileMetaData);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<FileMetaData>>> deleteList(@RequestBody List<FileMetaData> files) {

		if (files == null || files.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<FileMetaData>> responseList = new ArrayList<>();

		for (FileMetaData file : files) {
			RestResponse<FileMetaData> restResponse = new RestResponse<>();
			restResponse.setBody(file);
			
			ValidationResponse response = fileService.validate(file, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					fileService.delete(file);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					eventHistoryLogService.addFileLog("delete " + ENTITY, ENTITY + " deleted:\n" + file);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());

					errorOccurred = true;
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<FileMetaData>> deleteById(@PathVariable Long id) {
		FileMetaData fileFromDatabase = fileService.getFileMetaDataById(id);

		if (fileFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = fileService.validate(fileFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			fileService.delete(fileFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<FileMetaData> restResponse = new RestResponse<>();
		restResponse.setBody(fileFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		eventHistoryLogService.addFileLog("delete " + ENTITY, ENTITY + " deleted:\n" + fileFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private FileMetaData handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		FileMetaData entity = fileService.getFileMetaDataById(id);

		if (entity != null) {
			changes.forEach((key, value) -> {
				Field field = ReflectionUtils.findField(entity.getClass(), key);

				if (field != null) {
					field.setAccessible(true);

					String json = value == null ? null : value.toString();

					if (json == null) {
						ReflectionUtils.setField(field, entity, null);
					} else {
						if (field.getType().equals(String.class)) {
							ReflectionUtils.setField(field, entity, json);
						} else {

							if (field.getType().equals(LocalDateTime.class)) {
								LocalDateTime localDateTime = null;

								try {
									localDateTime = DateUtils.stringToLocalDateTime(json);
								} catch (Exception e) {
									throw new JsonParseException(new Throwable(e.getMessage()));
								}

								ReflectionUtils.setField(field, entity, localDateTime);
							}

							if (field.getType().equals(FileByteData.class)) {
								try {
									entity.setFile_byte_data(objectMapper.readValue(json, FileByteData.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("FileByteData json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(Vehicle.class)) {
								try {
									entity.setVehicle(objectMapper.readValue(json, Vehicle.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Vehicle json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(Refuel.class)) {
								try {
									entity.setRefuel(objectMapper.readValue(json, Refuel.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("Refuel json parsing error: " + e.getMessage()));
								}
							}

							if (field.getType().equals(VehicleEvent.class)) {
								try {
									entity.setVehicle_event(objectMapper.readValue(json, VehicleEvent.class));
								} catch (JsonProcessingException e) {
									throw new JsonParseException(new Throwable("VehicleEvent json parsing error: " + e.getMessage()));
								}
							}

						}
					}
				}
			});
		}

		return entity;
	}
}
