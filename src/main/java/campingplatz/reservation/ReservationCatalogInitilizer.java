package campingplatz.reservation;


import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
@Order(20)
class ReservationCatalogInitilizer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(ReservationCatalogInitilizer.class);

	private final ReservationCatalog reservationCatalog;

	ReservationCatalogInitilizer(ReservationCatalog reservationCatalog) {

		Assert.notNull(reservationCatalog, "VideoCatalog must not be null!");

		this.reservationCatalog = reservationCatalog;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.core.DataInitializer#initialize()
	 */
	@Override
	public void initialize() {

		if (reservationCatalog.findAll().iterator().hasNext()) {
			return;
		}

		LOG.info("Creating default catalog entries.");

	
	}
}
