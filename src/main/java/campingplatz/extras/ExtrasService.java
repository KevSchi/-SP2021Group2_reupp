package campingplatz.extras;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;


@Service
public class ExtrasService {
    @Autowired 	private ExtrasCatalog extrasCatalog;
    public Streamable<Extras> getExtras(){
		return extrasCatalog.findAll();
	}
    

}