package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;

@RestController
@RequestMapping(value = "api/intervention", produces="application/json")
public class InterventionController {
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	InterventionTypeRepository typeRepo;
	
	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	private JavaMailSender emailSender;
	
	@GetMapping
	public @ResponseBody ResponseEntity<List<Intervention>> get() {

		return new ResponseEntity<List<Intervention>>(interventionRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Intervention> getById(@PathVariable("id") int id) {
		Intervention intervention = interventionRepo.findById(id).orElse(null);
		intervention.setLicences(licenceRepo.findForIntervention(intervention));
		return new ResponseEntity<Intervention>(intervention, HttpStatus.OK);
	}
	
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Intervention intervention){
		try {
			interventionRepo.save(intervention, id);
			return new ResponseEntity<Object>(interventionRepo.findById(intervention.getId()), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Intervention intervention) {
		if(intervention.getDescription()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(intervention.getType()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		intervention.setUser(u);
		try{
			updateItemStatus(intervention);
			updateItemLocation(intervention);
		} catch(IllegalStateException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		try {
			intervention.setId(interventionRepo.create(intervention));
			interventionRepo.attachAll(intervention.getLicences(), intervention);
			interventionRepo.attach(intervention.getNotifier(), intervention);
			licenceRepo.attachAll(intervention.getLicences(), intervention.getItem());
			itemRepo.save(intervention.getItem());
			//TODO: Create and save the automatic report
			return new ResponseEntity<Intervention>(intervention, HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	private void updateItemLocation(Intervention intervention) {
		Item i = intervention.getItem();
		if(!intervention.getUnit().equals(i.getUnit()))
			i.setUnit(intervention.getUnit());
		if(!intervention.getRoom().equals(i.getRoom()))
			i.setRoom(intervention.getRoom());
	}
	
	private void updateItemStatus(Intervention intervention) throws IllegalStateException {
		switch(intervention.getType().getId()) {
			case 1:
				this.installItem(intervention.getItem());
				break;
			case 2:
				this.uninstallItem(intervention.getItem());
				break;
			case 3:
				this.loanItem(intervention.getItem());
				break;
			case 4:
				this.returnItem(intervention.getItem());
				break;
		}
	}
	
	private void installItem(Item i) {
	    if(!i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez pas installer un objet indisponible.");
	    
        i.setIs_placed(true);
        i.setIs_available(false);
	}
	
	private void uninstallItem(Item i) {
		if(i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez désinstaller un objet disponible");
		
		i.setIs_placed(false);
		i.setIs_available(true);
	}
	
	private void loanItem(Item i) {
	    if(!i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez pas prêter un objet indisponible.");
	    i.setIs_available(false);
	}
	
	private void returnItem(Item i) {
	    if(i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez pas retourner un objet déjà rendu.");
	    i.setIs_available(true);
	}
}
