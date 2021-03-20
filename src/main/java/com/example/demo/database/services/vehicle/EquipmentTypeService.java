package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.repositories.vehicle.EquipmentTypeRepository;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipmentTypeService {

	private final EquipmentTypeRepository repository;


	public List<EquipmentType> getAll() {
		return repository.findAll();
	}

	public EquipmentType getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<EquipmentType> type = repository.findById(id);
		if (type.isEmpty()) {
			return null;
		}
		return type.get();
	}

	public EquipmentType save(EquipmentType type) {
		return repository.save(type);
	}

	public void delete(EquipmentType type) {
		repository.delete(type);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(EquipmentType type) {

		if (type.getName() != null) {
			if (type.getName().length() <= 0) {
				return new ValidationResponse(false, "name cannot be empty");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}