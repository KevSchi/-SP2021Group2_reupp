package campingplatz.inventory;

import java.util.Optional;

import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


// Straight forward?

@Controller
class InventoryController {

	private final UniqueInventory<UniqueInventoryItem> inventory;
	private final UniqueInventory<UniqueInventoryItem> extrasInventory;
	InventoryController(UniqueInventory<UniqueInventoryItem> inventory, UniqueInventory<UniqueInventoryItem> extrasInventory) {
		this.inventory = inventory;
		this.extrasInventory = extrasInventory;
	}
	
	/**
	 * Displays all {@link InventoryItem}s in the system
	 *
	 * @param model will never be {@literal null}.
	 * @return the view name.
	 */

	@GetMapping("/stock")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String stock(Model model) {

		model.addAttribute("stock", inventory.findAll());

		return "stock";
	}


	@GetMapping("/extrasStock")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String extrasStock(Model model) {
		
		model.addAttribute("stock", extrasInventory.findAll());

		return "stock";
	}

	@GetMapping("/getID")
	Optional<UniqueInventoryItem> getID(@RequestParam("id") ProductIdentifier id) {
		return extrasInventory.findByProductIdentifier(id);
	}


}