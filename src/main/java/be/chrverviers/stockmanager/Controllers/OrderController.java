package be.chrverviers.stockmanager.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;import be.chrverviers.stockmanager.Domain.DTO.OrderDTO;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.OrderRepository;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/order", produces="application/json")
public class OrderController {

	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	OrderRepository orderRepo;
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	TypeRepository typeRepo;
	
	@GetMapping
	public @ResponseBody ResponseEntity<List<Order>> get() {
		return new ResponseEntity<List<Order>>(orderRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		Order order = orderRepo.findById(id).orElse(null);
		if(order == null)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		order.setItems(itemRepo.findForOrder(order));
		order.setTypes(typeRepo.findForOrder(order));
		return new ResponseEntity<Object>(order, HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}/intervention")
	public @ResponseBody ResponseEntity<Object> getInterventionForId(@PathVariable("id") int id) {
		return new ResponseEntity<Object>(interventionRepo.findForItemId(id), HttpStatus.OK);
	}
	
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody OrderDTO request){
		Order order = request.toOrder();
		if(order.getId() != id)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			order.setIsReceived(true);
			orderRepo.save(order, id);
			itemRepo.receiveForOrder(order);
			return new ResponseEntity<Object>(order, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La réception à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
  	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody OrderDTO request) {
		try {
			Order order = request.toOrder();
			User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			order.setUser(u);
			order.setId(orderRepo.create(order));
			orderRepo.attachAllItemId(order, itemRepo.createAll(order.getItems()));
			Map<Type, Integer> map = new HashMap<Type, Integer>();
			for(Item item: order.getItems()) {
				if(map.containsKey(item.getType())){
					map.merge(item.getType(), 1, Integer::sum);
				}else {
					map.put(item.getType(), 1);
				}
			}
			orderRepo.attachAll(order, map);
			return new ResponseEntity<Integer>(order.getId(), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
