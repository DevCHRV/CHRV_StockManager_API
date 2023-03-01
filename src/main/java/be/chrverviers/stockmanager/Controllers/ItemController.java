package be.chrverviers.stockmanager.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
	
	/**
	 * Simple GET method
	 * @return all the items
	 */
	@PreAuthorize("hasRole('PGM')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<Item>> get() {
		return new ResponseEntity<List<Item>>(itemRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method 
	 * @param id the id of the item you're looking for
	 * @return the item that has the requested id or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the item
		Item item = itemRepo.findById(id).orElse(null);
		if(item == null)
			return new ResponseEntity<Object>("Cet item n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and set it's licences
		item.setLicence(licenceRepo.findForItem(item));
		item.setInterventions(interventionRepo.findForItem(item));
		return new ResponseEntity<Object>(item, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param id the id of the item you're looking to update
	 * @return the update item or an error message
	 */
	@PreAuthorize("hasRole('PGM')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Item item){
		if(id!=item.getId())
			return new ResponseEntity<Object>("Cet item n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			//Save the item
			itemRepo.save(item, id);
			//As this is a leftover from JPA the front-end is sending full fledged item and licence associations
			//We used to save the licences
			//We could probably just use the new "attachAll" method but I don't want to break anything on a Friday
			licenceRepo.saveAll(item.getLicence());
			return new ResponseEntity<Object>(itemRepo.findById(item.getId()), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Simple save method
	 * @param item the item you want to save
	 * @return the item with it's generated id or an error message
	 */
	@PreAuthorize("hasRole('PGM')")
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Item item) {
		if(item.getSerial_number()==null) {
			return new ResponseEntity<Object>("Le numéro de série ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getReference()==null) {
			return new ResponseEntity<Object>("La référence ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getDescription()==null) {
			return new ResponseEntity<Object>("La description ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getPurchased_at()==null) {
			return new ResponseEntity<Object>("La date d'achat ne peut pas être vide", HttpStatus.BAD_REQUEST);
		}
		if(item.getWarranty_expires_at()==null) {
			return new ResponseEntity<Object>("La date d'expiration de la garantie ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getType()==null) {
			return new ResponseEntity<Object>("Le type ne peut pas être vide", HttpStatus.BAD_REQUEST);
		}
		try {
			//Create the item
			item.setId(itemRepo.create(item));
			//Add it's reference to the licences
			for(Licence l:item.getLicence()) {
				l.setItem(item);
			}
			//Save the licences
			licenceRepo.saveAll(item.getLicence());
			return new ResponseEntity<Object>(item, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}

}
