package com.example.demo;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.Role;
import com.example.demo.database.models.user.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;

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

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() {


		if (!userService.systemAdminExists()) {

			Role userRole = 				createRoleIfNotFound(StringUtils.ROLE_USER,					1);
			Role organisationAdminRole = 	createRoleIfNotFound(StringUtils.ROLE_ORGANISATION_ADMIN,	2);
			Role systemAdminRole = 			createRoleIfNotFound(StringUtils.ROLE_SYSTEM_ADMIN,			3);


			Organisation organisation = new Organisation();
			organisation.setName("Vedia");


			User systemAdmin = new User();
			systemAdmin.setUsername("system_admin");
			systemAdmin.setPassword(userService.getBcryptEncoder().encode("password"));
			systemAdmin.setRole(systemAdminRole);
			systemAdmin.setOrganisation(organisation);

			userService.save(systemAdmin);
		}
	}

	@Transactional
	private Role createRoleIfNotFound(String name, Integer level) {
		Role role = roleRepository.findByName(name);
		if (role == null) {
			role = new Role();
			role.setName(name);
			role.setLevel(level);
			role = roleRepository.save(role);
		}
		return role;
	}
}
