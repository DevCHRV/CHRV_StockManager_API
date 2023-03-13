package be.chrverviers.stockmanager.Controllers;

import java.util.List;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Domain.Models.Unit;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.RoomRepository;
import be.chrverviers.stockmanager.Repositories.UnitRepository;

@RestController
@RequestMapping(value = "api/unit", produces= "application/json")
@Transactional
public class UnitController {

	@Autowired
	UnitRepository unitRepo;
	
	@Autowired
	RoomRepository roomRepo;
	
    private Logger logger = LoggerFactory.getLogger(UnitController.class);

	/**
	 * Simple GET method
	 * @return all the items types
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<List<Unit>> get() {
		return new ResponseEntity<List<Unit>>(unitRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the item type you're looking for
	 * @return the type or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> findById(@PathVariable("id") int id) {
		Unit t = unitRepo.findById(id).orElse(null);
		if(t==null)
			return new ResponseEntity<Object>("Ce service n'existe pas !", HttpStatus.BAD_REQUEST);
		t.setRooms(roomRepo.findForUnit(t));
		return new ResponseEntity<Object>(t, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param id the id of the unit you're looking to update
	 * @return the updated unit or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Unit unit, HttpServletRequest request){
		logger.info(String.format("User '%s' is updating Unit with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=unit.getId()) {
			logger.warn(String.format("User '%s' has failed to update Unit with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("Ce service n'existe pas !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Save the unit
			unitRepo.save(unit, id);
			
			List<Room> dbRooms = roomRepo.findForUnit(unit);
						
			for(Room room : dbRooms) {
				if(unit.getRooms().contains(room)) {
					//We save the existing room
					roomRepo.save(room, room.getId());
					//We remove it from the list, so that what is left inside the list after the loop is the rooms that needs to be created
					unit.getRooms().remove(room);
				} else {
					//We it doesn't exist in the current unit list, then it means it was deleted in the front-end
					//So we try to delete it. If it fails it's almost certainly because of a ForeignKey exception
					//That mans that the room is currently linked to something and can't be deleted, so we send back an error
					boolean deleted = roomRepo.delete(room.getId());
					//We remove it from the list, so that what is left inside the list after the loop is the rooms that needs to be created
					unit.getRooms().remove(room);
					if(!deleted) {
						logger.error(String.format("User '%s' is failed to update Unit with id:'%s'. He tried to delete a Room that was still in use.", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
						return new ResponseEntity<Object>("La modification à échoué ! Vous ne pouvez pas supprimer un Local qui est utilisé !", HttpStatus.BAD_REQUEST);
					}
				}
			}
			//Now that the unit's room list only contains the new ones, we create them
			for(Room room : unit.getRooms()) {
				//We need to set the unit of the new room, because it can't be null.
				//To be sure we simply set the unit that is currently being modifier as the room's unit
				room.setUnit(unit);
				roomRepo.create(room);
			}
			
			logger.info(String.format("User '%s' has successfully updated Unit with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>(this.findById(id), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' is failed to update Unit with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}	
	
	/**
	 * Simple save method
	 * @param unit the Unit you want to save
	 * @return the unit with it's generated id or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Unit unit) {
		logger.info(String.format("User '%s' is creating a new Unit", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
		if(unit.getName()==null) {
			logger.info(String.format("User '%s' failed to create a new Unit due to bad request: bad 'name'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le nom ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		try {
			//Create the unit
			unit.setId(unitRepo.create(unit));
			//Add it's reference to the rooms
			for(Room room:unit.getRooms()) {
				//We set the reference as a "new" Unit because otherwise it would make a cyclic loop between the room and the unit 
				//That would cause a stack overflow when returning it
				room.setUnit(new Unit(unit.getId()));
				//Save the rooms
				roomRepo.create(room);
			}
			logger.info(String.format("User '%s' has successfully created a new Unit with id:'%s''", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), unit.getId()));
			return new ResponseEntity<Object>(unit, HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' failed to create a new Unit due to bad request", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
