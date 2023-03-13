package be.chrverviers.stockmanager.Controllers;

import java.util.List;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;
import be.chrverviers.stockmanager.Repositories.RoomRepository;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/item", produces="application/json")
@Transactional
public class ItemController {

	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	TypeRepository typeRepo;
	
	@Autowired
	RoomRepository roomRepo;
	
    private Logger logger = LoggerFactory.getLogger(ItemController.class);
    
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
		logger.info(String.format("User '%s' is updating Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=item.getId()) {
			logger.error(String.format("User '%s' has failed to update Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("Cet item n'existe pas !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Save the item
			itemRepo.save(item, id);
			//As this is a leftover from JPA the front-end is sending full fledged item and licence associations
			//We used to save the licences
			//We could probably just use the new "attachAll" method but I don't want to break anything on a Friday
			licenceRepo.saveAll(item.getLicence());
			
			logger.info(String.format("User '%s' has succesfully updated Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));

			return new ResponseEntity<Object>(itemRepo.findById(item.getId()), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' has failed to update Item with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
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
		logger.info(String.format("User '%s' is creating a new Item", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
		if(item.getSerialNumber()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'serial_number'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le numéro de série ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getReference()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'reference'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La référence ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getDescription()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'description'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La description ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getPurchasedAt()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'purchase date'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La date d'achat ne peut pas être vide", HttpStatus.BAD_REQUEST);
		}
		if(item.getWarrantyExpiresAt()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'warranty expiration date'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La date d'expiration de la garantie ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(item.getType()==null) {
			logger.info(String.format("User '%s' failed to create a new Item due to bad request: bad 'type'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le type ne peut pas être vide", HttpStatus.BAD_REQUEST);
		}
		try {
			//Set the default room
			item.setRoom(roomRepo.findByName("Stock").orElse(null));
			//Create the item
			item.setId(itemRepo.create(item));
			//Add it's reference to the licences
			for(Licence l:item.getLicence()) {
				l.setItem(item);
			}
			//Save the licences
			licenceRepo.saveAll(item.getLicence());
			logger.info(String.format("User '%s' has successfully created a new Item with id:'%s''", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), item.getId()));
			return new ResponseEntity<Object>(item, HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' failed to create a new Item due to bad request", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}

}
