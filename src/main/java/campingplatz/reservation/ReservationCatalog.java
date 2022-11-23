package campingplatz.reservation;


import org.salespointframework.catalog.Catalog;
import org.springframework.data.domain.Sort;

/**
 * An extension of {@link Catalog} to add video shop specific query methods.
 *
 * @author Oliver Gierke
 */
public interface ReservationCatalog extends Catalog<Reservation> {

	static final Sort DEFAULT_SORT = Sort.by("productIdentifier").descending();

	

}