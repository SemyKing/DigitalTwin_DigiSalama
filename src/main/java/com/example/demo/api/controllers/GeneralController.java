package com.example.demo.api.controllers;

import com.example.demo.database.models.JwtResponse;
import com.example.demo.database.models.user.User;
import com.example.demo.database.services.UserService;
import com.example.demo.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.servlet.http.HttpServletRequest;
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



	@GetMapping(value={"", "/", "/api1", "/api1/"})
	public String homePage(Model model, HttpServletRequest request) {
		return "home_page";
	}


	@GetMapping("/api1/login")
	public String loginPage() {
		return "login_page";
	}


//	@PostMapping("/api1/login")
//	public RedirectView validateLogin(@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes) throws Exception {
//
//		System.out.println("VALIDATE LOGIN");
//
//		if (user.getUsername() == null || user.getUsername().length() <= 0) {
//			redirectAttributes.addFlashAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Username is required");
//			return new RedirectView("/api1/login", true);
//		}
//
//		if (user.getPassword() == null || user.getPassword().length() <= 0) {
//			redirectAttributes.addFlashAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, "Password is required");
//			return new RedirectView("/api1/login", true);
//		}
//
//		try {
//			final UserDetails userDetails = userService.getUserDetails(user, null);
//
//			Authentication authentication = authenticate(user.getUsername(), user.getPassword(), userDetails.getAuthorities());
//
//			SecurityContextHolder.getContext().setAuthentication(authentication);
//
//			System.out.println("LOGIN SUCCESSFUL");
//
//			return new RedirectView("/api1/", true);
//
//		} catch (UsernameNotFoundException | CredentialNotFoundException | CredentialExpiredException exception) {
//			redirectAttributes.addFlashAttribute(StringUtils.ERROR_MESSAGE_ATTRIBUTE, exception.getMessage());
//
//			log.error(exception.getMessage());
//
//			return new RedirectView("/api1/login", true);
//		}
//	}


//	@RequestMapping(value = "/api1/logout", method = RequestMethod.POST)
//	public String logout(HttpServletRequest request, HttpServletResponse response) {
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		if (auth != null){
//			new SecurityContextLogoutHandler().logout(request, response, auth);
//		}
//		return "redirect:/api1/login";
//	}

	@RequestMapping(value = "/api2/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) throws Exception {

		try {
			final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

			authenticate(userDetails.getUsername(), user.getPassword(), userDetails.getAuthorities());

			final String token = jwtTokenUtil.generateToken(userDetails);

			return ResponseEntity.ok(new JwtResponse(token));
		} catch (UsernameNotFoundException e) {

			log.error(e.getMessage());

			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
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
