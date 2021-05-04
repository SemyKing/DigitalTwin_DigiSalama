package com.example.demo.database.services;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.utils.Constants;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	@Autowired
	private PasswordEncoder bcryptEncoder;

	private final UserRepository repository;

	private final RoleRepository roleRepository;
	private final OrganisationService organisationService;

	@Transactional
	public List<User> getAll() {
		return repository.findAll();
	}

	@Transactional
	public List<User> getAllByOrganisationId(Long id) {
		return repository.findAllByOrganisationId(id);
	}

	@Transactional
	public User getById(Long id) {
		User user = null;

		Optional<User> optional = repository.findById(id);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return null;
		}

		if (authentication.getName() == null || authentication.getName().length() <= 0) {
			return null;
		}

		return getByUsername(authentication.getName());
	}

	@Transactional
	public User getByUsername(String username) {
		User user = null;

		Optional<User> optional = repository.findUserByUsername(username);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	@Transactional
	public User getByEmail(String email) {
		User user = null;

		Optional<User> optional = repository.findUserByEmail(email);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	@Transactional
	public User getByPasswordUpdateToken(String token) {
		User user = null;

		Optional<User> optional = repository.findUserByPasswordUpdateToken(token);

		if (optional.isPresent()) {
			user = optional.get();
		}

		return user;
	}

	@Transactional
	public User save(User user) {
		return repository.save(user);
	}

	@Transactional
	public void delete(User user) {
		repository.delete(user);
	}

	@Transactional
	public void deleteAll() {
		repository.deleteAll();
	}


	public boolean isPasswordCorrect(Long id, String password) {
		User user = getById(id);

		if (user == null) {
			return false;
		}

		return getBcryptEncoder().matches(password, user.getPassword());
	}

	public boolean isPasswordCorrect(User user, String password) {
		if (user == null) {
			return false;
		}

		return getBcryptEncoder().matches(password, user.getPassword());
	}

	public boolean emailAlreadyExists(String email) {
		User user = getByEmail(email);
		return (user != null);
	}

	public boolean usernameAlreadyExists(String username) {
		User user = getByUsername(username);
		return (user != null);
	}

	@Transactional
	public int getSystemAdminsCount() {
		return repository.findSystemAdmins().size();
	}

	@Transactional
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

		return user.getRole().getName().equals(Constants.ROLE_SYSTEM_ADMIN);
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

		return user.getRole().getName().equals(Constants.ROLE_ORGANISATION_ADMIN);
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

		return user.getRole().getName().equals(Constants.ROLE_ORGANISATION_ADMIN) || user.getRole().getName().equals(Constants.ROLE_SYSTEM_ADMIN);
	}

	public boolean isCurrentUserSystemAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return false;
		}

		if (authentication.getName() == null || authentication.getName().length() <= 0) {
			return false;
		}

		User userFromDatabase = getByUsername(authentication.getName());

		if (userFromDatabase == null) {
			return false;
		}

		if (userFromDatabase.getRole() == null) {
			return false;
		}

		return userFromDatabase.getRole().getName().equals(Constants.ROLE_SYSTEM_ADMIN);
	}

	public boolean isCurrentUserOrganisationAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return false;
		}

		if (authentication.getName() == null || authentication.getName().length() <= 0) {
			return false;
		}

		User userFromDatabase = getByUsername(authentication.getName());

		if (userFromDatabase == null) {
			return false;
		}

		if (userFromDatabase.getRole() == null) {
			return false;
		}

		return userFromDatabase.getRole().getName().equals(Constants.ROLE_ORGANISATION_ADMIN);
	}

	public boolean isCurrentUserOrganisationOrSystemAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return false;
		}

		if (authentication.getName() == null || authentication.getName().length() <= 0) {
			return false;
		}

		User userFromDatabase = getByUsername(authentication.getName());

		if (userFromDatabase == null) {
			return false;
		}

		if (userFromDatabase.getRole() == null) {
			return false;
		}

		return userFromDatabase.getRole().getName().equals(Constants.ROLE_ORGANISATION_ADMIN) || userFromDatabase.getRole().getName().equals(Constants.ROLE_SYSTEM_ADMIN);
	}


    public ValidationResponse validate(User user, Mapping mapping) {
		if (user == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			user.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (user.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			User userFromDatabase = getById(user.getId());

			if (userFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}

			//PROTECTION AGAINST PASSWORD MANIPULATIONS
			user.setPassword(userFromDatabase.getPassword());
			user.setPassword_update_token(userFromDatabase.getPassword_update_token());
			user.setRole(userFromDatabase.getRole());
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (user.getUsername() == null) {
				return new ValidationResponse(false, "username is required");
			} else {
				if (user.getUsername().length() <= 0) {
					return new ValidationResponse(false, "username cannot be empty");
				}

				User userFromDatabase = getByUsername(user.getUsername());

				if (userFromDatabase != null) {
					if (user.getId() == null || !userFromDatabase.getId().equals(user.getId())) {
						return new ValidationResponse(false, "username is taken");
					}
				}
			}

			if (user.getPassword() == null) {
				return new ValidationResponse(false, "password is required");
			} else {
				if (user.getPassword().length() <= 0) {
					return new ValidationResponse(false, "password cannot be empty");
				}

				user.setPassword(getBcryptEncoder().encode(user.getPassword()));
			}

			if (user.getEmail() != null) {
				if (user.getEmail().length() <= 0) {
					return new ValidationResponse(false, "email cannot be empty");
				}

				User userFromDatabase = getByEmail(user.getEmail());

				if (userFromDatabase != null) {
					if (user.getId() == null || !userFromDatabase.getId().equals(user.getId())) {
						return new ValidationResponse(false, "email is taken");
					}
				}
			}

			if (user.getRole() == null || user.getRole().getId() == null) {
				user.setRole(roleRepository.findByName(Constants.ROLE_USER));
			}

			if (user.getOrganisation() == null) {
				return new ValidationResponse(false, "organisation is required");
			}

			if (user.getOrganisation().getId() == null) {
				return new ValidationResponse(false, "organisation ID is required");
			}

			Organisation organisation = organisationService.getById(user.getOrganisation().getId());

			if (organisation == null) {
				return new ValidationResponse(false, "organisation with ID " + user.getOrganisation().getId() + " not found");
			}

			user.setOrganisation(organisation);


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<User>().validateStringFields(user);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (user.getId() == null) {
				return new ValidationResponse(false, "entity ID is required");
			}

			if (isUserSystemAdmin(user.getId())) {
				if (getSystemAdminsCount() <= 1) {
					return new ValidationResponse(false, "cannot delete last System Administrator");
				}
			}
		}

		return new ValidationResponse(true, "validation success");
    }


	public String generatePasswordUpdateToken(User user) {
		String uuid = UUID.randomUUID().toString();

		user.setPassword_update_token(uuid);
		save(user);

		return uuid;
	}

	public PasswordEncoder getBcryptEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User userFromDatabase = getByUsername(username);
		if (userFromDatabase == null) {
			throw new UsernameNotFoundException("Invalid credentials");
		}

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(userFromDatabase.getRole().getName()));

		return new org.springframework.security.core.userdetails.User(userFromDatabase.getUsername(), userFromDatabase.getPassword(), authorities);
	}
}
