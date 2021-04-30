package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.repositories.vehicle.EquipmentTypeRepository;
import com.example.demo.utils.FieldReflectionUtils;
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

	private final EquipmentRepository equipmentRepository;


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
		if (type == null || type.getId() == null) {
			return;
		}

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		equipmentRepository.findAllByTypeId(type.getId()).forEach(equipment -> {
			equipment.setEquipment_type(null);
			equipmentRepository.save(equipment);

//			equipmentRepository.delete(equipment);
		});

		repository.delete(type);
	}

	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		equipmentRepository.findAll().forEach(equipment -> {
			equipment.setEquipment_type(null);
			equipmentRepository.save(equipment);

//			equipmentRepository.delete(equipment);
		});

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
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			EquipmentType typeFromDatabase = getById(type.getId());

			if (typeFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {




			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<EquipmentType>().validateStringFields(type);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (type.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}