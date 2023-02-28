package be.chrverviers.stockmanager.Controllers;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.DTO.LoginDTO;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.RoleRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;
import be.chrverviers.stockmanager.Services.CustomUserDetailsService;
import be.chrverviers.stockmanager.Services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;

@RestController
@RequestMapping(value = "api/auth")
public class AuthController {
	
	/**
	 * NB: There is no logout method because Spring is configured to use JWT Authentication which means that the 
	 * User's informations are created when Spring validates the token and are wiped after the request's completion
	 */
	
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	JwtService jwtService;
	@Autowired
	CustomUserDetailsService customUserDetailsService;

	@PostMapping(value = "/login")
	public ResponseEntity<Object> authenticateUser(@RequestBody LoginDTO loginDto, HttpServletResponse request){
		try {
			loginDto.setUsername(loginDto.getUsername().toUpperCase());
			//Try to authenticate the user with the Active Directory
	        authenticationManager.authenticate(
	        		new UsernamePasswordAuthenticationToken(
	        				loginDto.getUsername(), loginDto.getPassword()
	        				)
	        		);
	        //If we arrive here, then it means that the User exists in the Active directory
	        User user = userRepository.findByUsername(loginDto.getUsername()).orElse(null);
	        //If it doesn't exist in the database yet, we try to add it first
	        if(user==null) {
	        	//This method will build and save a new user with infomations obtained by querying the Active directory
	        	customUserDetailsService.buildUser(loginDto.getUsername());
	        	//Now we try to get it from the DB again, if it doesn't exist then it means something went wrong with the User creation
	        	user = userRepository.findByUsername(loginDto.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found !"));
	        }
	        user.setRoles(roleRepository.findForUser(user));
	        //If the user is found, we create and return the token
	        String token = jwtService.generateToken(user);
	        
	        Cookie cookie = new Cookie("auth.jwt_token", token);
    	        cookie.setSecure(false);
    	        cookie.setHttpOnly(true);
    	        cookie.setPath("/");
    	        cookie.setMaxAge(1 * 60 * 60 * 24);
	        request.addCookie(cookie);
	        
	        return new ResponseEntity<Object>(user, HttpStatus.OK);
		} catch(ExpiredJwtException e) {
	        return new ResponseEntity<>("Votre session a expiré, veuillez vous reconnecter.", HttpStatus.UNAUTHORIZED);
		} catch(UsernameNotFoundException e) {
			//This error is thrown when we fail to add the user to the application's database
	        return new ResponseEntity<>("Une erreur s'est produite lors de l'ajout de l'utilisateur à la BD.", HttpStatus.BAD_REQUEST);
		} catch(AuthenticationException e) {
			//This error is thrown when the Active Directory denies the connection for the given credentials
			//It means that either the credentials are wrong or the user isn't member of the OU=Informatique
	        return new ResponseEntity<>("Cet utilisateur n'existe pas ou n'est pas autorisé !", HttpStatus.BAD_REQUEST);
		}
    }
	
	@PostMapping(value = "/logout")
	public ResponseEntity<Object> logout(HttpServletResponse request){
        Cookie cookie = new Cookie("auth.jwt_token", null);
	        cookie.setSecure(true);
	        cookie.setHttpOnly(true);
	        cookie.setPath("/");
	        cookie.setMaxAge(0);
        request.addCookie(cookie);
        return new ResponseEntity<Object>(HttpStatus.OK);
	}
}
