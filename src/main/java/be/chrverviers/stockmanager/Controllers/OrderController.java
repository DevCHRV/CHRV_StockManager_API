package be.chrverviers.stockmanager.Controllers;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import be.chrverviers.stockmanager.Domain.DTO.OrderCreationDTO;
import be.chrverviers.stockmanager.Domain.DTO.OrderCreationItemDTO;
import be.chrverviers.stockmanager.Domain.DTO.OrderCreationTypeDTO;
import be.chrverviers.stockmanager.Domain.DTO.OrderDTO;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.OrderRepository;
import be.chrverviers.stockmanager.Repositories.RoomRepository;
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
	
	@Autowired
	RoomRepository roomRepo;
	
    private Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	/**
	 * Simple GET method
	 * @return all the orders
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<Order>> get() {
		return new ResponseEntity<List<Order>>(orderRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the order you're looking for
	 * @return the order or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the order
		Order order = orderRepo.findById(id).orElse(null);
		if(order == null)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and sets it's items
		if(order.getIsReceived()) {
			order.setItems(itemRepo.findForOrder(order));
		}else {
			order.setItems(itemRepo.findForPendingOrder(order));
		}
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
	@PreAuthorize("hasRole('TEC')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody OrderDTO request){
		logger.info(String.format("User '%s' is updating Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(request.getId() != id)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		//We try to transform the DTO into an Order object so that it can't be used as an order by the rest of the api
		Order order = request.toOrder();
		if(order.getId() != id)
			return new ResponseEntity<Object>("Cette commande n'existe pas !", HttpStatus.BAD_REQUEST);
		if(order.getIsReceived()) {
			//If the order is received then it means that all items are complete and ready to be inserted in the main Item table
			try {
				//When we update it then it means it's received
				order.setIsReceived(true);
				//Save the order
				orderRepo.save(order, id);
				//We give them the default stock room
				order.getItems().forEach(i->i.setRoom(roomRepo.findByName("Stock").orElse(null)));
				//We set the order's items as received and insert them in the main Item table
				itemRepo.createAll(order.getItems(), order);
				itemRepo.delete(order);
				//As we are working with a DTO that isn't complete, we're sending back the completely rebuilt object
				//To avoid problems
				logger.info(String.format("User '%s' has successfully completed Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
				return this.getById(order.getId());
			} catch(Exception e) {
				logger.error(String.format("User '%s' has failed to complete Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
				return new ResponseEntity<Object>("La réception à échoué !", HttpStatus.BAD_REQUEST);
			}
		} else {
			//If the order is not received then it means that the user is trying to update the OrderItem to complete them.
			try {
				//We update the items in the temporary ORDER_ITEM table
				itemRepo.saveAll(order.getItems(), order);
				//As we are working with a DTO that isn't complete, we're sending back the completely rebuilt object
				//To avoid problems
				logger.info(String.format("User '%s' has successfully updated Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
				return this.getById(order.getId());
			} catch(Exception e) {
				logger.error(String.format("User '%s' has failed to update Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
				return new ResponseEntity<Object>("La réception à échoué !", HttpStatus.BAD_REQUEST);
			}
		}

	}
	
	/**
	 * Simple POST method
	 * @param request the id of the order you're looking to save
	 * NB: you cannot update an order's items because the relations is represented by a Map which Spring absolutely can't 
	 * Deserialize for some reason, so must conform to the OrderDTO interface instead
	 * @return the order with it's generated id or an error message
	 */
	@PreAuthorize("hasRole('PGM')")
  	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody OrderCreationDTO request) {
		logger.info(String.format("User '%s' is creating a new Order", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
		try {
			//We try to transform the DTO into an Order object so that it can't be used as an order by the rest of the api
			Order order = request.toOrder();
			//We get the logged user's information for Spring Context
			User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			//We set the order's user
			order.setUser(u);
			//We save the order
			order.setId(orderRepo.create(order));
			//We need to get all the items of the DTO, create them (and generate their reference)
			List<Item> items = new ArrayList<Item>();
			try {
				for(OrderCreationTypeDTO type: request.getTypes()) {
					for(OrderCreationItemDTO item: type.getItems()) {
				  		int monthlyCount = itemRepo.getCountForCurrentMonthForType(type.getId());
						for(int i=0; i<item.getQuantity(); i++) {
							Item tmp = new Item();
							tmp.setType(type.toItemType());
							tmp.setReference(generateItemReference(type, item, monthlyCount));
							tmp.setDescription(item.getDescription());
							tmp.setPurchasedAt(
									new Date(
										item.getPurchasedAt()
										.atStartOfDay()
										.atZone(ZoneId.systemDefault())
										.toInstant().toEpochMilli()
										)
									);
							tmp.setWarrantyExpiresAt(								
									new Date(
										item.getWarrantyExpiresAt()
										.atStartOfDay()
										.atZone(ZoneId.systemDefault())
										.toInstant().toEpochMilli()
									)
							);
							tmp.setPrice(item.getPrice());
							tmp.setProvider(item.getProvider());
							tmp.setCheckupInterval(item.getCheckupInterval());
							tmp.setId(itemRepo.create(tmp, order));
							items.add(tmp);
							monthlyCount++;
						}
					}
				}
			}catch (Exception e) {
				logger.error(String.format("User '%s' has failed to create a new Order with id:'%s'. The Order was created but there was an error during the related Items creation", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), order.getId()));
				return new ResponseEntity<String>("La création du matériel lié à échoué !", HttpStatus.BAD_REQUEST);
			} finally {
				order.setItems(items);
			}
//			//We create an item instance for each item of the order and we attach them to the order
//			try {
//				orderRepo.attachAllItemId(order, itemRepo.createAll(order.getItems()));
//			} catch(Exception e) {
//				logger.error(String.format("User '%s' has failed to create a new Order with id:'%s'. The Order was created but there was an error during the related Items creation", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), order.getId()));
//				return new ResponseEntity<String>("La création du matériel lié à échoué !", HttpStatus.BAD_REQUEST);
//			}
			//We build the map that contains the type and the related quantity as key and value
			Map<Type, Integer> map = new HashMap<Type, Integer>();
			for(Item item: order.getItems()) {
				if(map.containsKey(item.getType())){
					map.merge(item.getType(), 1, Integer::sum);
				} else {
					map.put(item.getType(), 1);
				}
			}
			//We attach all the types and quantity to the order
			orderRepo.attachAll(order, map);
			logger.info(String.format("User '%s' has successfully created a new Order with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), order.getId()));
			return new ResponseEntity<Integer>(order.getId(), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' has failed to create a new Order", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
  	
  	private String generateItemReference(OrderCreationTypeDTO type, OrderCreationItemDTO item, int count) {
  		return String.format("%s%02d%02d%05d",type.getAlias(), item.getPurchasedAt().getDayOfMonth(), item.getPurchasedAt().getMonthValue(), count+1);
  	}
}
