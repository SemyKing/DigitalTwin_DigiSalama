package com.example.demo.database.services;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.User;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.utils.Mapping;
import com.example.demo.utils.StringUtils;
import com.example.demo.utils.ValidationResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	@Autowired
	private PasswordEncoder bcryptEncoder;

	private final UserRepository repository;

	private final RoleRepository roleRepository;

	private final OrganisationService organisationService;


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

	public User getByUsernameAndPassword(String username, String password) {
		User user = null;

		Optional<User> optional = repository.findUserByUsernameAndPassword(username, getBcryptEncoder().encode(password));

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

	public User save(User user) {
		return repository.save(user);
	}

	public void delete(User user) {
		repository.delete(user);
	}

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

    public ValidationResponse validate(User user, Mapping mapping) {

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

			if (mapping.equals(Mapping.POST_UI) || mapping.equals(Mapping.POST_API)) {
				user.setPassword(getBcryptEncoder().encode(user.getPassword()));
			}

			if (mapping.equals(Mapping.PUT_UI) || mapping.equals(Mapping.PUT_API) || mapping.equals(Mapping.PATCH_UI) || mapping.equals(Mapping.PATCH_API)) {
				User userFromDatabase = getById(user.getId());

				// SHOULD BE IMPOSSIBLE SINCE PUT AND PATCH REQUIRE ID IN URL AND IF NOT FOUND THEN ERROR IS THROWN PRIOR TO THIS
				if (userFromDatabase == null) {
					System.err.println("Validation of USER error. USER with ID: " + user.getId() + " was not found in database. Request mapping: " + mapping.toString());
					return new ValidationResponse(false, "user not found with ID: " + user.getId());
				} else {
					user.setPassword(userFromDatabase.getPassword());
				}
			}
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

		if (user.getFirstName() != null) {
			if (user.getFirstName().length() <= 0) {
				return new ValidationResponse(false, "first name cannot be empty");
			}
		}

		if (user.getLastName() != null) {
			if (user.getLastName().length() <= 0) {
				return new ValidationResponse(false, "last name cannot be empty");
			}
		}


		/*
		USER MUST BE IN ORGANISATION
		ORGANISATION MUST HAVE AT LEAST ID OR NAME
		 */
		if (user.getOrganisation() != null) {

			if (user.getOrganisation().getId() == null && user.getOrganisation().getName() == null) {
				return new ValidationResponse(false, "organisation must have at least ID or Name");
			}

			boolean set = false;

			if (user.getOrganisation().getId() != null) {

				Organisation organisationFromDatabase = organisationService.getById(user.getOrganisation().getId());

				if (organisationFromDatabase != null) {
					user.setOrganisation(organisationFromDatabase);
				} else {
					user.getOrganisation().setId(null);
				}

				set = true;
			}

			if (user.getOrganisation().getName() != null && user.getOrganisation().getName().length() > 0) {
				if (!set) {
					Organisation organisationFromDatabase = organisationService.getByName(user.getOrganisation().getName());

					if (organisationFromDatabase != null) {
						user.setOrganisation(organisationFromDatabase);
					}
				}
			}
		} else {
			return new ValidationResponse(false, "user must be in organisation");
		}

		if (user.getRole() == null || user.getRole().getId() == null) {
			user.setRole(roleRepository.findByName(StringUtils.ROLE_USER));
		}

		return new ValidationResponse(true, "validation success");
    }



	public PasswordEncoder getBcryptEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User userFromDatabase = getByUsername(username);
		if (userFromDatabase == null) {
			throw new UsernameNotFoundException("username not found");
		}

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(userFromDatabase.getRole().getName()));

		return new org.springframework.security.core.userdetails.User(userFromDatabase.getUsername(), userFromDatabase.getPassword(), authorities);
	}
}
