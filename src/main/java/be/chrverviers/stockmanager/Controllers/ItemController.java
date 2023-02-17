package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/item", produces="application/json")
public class ItemController {

	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	TypeRepository typeRepo;
	
	@GetMapping
	public @ResponseBody ResponseEntity<List<Item>> get() {
		return new ResponseEntity<List<Item>>(itemRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		Item item = itemRepo.findById(id).orElse(null);
		if(item == null)
			return new ResponseEntity<Object>("Cet item n'existe pas !", HttpStatus.BAD_REQUEST);
		List<Licence> licences = licenceRepo.findForItem(item);
		item.setLicence(licences);
		return new ResponseEntity<Object>(item, HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}/intervention")
	public @ResponseBody ResponseEntity<Object> getInterventionForId(@PathVariable("id") int id) {
		return new ResponseEntity<Object>(interventionRepo.findForItemId(id), HttpStatus.OK);
	}
	
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Item item){
		try {
			itemRepo.save(item, id);
			licenceRepo.saveAll(item.getLicence());
			//licenceRepo.saveAll( item.getLicence());
			return new ResponseEntity<Object>(itemRepo.findById(item.getId()), HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Item item) {
		if(item.getSerial_number()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(item.getReference()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(item.getType()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		try {
			int tmp = itemRepo.create(item);
			for(Licence l:item.getLicence()) {
				l.setItem(new Item(tmp));
			}
			licenceRepo.saveAll(item.getLicence());
			return new ResponseEntity<Integer>(tmp, HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
