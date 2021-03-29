package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipmentService {

	private final EquipmentRepository repository;


	public List<Equipment> getAll() {
		return repository.findAll();
	}

	public Equipment getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Equipment> equipment = repository.findById(id);
		if (equipment.isEmpty()) {
			return null;
		}
		return equipment.get();
	}

	public List<Equipment> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public Equipment save(Equipment equipment) {
		return repository.save(equipment);
	}

	public void delete(Equipment equipment) {
		if (equipment == null) {
			return;
		}

		if (equipment.getId() == null) {
			return;
		}
		repository.delete(equipment);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(Equipment equipment, Mapping mapping) {

		if (equipment == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			equipment.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (equipment.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			Equipment equipmentFromDatabase = getById(equipment.getId());

			if (equipmentFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (equipment.getDescription() != null) {
				if (equipment.getDescription().length() <= 0) {
					return new ValidationResponse(false, "description cannot be empty");
				}
			}

			//...
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (equipment.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}


}