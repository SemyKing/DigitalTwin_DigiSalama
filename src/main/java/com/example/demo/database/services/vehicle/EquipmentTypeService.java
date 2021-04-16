package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.EquipmentTypeRepository;
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
		if (type == null) {
			return;
		}

		if (type.getId() == null) {
			return;
		}
		repository.delete(type);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(EquipmentType type, Mapping mapping) {

		if (type == null) {
			return new ValidationResponse(false, "provided NULL equipment_type");
		}

		if (mapping.equals(Mapping.POST)) {
			type.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (type.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			EquipmentType typeFromDatabase = getById(type.getId());

			if (typeFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			// EMPTY STRING CHECK
			List<Field> stringFields = new ArrayList<>();

			Field[] allFields = EquipmentType.class.getDeclaredFields();
			for (Field field : allFields) {
				if (field.getType().equals(String.class)) {
					stringFields.add(field);
				}
			}

			for (Field field : stringFields) {
				field.setAccessible(true);
				Object object = ReflectionUtils.getField(field, type);

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
			if (type.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}