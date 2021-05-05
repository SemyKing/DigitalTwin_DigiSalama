package com.example.demo.api.ui_controllers;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.JwtResponse;
import com.example.demo.database.services.UserService;
import com.example.demo.security.JwtTokenUtil;
import com.example.demo.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class GeneralController {

	@Autowired
	private final AuthenticationManager authenticationManager;

	@Autowired
	private final JwtTokenUtil jwtTokenUtil;

	@Autowired
	private final UserService userService;

	@GetMapping(value={"", "/"})
	public String index(Model model) {
		return "index";
	}


	@GetMapping(value={"/api1", "/api1/"})
	public String main(Model model) {
		return "main-layout";
	}


	@GetMapping("/login")
	public String loginPage() {
		return "login_page";
	}


	// SHOW CURRENT USER PROFILE
	@GetMapping("/api1/profile")
	public String profile(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null) {
			User userFromDatabase = userService.getByUsername(authentication.getName());

			if (userFromDatabase != null) {
				model.addAttribute("user", userFromDatabase);
				return "user/user_profile_page";
			}
		}

		return Constants.REDIRECT + "/";
	}


	@RequestMapping(value = "/api2/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) throws Exception {
		try {
			final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

			if (userDetails == null) {
				log.info("GeneralController -> createAuthenticationToken() userDetails is NULL");
				return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
			}

			authenticate(userDetails.getUsername(), user.getPassword(), userDetails.getAuthorities());

			final String token = jwtTokenUtil.generateToken(userDetails);

			return ResponseEntity.ok(new JwtResponse(token));
		} catch (UsernameNotFoundException e) {
			log.error("GeneralController -> createAuthenticationToken() UsernameNotFoundException: " + e.getMessage() + ", username: '" + user.getUsername() + "'");

			return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
		}
	}

	private void authenticate(String username, String password, Collection<? extends GrantedAuthority> authorities) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password, authorities));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}
