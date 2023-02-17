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
	
	@GetMapping
	public @ResponseBody ResponseEntity<List<Licence>> get() {
		return new ResponseEntity<List<Licence>>(licenceRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Licence> getById(@PathVariable("id") int id) {
		return new ResponseEntity<Licence>(licenceRepo.findById(id).orElse(null), HttpStatus.OK);
	}
	
	@PutMapping(value="/")
	public @ResponseBody ResponseEntity<Object> update(@RequestBody Licence licence){
		try {
			licenceRepo.save(licence, licence.getId());
			return new ResponseEntity<Object>(licenceRepo.findById(licence.getId()), HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Licence licence) {
		if(licence.getType()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(licence.getDescription()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(licence.getValue()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		try {
			int tmp = licenceRepo.create(licence);
			return new ResponseEntity<Integer>(tmp, HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
