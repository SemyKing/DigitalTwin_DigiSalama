package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

			// EMPTY STRING CHECK
			List<Field> stringFields = new ArrayList<>();

			Field[] allFields = Equipment.class.getDeclaredFields();
			for (Field field : allFields) {
				if (field.getType().equals(String.class)) {
					stringFields.add(field);
				}
			}

			for (Field field : stringFields) {
				field.setAccessible(true);
				Object object = ReflectionUtils.getField(field, equipment);

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
			if (equipment.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}


}