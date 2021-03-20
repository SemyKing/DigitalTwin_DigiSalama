package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.utils.ValidationResponse;
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
		repository.delete(equipment);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(Equipment equipment) {

		if (equipment.getDescription() != null) {
			if (equipment.getDescription().length() <= 0) {
				return new ValidationResponse(false, "description cannot be empty");
			}
		}

		return new ValidationResponse(true, "validation success");
	}


}