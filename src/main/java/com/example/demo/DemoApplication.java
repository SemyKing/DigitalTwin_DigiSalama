package com.example.demo;

import com.example.demo.database.models.Role;
import com.example.demo.database.models.User;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import javax.transaction.Transactional;

@Slf4j
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

			User systemAdmin = new User();
			systemAdmin.setUsername("system_admin");
			systemAdmin.setPasswordHash(StringUtils.generateHashFromString("password"));
			systemAdmin.setRole(systemAdminRole);
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
			roleRepository.save(role);
		}
		return role;
	}
}
