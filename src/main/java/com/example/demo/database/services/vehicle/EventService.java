package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.EventRepository;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.RefuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

	private final EventRepository repository;

	private final FileRepository fileRepository;


	public List<VehicleEvent> getAll() {
		return repository.findAll();
	}

	public VehicleEvent getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<VehicleEvent> event = repository.findById(id);
		if (event.isEmpty()) {
			return null;
		}
		return event.get();
	}

	public List<VehicleEvent> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public VehicleEvent save(VehicleEvent event) {
		return repository.save(event);
	}

	public void delete(VehicleEvent event) {
		if (event == null) {
			return;
		}

		if (event.getId() != null) {
			List<FileDB> files = fileRepository.findAllByVehicleEventId(event.getId());
			for (FileDB file : files) {
				file.setVehicle(null);
				fileRepository.save(file);

//				fileRepository.delete(file);
			}
		}

		repository.delete(event);
	}

	public void deleteAll() {
		List<FileDB> files = fileRepository.findAll();

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		fileRepository.deleteAll();

		for (FileDB file : files) {
			file.setEvent(null);
			fileRepository.save(file);
		}

		repository.deleteAll();
	}


    public ValidationResponse validate(VehicleEvent event, Mapping mapping) {

		if (event == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			event.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (event.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			VehicleEvent eventFromDatabase = getById(event.getId());

			if (eventFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (event.getName() != null) {
				if (event.getName().length() <= 0) {
					return new ValidationResponse(false, "name cannot be empty");
				}
			}

			if (event.getDescription() != null) {
				if (event.getDescription().length() <= 0) {
					return new ValidationResponse(false, "description cannot be empty");
				}
			}

			// OTHER VALIDATION
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (event.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			VehicleEvent eventFromDatabase = getById(event.getId());

			if (eventFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
