package com.example.demo.database.services;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.repositories.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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

	public Organisation save(Organisation organisation) {
		return repository.save(organisation);
	}

	public void delete(Organisation organisation) {
		repository.delete(organisation);
	}

	public void deleteAll() {
		repository.deleteAll();
	}
}
