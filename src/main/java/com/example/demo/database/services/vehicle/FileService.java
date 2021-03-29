package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

	private final FileRepository repository;


	public List<FileDB> getAll() {
		return repository.findAll();
	}

	public FileDB getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<FileDB> image = repository.findById(id);
		if (image.isEmpty()) {
			return null;
		}
		return image.get();
	}

	public List<FileDB> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public FileDB save(FileDB file) {
		return repository.save(file);
	}

	public FileDB save(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (fileName.length() <= 0) {
			throw new FileUploadException("File name cannot be empty");
		}

		if (file.getContentType() == null) {
			throw new FileUploadException("File content type cannot be NULL");
		}

		FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());

		return repository.save(fileDB);
	}


	public void delete(FileDB fileDB) {
		if (fileDB == null) {
			return;
		}

		if (fileDB.getId() == null) {
			return;
		}
		repository.delete(fileDB);
	}

	public void deleteAll() {
		repository.deleteAll();
	}


	public String getImageData(byte[] byteData) {
		return Base64.getMimeEncoder().encodeToString(byteData);
	}

	public ValidationResponse validate(FileDB file, Mapping mapping) {

		if (file == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			file.setId(null);

			if (file.getMultipart_file() == null) {
				return new ValidationResponse(false, "multipart_file is required");
			}
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (file.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			FileDB fileFromDatabase = getById(file.getId());

			if (fileFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

		}

		return new ValidationResponse(true, "validation successful");
	}
}
