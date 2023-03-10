package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Repositories.RoomRepository;
import be.chrverviers.stockmanager.Repositories.UnitRepository;

@RestController
@RequestMapping(value = "api/room", produces= "application/json")
@Transactional
public class RoomController {

	@Autowired
	UnitRepository unitRepo;
	
	@Autowired
	RoomRepository roomRepo;
	
    private Logger logger = LoggerFactory.getLogger(RoomController.class);

	/**
	 * Simple GET method
	 * @return all the items types
	 */
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<List<Room>> get() {
		return new ResponseEntity<List<Room>>(roomRepo.findAll(), HttpStatus.OK);
	}
}
