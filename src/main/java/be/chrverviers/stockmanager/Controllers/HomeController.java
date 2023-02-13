package be.chrverviers.stockmanager.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Repositories.ItemRepository;

@RestController
@RequestMapping(value = "api/home")
public class HomeController {
	
	/*
	 * TODO LOG
	 * WHO => WHAT => WHEN
	 */
	@RequestMapping(value = "/hello", produces="application/json")
	public @ResponseBody ResponseEntity<String> home() {
		return new ResponseEntity<String>("Hellow World !", HttpStatus.OK);
	}

}