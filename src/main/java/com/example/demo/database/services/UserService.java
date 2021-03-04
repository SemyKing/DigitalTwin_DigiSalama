package com.example.demo.database.services;

import com.example.demo.database.models.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.utils.StringUtils;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository repository;

	private final RoleRepository roleRepository;


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

    public ValidationResponse validateUser(User user) {

		if (user.getUsername() == null) {
			return new ValidationResponse(null, user, null, null,false, "username is required");
		} else {
			if (user.getUsername().length() <= 0) {
				return new ValidationResponse(null, user, null, null,false, "username cannot be empty");
			}

			User userFromDatabase = getByUsername(user.getUsername());

			if (userFromDatabase != null) {
				if (user.getId() == null) {
					return new ValidationResponse(null, user, null, null,false, "username is taken");
				} else {
					if (!userFromDatabase.getId().equals(user.getId())) {
						return new ValidationResponse(null, user, null, null,false, "username is taken");
					}
				}
			}
		}

		if (user.getPasswordHash() != null) {
			if (user.getPasswordHash().length() <= 0) {
				return new ValidationResponse(null, user, null, null,false, "password cannot be empty");
			}
		}

		if (user.getEmail() != null) {
			if (user.getEmail().length() <= 0) {
				return new ValidationResponse(null, user, null, null,false, "email cannot be empty");
			}

			User userFromDatabase = getByEmail(user.getEmail());

			if (userFromDatabase != null) {
				if (user.getId() == null) {
					return new ValidationResponse(null, user, null, null,false, "email is taken");
				} else {
					if (!userFromDatabase.getId().equals(user.getId())) {
						return new ValidationResponse(null, user, null, null,false, "email is taken");
					}
				}
			}
		}

		if (user.getFirstName() != null) {
			if (user.getFirstName().length() <= 0) {
				return new ValidationResponse(null, user, null, null,false, "first name cannot be empty");
			}
		}

		if (user.getLastName() != null) {
			if (user.getLastName().length() <= 0) {
				return new ValidationResponse(null, user, null, null,false, "last name cannot be empty");
			}
		}

		if (user.getOrganisation() == null || user.getOrganisation().getId() == null) {
			return new ValidationResponse(null, user, null, null, false, "user must be in organisation");
		}

		if (user.getRole() == null || user.getRole().getId() == null) {
			user.setRole(roleRepository.findByName(StringUtils.ROLE_USER));
		}

		return new ValidationResponse(null, user, null, null,true, "validation success");
    }
}
