package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.LicenceType;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Repositories.LicenceTypeRepository;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/licence/type", produces= "application/json")
@Transactional
public class LicenceTypeController {

	@Autowired
	LicenceTypeRepository typeRepo;
	
	/**
	 * Simple GET method
	 * @return all the LicenceTypes
	 */
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<List<LicenceType>> get() {
		return new ResponseEntity<List<LicenceType>>(typeRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the licence type you're looking for
	 * @return the licence type or null
	 */
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<LicenceType> findById(@PathVariable("id") int id) {
		return new ResponseEntity<LicenceType>(typeRepo.findById(id).orElse(null), HttpStatus.OK);
	}
	
	/**
	 * Simple POST method
	 * @return the licence type with it's generated id or an error message
	 */
	@PostMapping(value = "/save")
	public @ResponseBody ResponseEntity<LicenceType> save() {
		//TODO the method
		return null;
//		LicenceType t = new LicenceType();
//		t.setName("Random");
//		return new ResponseEntity<LicenceType>(typeRepo.save(t), HttpStatus.OK);
	}
}
