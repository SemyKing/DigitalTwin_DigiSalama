package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.EquipmentType;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.repositories.vehicle.EquipmentTypeRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
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

	private final EquipmentTypeRepository equipmentTypeRepository;
	private final VehicleRepository vehicleRepository;


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
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Equipment equipmentFromDatabase = getById(equipment.getId());

			if (equipmentFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (equipment.getEquipment_type() != null) {
				if (equipment.getEquipment_type().getId() == null) {
					return new ValidationResponse(false, "equipment_type ID is required");
				}

				Optional<EquipmentType> equipmentType = equipmentTypeRepository.findById(equipment.getEquipment_type().getId());

				if (equipmentType.isEmpty()) {
					return new ValidationResponse(false, "equipment_type with ID: " + equipment.getVehicle().getId() + " not found");
				}

				equipment.setEquipment_type(equipmentType.get());
			}

			if (equipment.getVehicle() != null) {
				if (equipment.getVehicle().getId() == null) {
					return new ValidationResponse(false, "vehicle ID is required");
				}

				Optional<Vehicle> vehicle = vehicleRepository.findById(equipment.getVehicle().getId());

				if (vehicle.isEmpty()) {
					return new ValidationResponse(false, "vehicle with ID: " + equipment.getVehicle().getId() + " not found");
				}

				equipment.setVehicle(vehicle.get());
			}

			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Equipment>().validateStringFields(equipment);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (equipment.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}