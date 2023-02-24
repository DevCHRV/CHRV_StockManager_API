package be.chrverviers.stockmanager.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class OrderController {

	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	OrderRepository orderRepo;
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	TypeRepository typeRepo;
	
	/**
	 * Simple GET method
	 * @return all the orders
	 */
	@GetMapping
	public @ResponseBody ResponseEntity<List<Order>> get() {
		return new ResponseEntity<List<Order>>(orderRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the order you're looking for
	 * @return the order or an error message
	 */
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the order
		Order order = orderRepo.findById(id).orElse(null);
		if(order == null)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and sets it's items
		order.setItems(itemRepo.findForOrder(order));
		//Get and sets it's types (and the quantity related to it)
		order.setTypes(typeRepo.findForOrder(order));
		return new ResponseEntity<Object>(order, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param id the id of the order you're looking to update
	 * NB: you cannot update an order's items because the relations is represented by a Map which Spring absolutely can't 
	 * Deserialize for some reason, so must conform to the OrderDTO interface instead
	 * @param request the order you wan't to update
	 * @return the order you're looking to update or an error message
	 */
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody OrderDTO request){
		if(request.getId() != id)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		//We try to transform the DTO into an Order object so that it can't be used as an order by the rest of the api
		Order order = request.toOrder();
		if(order.getId() != id)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			//When we update it then it means it's received
			order.setIsReceived(true);
			//Save the order
			orderRepo.save(order, id);
			//We set the order's items as received
			itemRepo.receiveForOrder(order);
			//As we are working with a DTO that isn't complete, we're sending back the completely rebuilt object
			//To avoid problems
			return this.getById(order.getId());
		} catch(Exception e) {
			return new ResponseEntity<Object>("La réception à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Simple POST method
	 * @param request the id of the order you're looking to save
	 * NB: you cannot update an order's items because the relations is represented by a Map which Spring absolutely can't 
	 * Deserialize for some reason, so must conform to the OrderDTO interface instead
	 * @return the order with it's generated id or an error message
	 */
  	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody OrderDTO request) {
		try {
			//We try to transform the DTO into an Order object so that it can't be used as an order by the rest of the api
			Order order = request.toOrder();
			//We get the logged user's information for Spring Context
			User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			//We set the order's user
			order.setUser(u);
			//We save the order
			order.setId(orderRepo.create(order));
			//We create an item instance for each item of the order and we attach them to the order
			orderRepo.attachAllItemId(order, itemRepo.createAll(order.getItems()));
			//We build the map that contains the type and the related quantity as key and value
			Map<Type, Integer> map = new HashMap<Type, Integer>();
			for(Item item: order.getItems()) {
				if(map.containsKey(item.getType())){
					map.merge(item.getType(), 1, Integer::sum);
				}else {
					map.put(item.getType(), 1);
				}
			}
			//We attach all the types and quantity to the order
			orderRepo.attachAll(order, map);
			return new ResponseEntity<Integer>(order.getId(), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
}
