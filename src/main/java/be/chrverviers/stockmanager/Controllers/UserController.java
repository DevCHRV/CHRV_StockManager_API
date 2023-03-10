package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.RoleRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;
import be.chrverviers.stockmanager.Services.CustomUserDetailsService;

@RestController
@RequestMapping(value = "api/user", produces="application/json")
@Transactional
public class UserController {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	RoleRepository roleRepo;
	
	@Autowired 
	CustomUserDetailsService userDetailsService;
	
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    
	/**
	 * Simple GET method
	 * @return all the users
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<User>> get() {
		return new ResponseEntity<List<User>>(userRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @return all the users
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping("/ldap")
	public @ResponseBody ResponseEntity<List<User>> getLDAP() {
		return new ResponseEntity<List<User>>(userDetailsService.getLDAPUsers(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the user you're looking for
	 * @return the User that has the requested id or an error message
	 */
	@PreAuthorize("hasRole('ADM')")
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the intervention
		User user = userRepo.findById(id).orElse(null);
		if(user==null)
			return new ResponseEntity<Object>("Cet utilisateur n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and set it's licences
		user.setRoles(roleRepo.findForUser(user));
		return new ResponseEntity<Object>(user, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param id the id of the user you're looking to update
	 * @return the updated user or an error message
	 */
	@PreAuthorize("hasRole('ADM')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody User user, HttpServletRequest request){
		logger.info(String.format("User '%s' is updating User with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=user.getId()) {
			logger.warn(String.format("User '%s' did not have the required authority to update User with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("Cet utilisateur n'existe pas !", HttpStatus.BAD_REQUEST);
		}
				
		if(roleRepo.findForUser(user).stream().anyMatch(role->role.getName().equals("ROLE_ADM"))) {
			logger.warn(String.format("User '%s' cannot update administrator User with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("Vous ne pouvez pas modifier un compte administrateur !", HttpStatus.BAD_REQUEST);
		}
		
		try {
			//Save the user
			userRepo.save(user, id);
			//Detach all his roles
			userRepo.detachAllRoles(user);
			//Attach the new ones
			userRepo.attachAll(user.getRoles(), user);
			
			logger.info(String.format("User '%s' has successfully updated User with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));

			return new ResponseEntity<Object>(userRepo.findById(user.getId()), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' is failed to update User with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}	
}
