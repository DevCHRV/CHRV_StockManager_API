package be.chrverviers.stockmanager.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import be.chrverviers.stockmanager.Repositories.UserRepository;
import be.chrverviers.stockmanager.Services.CustomUserDetailsService;
import be.chrverviers.stockmanager.Services.JwtService;

@RestController
@RequestMapping(value = "api/auth")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	UserRepository userRepository;
	@Autowired
	JwtService jwtService;
	@Autowired
	CustomUserDetailsService customUserDetailsService;

	@PostMapping(value = "/login")
	public ResponseEntity<String> authenticateUser(@RequestBody LoginDTO loginDto){
		try {
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
	        //If the user is found, we create and return the token
	        return new ResponseEntity<String>(jwtService.generateToken(user), HttpStatus.OK);
		} catch(UsernameNotFoundException e) {
			//This error is thrown when we fail to add the user to the application's database
	        return new ResponseEntity<>("Une erreur s'est produite lors de l'ajout de l'utilisateur à la BD.", HttpStatus.BAD_REQUEST);
		} catch(AuthenticationException e) {
			//This error is thrown when the Active Directory denies the connection for the given credentials
			//It means that either the credentials are wrong or the user isn't member of the OU=Informatique
	        return new ResponseEntity<>("Cet utilisateur n'existe pas ou n'est pas autorisé !", HttpStatus.BAD_REQUEST);
		}
    }
}
