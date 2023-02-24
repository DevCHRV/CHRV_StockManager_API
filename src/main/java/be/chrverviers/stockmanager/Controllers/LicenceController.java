package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import be.chrverviers.stockmanager.Repositories.LicenceRepository;
import be.chrverviers.stockmanager.Repositories.LicenceTypeRepository;

@RestController
@RequestMapping(value = "api/licence", produces="application/json")
public class LicenceController {

	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	LicenceTypeRepository typeRepo;
	
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
	@PutMapping(value="/")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Licence licence){
		if(id!=licence.getId())
			return new ResponseEntity<Object>("Cette licence n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			//Save the licence
			licenceRepo.save(licence, licence.getId());
			return new ResponseEntity<Object>(licenceRepo.findById(licence.getId()), HttpStatus.OK);
		} catch(Exception e) {
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
		if(licence.getType()==null) {
			return new ResponseEntity<Object>("Le type ne peut être vide !", HttpStatus.BAD_REQUEST);
		}
		if(licence.getDescription()==null) {
			return new ResponseEntity<Object>("La description ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(licence.getValue()==null) {
			return new ResponseEntity<Object>("La valeur ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Save the licence
			licence.setId(licenceRepo.create(licence));
			return new ResponseEntity<Object>(licence, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
