package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.RefuelRepository;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
			if (refuel.getLocation() != null) {
				if (refuel.getLocation().length() <= 0) {
					return new ValidationResponse(false, "location cannot be empty");
				}
			}

			if (refuel.getFuel_name() != null) {
				if (refuel.getFuel_name().length() <= 0) {
					return new ValidationResponse(false, "fuel name cannot be empty");
				}
			}

			if (refuel.getRefuel_amount() != null) {
				if (refuel.getRefuel_amount() < 0) {
					return new ValidationResponse(false, "refuel amount invalid");
				}
			}

			if (refuel.getPrice() != null) {
				if (refuel.getPrice() < 0) {
					return new ValidationResponse(false, "price invalid");
				}
			}

			if (refuel.getDescription() != null) {
				if (refuel.getDescription().length() <= 0) {
					return new ValidationResponse(false, "description cannot be empty");
				}
			}

			// OTHER VALIDATION
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
