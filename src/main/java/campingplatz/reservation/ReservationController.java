package campingplatz.reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ReservationController {
	@Autowired
	UserAccountManagement userAccounts;

	private final ReservationCatalog reservationCatalog;


	ReservationController(ReservationCatalog reservationCatalog) {

		this.reservationCatalog = reservationCatalog;
	
	}

	@GetMapping("/reservation")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String ordersort(Model model) {
		List<Reservation> reservations = new ArrayList<Reservation>();
		List<Reservation> futureReservations = new ArrayList<Reservation>();
		
		for (Reservation reservation : reservationCatalog.findAll()) {
			if(reservation.getStartDate().isAfter(LocalDate.now())){
				futureReservations.add(reservation);
			}else{
				reservations.add(reservation);
			}
			
		}

		model.addAttribute("reservationCatalog", reservations);
		model.addAttribute("futureReservations", futureReservations);

		model.addAttribute("header1", "Aktuell");
		model.addAttribute("header2", "Zukünftig");

		return "reservations";
	}

	@GetMapping("/getReservations")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String findOrders(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("start") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("end") LocalDate end,
			@RequestParam("name") String name) {
		
		if ((start != null && end != null) && start.isAfter(end)) {
			LocalDate temp = start;
			start = end;
			end = temp;
		}
		List<Reservation> reservations = new ArrayList<Reservation>();
		List<Reservation> futureReservations = new ArrayList<Reservation>();


		
		for (Reservation reservation : reservationCatalog.findAll()) {
			reservations.add(reservation);
		}
		

		if (!(name == "")) {
			for(int i = reservations.size()-1; i>=0;i--){
				if(!reservations.get(i).getAccount().equals(name)){
					reservations.remove(reservations.get(i));
				}
			}
		}

		if(start != null)
		for(int i = reservations.size()-1; i>=0;i--){
			if(!reservations.get(i).getStartDate().equals(start)){
				reservations.remove(reservations.get(i));
			}
		}
		

		if(end != null)
		for(int i = reservations.size()-1; i>=0;i--){
			if(!reservations.get(i).getEndDate().equals(end)){
				reservations.remove(reservations.get(i));
			}
		}
		
		model.addAttribute("header1", "Zur Suche passende Reservierungen");
		model.addAttribute("reservationCatalog", reservations);
		model.addAttribute("futureReservations" , futureReservations);
		return "reservations";
	}
	
}