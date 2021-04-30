package com.example.demo.security;

import com.example.demo.database.services.UserService;
import com.example.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	// UI COOKIE TOKEN VALID FOR 24 HOURS
	private final static int SESSION_COOKIE_VALIDITY = 86400000;

	@Autowired
	private UserService userService;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
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
					.antMatcher(Constants.JSON_API + "/**")
					.authorizeRequests()
						.antMatchers(Constants.JSON_API + "/authenticate").permitAll()
//						.antMatchers(Constants.JSON_API + "/**").authenticated()
						.antMatchers(Constants.JSON_API + "/**").permitAll()
						.and()
					.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
					.and()

					.cors().and().csrf().disable()

					// make sure we use stateless session session won't be used to store user's state.
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

			// Add a filter to validate the tokens with every request
			http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		}
	}


	@Configuration
	public static class UI_SecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
//			http
//					.authorizeRequests()
//						.antMatchers("/", "/login", "/signin", "/logout").permitAll()
//						.antMatchers(Constants.UI_API + "/**").authenticated()
//						.and()
//					.formLogin()
//						.loginProcessingUrl("/signin")
//						.loginPage("/login").permitAll()
//						.defaultSuccessUrl(Constants.UI_API + "/", true)
//						.usernameParameter("username")
//						.passwordParameter("password")
//						.and()
//					.csrf().disable()
//
//					.rememberMe().tokenValiditySeconds(SESSION_COOKIE_VALIDITY).key("mySecret!").rememberMeParameter("checkRememberMe")
//					.and()
//					.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login");
		}
	}
}



