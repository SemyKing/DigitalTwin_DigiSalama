package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.RefuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefuelService {

	private final RefuelRepository repository;

	private final FileRepository fileRepository;

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");


	public List<Refuel> getAll() {
		return repository.findAll();
	}

	public Refuel getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Refuel> refuel = repository.findById(id);
		if (refuel.isEmpty()) {
			return null;
		}
		return refuel.get();
	}

	public List<Refuel> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public Refuel save(Refuel refuel) {
		return repository.save(refuel);
	}

	public void delete(Refuel refuel) {
		if (refuel == null) {
			return;
		}

		if (refuel.getId() != null) {
			List<FileDB> files = fileRepository.findAllByRefuelId(refuel.getId());
			for (FileDB file : files) {
				file.setVehicle(null);
				fileRepository.save(file);

//				fileRepository.delete(file);
			}
		}

		repository.delete(refuel);
	}

	public void deleteAll() {
		List<FileDB> files = fileRepository.findAll();

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		fileRepository.deleteAll();

		for (FileDB file : files) {
			file.setRefuel(null);
			fileRepository.save(file);
		}

		repository.deleteAll();
	}


    public ValidationResponse validate(Refuel refuel, Mapping mapping) {

		if (refuel == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			refuel.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (refuel.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			Refuel refuelFromDatabase = getById(refuel.getId());

			if (refuelFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			// EMPTY STRING CHECK
			List<Field> stringFields = new ArrayList<>();

			Field[] allFields = Refuel.class.getDeclaredFields();
			for (Field field : allFields) {
				if (field.getType().equals(String.class)) {
					stringFields.add(field);
				}
			}

			for (Field field : stringFields) {
				field.setAccessible(true);
				Object object = ReflectionUtils.getField(field, refuel);

				if (object != null) {
					if (object instanceof String) {
						if (((String) object).length() <= 0) {
							return new ValidationResponse(false, "'" + field.getName() + "' cannot be empty");
						}
					}
				}
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (refuel.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			Refuel refuelFromDatabase = getById(refuel.getId());

			if (refuelFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
