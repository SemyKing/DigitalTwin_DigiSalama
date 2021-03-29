package com.example.demo.database.services;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.repositories.OrganisationRepository;
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
public class OrganisationService {

	private final OrganisationRepository repository;


	public List<Organisation> getAll() {
		return repository.findAll();
	}

	public Organisation getById(Long id) {
		if (id == null) {
			return null;
		}
		Optional<Organisation> organisation = repository.findById(id);
		if (organisation.isEmpty()) {
			return null;
		}
		return organisation.get();
	}

	public Organisation getByName(String name) {
		if (name == null ||name.length() <= 0) {
			return null;
		}
		Optional<Organisation> organisation = repository.findOrganisationByName(name);
		if (organisation.isEmpty()) {
			return null;
		}
		return organisation.get();
	}

	public Organisation save(Organisation organisation) {
		return repository.save(organisation);
	}

	public void delete(Organisation organisation) {
		repository.delete(organisation);
	}

	public void deleteAll() {
		repository.deleteAll();
	}



	public ValidationResponse validate(Organisation organisation, Mapping mapping) {

		if (organisation == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			organisation.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (organisation.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			// CHECK ID
			Organisation organisationFromDatabase = getById(organisation.getId());

			if (organisationFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (organisation.getName() != null) {
				if (organisation.getName().length() <= 0) {
					return new ValidationResponse(false, "name cannot be empty");
				}
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (organisation.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}
