package campingplatz.extras;

import org.salespointframework.catalog.ProductIdentifier;

import org.springframework.data.repository.CrudRepository;
public interface ExtrasRepository extends CrudRepository<Extras,ProductIdentifier>{  
	
    Extras findExtraByName(String name);
}