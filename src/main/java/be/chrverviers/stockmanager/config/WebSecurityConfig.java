package be.chrverviers.stockmanager.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import be.chrverviers.stockmanager.Repositories.UserRepository;


@Configuration
public class WebSecurityConfig {
	
	private JwtAuthenticationFilter jwtAuthFilter;
	
	public WebSecurityConfig(JwtAuthenticationFilter jwtFilter, AuthenticationManager authenticationManager) {
		this.jwtAuthFilter = jwtFilter;
	}
	
	/**
	* Setup Spring Http Security
	* We configure CORS for our application and require HTTPS connection
	*/
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		//Disable default CORS to allow it to be overwritten by the Bean bellow
		http.cors().and().csrf().disable();
		//Here we authorize access to the login url for everyone
		http.authorizeRequests()
			//.expressionHandler(webSecurityExpressionHandler())
				.antMatchers("/api/auth/login").permitAll().and()
			.authorizeRequests()
				.antMatchers("/api/**").authenticated();
		//Specify that we don't need Spring to store user session info
		http.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		//.authenticationProvider(authenticationProvider)
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	/*
	 * Setup our own CORS configuration
	 * We allow both the development URLs
	 * We allow the use of all the HTTP Verbs used for API calls
	 * TODO: Allow prod URLs
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cors = new CorsConfiguration();		
		//Straightforward: we choose which headers are allowed to reach our api
		cors.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Set-Cookie", "Access-Control-Allow-Credentials", "Cookie"));
		//We choose the allowed origins
		cors.setAllowedOrigins(Arrays.asList(
				"https://localhost:4200",
				"http://localhost:4200",
				"https://localhost",
				"http://localhost"
				));
		//The allowed methods -> here I allow all HTTP Verbs as we're going to try to make the API Restful compliant
		cors.setAllowedMethods(Arrays.asList(
				"GET", "POST", "PUT", "DELETE", "OPTIONS"
				));
		cors.setAllowCredentials(true);
		//Here we choose which urls are accessible for the allowed origins
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);
		return source;
	}
	
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		String hierarchy = "ROLE_ADM > ROLE_PGM \n ROLE_PGM > ROLE_TEC";
		roleHierarchy.setHierarchy(hierarchy);
		return roleHierarchy;
	}
	
//	@Bean
//	public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
//	    DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
//	    expressionHandler.setRoleHierarchy(roleHierarchy());
//	    return expressionHandler;
//	}
	

}
