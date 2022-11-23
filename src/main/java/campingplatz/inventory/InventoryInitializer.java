package campingplatz.inventory;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import campingplatz.campsite.CampsiteCatalog;
import campingplatz.extras.ExtrasCatalog;

/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 * @see DataInitializer
 */
@Component
@Order(20)
class InventoryInitializer implements DataInitializer {

	private final UniqueInventory<UniqueInventoryItem> inventory;
	private final UniqueInventory<UniqueInventoryItem> extrasInventory;
	private final CampsiteCatalog siteCatalog;
	private final ExtrasCatalog extrasCatalog;

	InventoryInitializer(UniqueInventory<UniqueInventoryItem> inventory, UniqueInventory<UniqueInventoryItem> extrasInventory, CampsiteCatalog siteCatalog, ExtrasCatalog extrasCatalog) {

		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(siteCatalog, "VideoCatalog must not be null!");
		this.extrasInventory = extrasInventory;
		this.inventory = inventory;
		this.siteCatalog = siteCatalog;
		this.extrasCatalog = extrasCatalog;
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.core.DataInitializer#initialize()
	 */
	@Override
	public void initialize() {

		// (｡◕‿◕｡)
		// Über alle Discs iterieren und jeweils ein InventoryItem mit der Quantity 10 setzen
		// Das heißt: Von jeder Disc sind 10 Stück im Inventar.

		siteCatalog.findAll().forEach(site -> {

			// Try to find an InventoryItem for the project and create a default one with 10 items if none available
			if (inventory.findByProduct(site).isEmpty()) {
				inventory.save(new UniqueInventoryItem(site, Quantity.of(1)));
			}
		});

		extrasCatalog.findAll().forEach(extra -> {

			// Try to find an InventoryItem for the project and create a default one with 10 items if none available
			if (extrasInventory.findByProduct(extra).isEmpty()) {
				extrasInventory.save(new UniqueInventoryItem(extra, extra.getQuantity()));
			}
		});
	}
}