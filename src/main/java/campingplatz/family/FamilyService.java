package campingplatz.family;

import java.util.ArrayList;
// import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import campingplatz.booking.Booking;

@Service
public class FamilyService {
    @Autowired private OrderManagement<Order> orderManagement;

    private List<Family> families = new ArrayList<Family>();

    public void addFamily(Family family) {
        
        families.add(family);
    }

    public Family findByUserAccount(UUID familyId) {
        for (Family family : families) {
            if(family.getId() == familyId){
                return family;
            }
        }
        return null;
	}

    public void addFamily(Booking orderId, ArrayList<String> name, boolean group){
       
        Family tmp = new Family(name, group);
        //falls es nirgendwo mehr genutzt wird auch einfach das von oben hierein?
        addFamily(tmp);
        orderId.addFamily(tmp);
        orderManagement.save(orderId);
    }

}