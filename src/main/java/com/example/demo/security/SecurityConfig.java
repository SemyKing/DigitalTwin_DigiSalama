package com.example.demo.security;

import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private UserService userService;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load user for matching credentials
		// Use BCryptPasswordEncoder
		auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Order(1)
	@Configuration
	public static class JWT_SecurityConfig extends WebSecurityConfigurerAdapter {

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		@Autowired
		private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

		@Autowired
		private JwtRequestFilter jwtRequestFilter;


		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.antMatcher("/api2/**")
					.cors()
						.and()
					.csrf()
						.disable()

					.authorizeRequests()
//						.antMatchers("/api2/authenticate").permitAll()
//						.anyRequest().authenticated()
////					.antMatchers("/api2/**").authenticated()
//
//					// all other requests need to be authenticated
//					.and()
//					.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
//					.and()

					.antMatchers("/api2/**").permitAll().and()

					// make sure we use stateless session session won't be used to store user's state.
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

			// Add a filter to validate the tokens with every request
			http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		}
	}


	@Order(2)
	@Configuration
	public static class UI_SecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
					.antMatcher("/api1/**")
					.csrf().disable()
//
					.authorizeRequests()
//					.antMatchers("/resources/**").permitAll()
//					.antMatchers("/api1/login").permitAll()
//
//					.anyRequest().authenticated()
//					.and()
//						.httpBasic()


					.antMatchers("/api1/**").permitAll().and()


//					// make sure we use stateless session session won't be used to store user's state.
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
	}
}



