package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.repositories.vehicle.FileRepository;
import lombok.RequiredArgsConstructor;
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

	public FileDB save(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());

		return repository.save(fileDB);
	}

	public void delete(FileDB fileDB) {
		repository.delete(fileDB);
	}

	public void deleteAll() {
		repository.deleteAll();
	}


	public String getImageData(byte[] byteData) {
		return Base64.getMimeEncoder().encodeToString(byteData);
	}
}
