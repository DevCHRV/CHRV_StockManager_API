package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;
import be.chrverviers.stockmanager.Repositories.LicenceTypeRepository;

@RestController
@RequestMapping(value = "api/licence", produces="application/json")
public class LicenceController {

	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	LicenceTypeRepository typeRepo;
	
    private Logger logger = LoggerFactory.getLogger(LicenceController.class);
	
	/**
	 * Simple GET method
	 * @return all the licences
	 */
	@GetMapping
	public @ResponseBody ResponseEntity<List<Licence>> get() {
		return new ResponseEntity<List<Licence>>(licenceRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the licence you're looking for
	 * @return the licence you're looking for or an error message
	 */
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		Licence l = licenceRepo.findById(id).orElse(null);
		if(l==null)
			return new ResponseEntity<Object>("Cette licence n'existe pas !", HttpStatus.BAD_REQUEST);
		return new ResponseEntity<Object>(l, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param licence the value of the licence you're looking to update
	 * @return the updated licence or an error message
	 */
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Licence licence){
		logger.info(String.format("User '%s' is updating Licence with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=licence.getId()) {
			logger.error(String.format("User '%s' has failed to update Licence with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("Cette licence n'existe pas !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Save the licence
			licenceRepo.save(licence, licence.getId());
			
			logger.error(String.format("User '%s' has successfully updated Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>(licenceRepo.findById(licence.getId()), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' has failed to update Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Simple POST method
	 * @param licence the licence you're looking to save
	 * @return the licence with it's generated id or an error message
	 */
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Licence licence) {
		logger.info(String.format("User '%s' is creating a new Licence", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
		if(licence.getType()==null) {
			logger.info(String.format("User '%s' failed to create a new Licence due to bad request: bad 'type'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le type ne peut être vide !", HttpStatus.BAD_REQUEST);
		}
		if(licence.getDescription()==null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'description'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La description ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(licence.getValue()==null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'value'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La valeur ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Save the licence
			licence.setId(licenceRepo.create(licence));
			logger.info(String.format("User '%s' has succesfully created a new Licence with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), licence.getId()));
			return new ResponseEntity<Object>(licence, HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' failed to create a new Intervention due to bad request", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
