package com.example.demo;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.Role;
import com.example.demo.database.models.user.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.OrganisationService;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;


@SpringBootApplication
@ComponentScan(basePackages = {"com.example.demo"})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganisationService organisationService;

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() {
		if (!userService.systemAdminExists()) {

			Role userRole = 			 createRoleIfNotFound(Constants.ROLE_USER);
			Role organisationAdminRole = createRoleIfNotFound(Constants.ROLE_ORGANISATION_ADMIN);
			Role systemAdminRole = 		 createRoleIfNotFound(Constants.ROLE_SYSTEM_ADMIN);

			Organisation organisation = new Organisation();
			organisation.setName("Vedia");

			organisationService.save(organisation);

			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("password"));
			admin.setFirst_name("F_NAME");
			admin.setLast_name("L_NAME");
			admin.setEmail("test@mail.com");
			admin.setRole(systemAdminRole);
			admin.setOrganisation(organisation);

			userService.save(admin);
		}
	}

	@Transactional
	private Role createRoleIfNotFound(String name) {
		Role role = roleRepository.findByName(name);
		if (role == null) {
			role = new Role();
			role.setName(name);
			role = roleRepository.save(role);
		}
		return role;
	}
}
