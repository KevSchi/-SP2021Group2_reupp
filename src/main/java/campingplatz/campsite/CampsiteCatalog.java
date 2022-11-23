package campingplatz.campsite;

import org.salespointframework.catalog.Catalog;
import org.springframework.data.domain.Sort;

import campingplatz.campsite.Campsite.ParkingSpace;
import campingplatz.campsite.Campsite.Size;
import campingplatz.campsite.Campsite.Visibility;

public interface CampsiteCatalog extends Catalog<Campsite> {
	static final Sort DEFAULT_SORT = Sort.by("productIdentifier").descending();


	Iterable<Campsite> findBySize(Size size, Sort sort);
	default Iterable<Campsite> findBySize(Size size) {
		return findBySize(size, DEFAULT_SORT);
	}

	Iterable<Campsite> findByParkingSpace(ParkingSpace parkingSpace, Sort sort);
	default Iterable<Campsite> findByParkingSpace(ParkingSpace parkingSpace) {
		return findByParkingSpace(parkingSpace, DEFAULT_SORT);
	}

	Iterable<Campsite> findByVisibility(Visibility visibility, Sort sort);
	default Iterable<Campsite> findByVisibility(Visibility visibility) {
		return findByVisibility(visibility, DEFAULT_SORT);
	}

}