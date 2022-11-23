package campingplatz.extras;

import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 * @see DataInitializer
 */
@Component
@Order(20)
class ExtrasDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(ExtrasDataInitializer.class);
	private final ExtrasCatalog extrasCatalog;

	ExtrasDataInitializer(ExtrasCatalog extrasCatalog) {

		Assert.notNull(extrasCatalog, "ExtrasCatalog must not be null!");

		this.extrasCatalog = extrasCatalog;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.core.DataInitializer#initialize()
	 */
	@Override
	public void initialize() {
		if (extrasCatalog.findAll().iterator().hasNext()) {
			return;
		}

		LOG.info("Creating default Extrascatalog entries.");
		extrasCatalog.save(new Extras("Ball", "ball.png", Money.of(6.99, EURO),Quantity.of(10), true, false, true));
		extrasCatalog.save(new Extras("Federballsets", "Federballsets.png", Money.of(14.99, EURO),Quantity.of(10),true, false, true));
		extrasCatalog.save(new Extras("Tischtennisgarnituren", "Tischtennisgarnituren.png", Money.of(14.99, EURO),Quantity.of(10),true, false, true));
		extrasCatalog.save(new Extras("Volleyballnetze", "Volleyballnetze.png", Money.of(14.99, EURO),Quantity.of(10),true, false, true));
		extrasCatalog.save(new Extras("Fahrrad", "bike.png", Money.of(29.99, EURO),Quantity.of(10),true, false, true));
		extrasCatalog.save(new Extras("Verschmutzungsgebühr", "trashtalk.png", Money.of(39.99, EURO),Quantity.of(20),false, false, false));
		extrasCatalog.save(new Extras("Elektrizität Pauschal", "power.png", Money.of(2.00, EURO),Quantity.of(64000),false, false, true));
		extrasCatalog.save(new Extras("Elektrizität kW/h", "power.png", Money.of(0.32, EURO),Quantity.of(64000),false, true, false));
		extrasCatalog.save(new Extras("Wasser Pauschal", "wasserhahn.png", Money.of(2.00, EURO),Quantity.of(64000),false, false, true));
		extrasCatalog.save(new Extras("Wasser m³", "wasserhahn.png", Money.of(0.40, EURO),Quantity.of(64000),false, true, false));
		
	
	}
}