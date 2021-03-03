package com.example.demo.database.services;

import com.example.demo.database.models.User;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository repository;


	public List<User> getAll() {
		return repository.findAll();
	}

	public User getById(Long id) {
		User user = null;

		Optional<User> optional = repository.findById(id);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	public User getByUsername(String username) {
		User user = null;

		Optional<User> optional = repository.findUserByUsername(username);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	public User getByEmail(String email) {
		User user = null;

		Optional<User> optional = repository.findUserByEmail(email);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	public User getByApiToken(String apiToken) {
		User user = null;

		Optional<User> optional = repository.findUserByApiToken(apiToken);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	public User save(User user) {
		return repository.save(user);
	}

	public void delete(User user) {
		repository.delete(user);
	}

	public void deleteAll() {
		repository.deleteAll();
	}


	public boolean emailAlreadyExists(String email) {
		User user = getByEmail(email);
		return (user != null);
	}

	public boolean usernameAlreadyExists(String username) {
		User user = getByUsername(username);
		return (user != null);
	}

	public boolean systemAdminExists() {
		List<User> systemAdmins = repository.findSystemAdmins();
		return (systemAdmins.size() > 0);
	}

	public boolean isUserSystemAdmin(Long id) {
		if (id == null) {
			return false;
		}

		User user = getById(id);

		if (user == null) {
			return false;
		}

		if (user.getRole() == null) {
			return false;
		}

		return user.getRole().getName().equals(StringUtils.ROLE_SYSTEM_ADMIN);
	}

	public boolean isUserOrganisationAdmin(Long id) {
		if (id == null) {
			return false;
		}

		User user = getById(id);

		if (user == null) {
			return false;
		}

		if (user.getRole() == null) {
			return false;
		}

		return user.getRole().getName().equals(StringUtils.ROLE_ORGANISATION_ADMIN);
	}

	public boolean isUserOrganisationOrSystemAdmin(Long id) {
		if (id == null) {
			return false;
		}

		User user = getById(id);

		if (user == null) {
			return false;
		}

		if (user.getRole() == null) {
			return false;
		}

		return user.getRole().getName().equals(StringUtils.ROLE_ORGANISATION_ADMIN) || user.getRole().getName().equals(StringUtils.ROLE_SYSTEM_ADMIN);
	}
}
