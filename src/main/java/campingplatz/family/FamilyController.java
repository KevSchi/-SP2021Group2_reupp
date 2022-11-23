// UserManagement
package campingplatz.family;


import campingplatz.reservation.Reservation;
import campingplatz.reservation.ReservationCatalog;
import campingplatz.user.UserManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
class FamilyController {
    @Autowired private FamilyService familyService;
    @Autowired private UserManagement userManagement;
    @Autowired private ReservationCatalog reservationCatalog;

    //String n = userManagement.findByName();

    @GetMapping("/addFamily/{reservation}")
	public String addFamily(@PathVariable Reservation reservation, Model model) {
        model.addAttribute("order", reservationCatalog.findById(reservation.getId()).get()); //--> in booking die Methoden anpassen 
        model.addAttribute("user", userManagement.findByUserAccount(userManagement.findByName((reservationCatalog.findById(reservation.getId()).get().getAccount()))));
		return "addFamily";
	}


    

    // @PostMapping("/addFamily/{orderId}")
	// String addFamily(@PathVariable Booking orderId, @RequestParam("otherPeople") String name, @RequestParam(value="isInGroup", defaultValue = "false") boolean group){
    //     //boolean b1=Boolean.parseBoolean(group);  
       
    //     Family tmp = new Family(name, group);
    //     familyService.addFamily(tmp);
    //     orderId.addFamily(tmp); // --> könnte falsch sein !!
    //     orderManagement.save(orderId);
    //     System.out.println(tmp.isIsInGroup());
	// 	return"redirect:/orders";                
    // }

   
          
    @GetMapping("/families")
    @PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
    String showFamilies(Model model){

        
        model.addAttribute("familyService", familyService); // hier fehlt noch die genaue Auflistung aller Familien 
        

        return "allFamilies";
    } 

    @GetMapping("/groups")
    String showAllGroups(Model model){


        return"redirect:/all";
    }

    
}