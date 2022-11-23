package campingplatz.catalog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import campingplatz.booking.Booking;
import campingplatz.campsite.*;
import campingplatz.campsite.Campsite.Size;
import campingplatz.extras.Extras;
import campingplatz.extras.ExtrasCatalog;
import campingplatz.extras.ExtrasRepository;
import campingplatz.mail.Mail;
import campingplatz.reservation.Reservation;
import campingplatz.reservation.ReservationCatalog;
import campingplatz.user.UserManagement;
import ch.qos.logback.core.net.SyslogOutputStream;
import ch.qos.logback.core.util.Duration;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;

@Controller
class CatalogController<ExtrasInventory> {
	@Autowired
	ExtrasCatalog extrasCatalog;
	@Autowired
	ExtrasRepository extrasRepository;
	@Autowired
	ReservationCatalog reservationCatalog;
	@Autowired
	UniqueInventory<UniqueInventoryItem> extrasInventory;
	@Autowired
	UserManagement userManagement;
	private static final Quantity NONE = Quantity.of(0);

	private CampsiteCatalog campsiteCatalog;

	private final UniqueInventory<UniqueInventoryItem> inventory;

	// private BusinessTime businessTime;

	CatalogController(CampsiteCatalog campsiteCatalog, UniqueInventory<UniqueInventoryItem> inventory) { // , BusinessTime
																																																				// businessTime

		this.campsiteCatalog = campsiteCatalog;

		this.inventory = inventory;
		// this.businessTime = businessTime;
	}

	@GetMapping("/getByDate")
	String findByDate(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("start") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("end") LocalDate end,
			@RequestParam(value = "size", defaultValue = "ALL") String size,
			@RequestParam(value = "parking", defaultValue = "ALL") String parkingSpace,
			@RequestParam(value = "permacamper", defaultValue = "false") boolean permacamper) {

		if ((start != null && end != null) && start.isAfter(end)) {
			LocalDate temp = start;
			start = end;
			end = temp;
		}
		List<LocalDate> dates = new ArrayList<LocalDate>();
		List<Campsite> campsites = new ArrayList<Campsite>();
		List<Campsite> sameSize = new ArrayList<Campsite>();
		List<Campsite> sameParking = new ArrayList<Campsite>();
		if (start != null && end != null) {
			dates = start.datesUntil(end).collect(Collectors.toList());
		}

		if (size.equals("ALL")) {

			for (Campsite campsite : campsiteCatalog.findByVisibility(Campsite.Visibility.VISIBLE)) {

				campsites.add(campsite);
			}
		} else {
			for (Campsite campsite : campsiteCatalog.findBySize(Size.of(size))) {
				if (campsite.getVisibility().toString().equals("VISIBLE"))
					campsites.add(campsite);
			}
		}

		if (permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("NOT_PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		} else if (!permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		}
		if (!parkingSpace.equals("ALL")) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (!campsites.get(i).getParkingSpace().toString().equals(parkingSpace)) {
					campsites.remove(i);
				}
			}
		}

		if (start != null && end != null) {
			for (LocalDate date : dates) {
				for (int i = campsites.size() - 1; i >= 0; i--) {
					if (campsites.get(i).hasDate(date))
						campsites.remove(i);
				}
			}
		} else if (start != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(start))
					campsites.remove(i);
			}
		} else if (end != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(end))
					campsites.remove(i);
			}
		}
		model.addAttribute("message", "");
		if (campsites.isEmpty()) {
			model.addAttribute("message", "Leider konnten wir keine entsprechenden freien Platz finden");
			sameSize = sameSize(start, end, size, permacamper);
			sameParking = sameParking(start, end, parkingSpace, permacamper);
		}
		model.addAttribute("sameSize", sameSize);
		model.addAttribute("sameParking", sameParking);
		model.addAttribute("title", "campsiteCatalog.all.title");
		model.addAttribute("campsiteCatalog", campsites);
		return "campsiteCatalog";
	}

	private List<Campsite> sameSize(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, String size, boolean permacamper) {

		List<LocalDate> dates = new ArrayList<LocalDate>();
		List<Campsite> campsites = new ArrayList<Campsite>();
		if (start != null && end != null) {
			dates = start.datesUntil(end).collect(Collectors.toList());
		}

		if (size.equals("ALL")) {

			for (Campsite campsite : campsiteCatalog.findByVisibility(Campsite.Visibility.VISIBLE)) {

				campsites.add(campsite);
			}
		} else {
			for (Campsite campsite : campsiteCatalog.findBySize(Size.of(size))) {
				if (campsite.getVisibility().toString().equals("VISIBLE"))
					campsites.add(campsite);
			}
		}

		if (permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("NOT_PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		} else if (!permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		}

		if (start != null && end != null) {
			for (LocalDate date : dates) {
				for (int i = campsites.size() - 1; i >= 0; i--) {
					if (campsites.get(i).hasDate(date))
						campsites.remove(i);
				}
			}
		} else if (start != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(start))
					campsites.remove(i);
			}
		} else if (end != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(end))
					campsites.remove(i);
			}
		}

		return campsites;
	}

	private List<Campsite> sameParking(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, String parkingSpace, boolean permacamper) {

		List<LocalDate> dates = new ArrayList<LocalDate>();
		List<Campsite> campsites = new ArrayList<Campsite>();
		if (start != null && end != null) {
			dates = start.datesUntil(end).collect(Collectors.toList());
		}

		for (Campsite campsite : campsiteCatalog.findByVisibility(Campsite.Visibility.VISIBLE)) {

			campsites.add(campsite);
		}

		if (permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("NOT_PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		} else if (!permacamper) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).getPermaCamper().toString().equals("PERMACAMPER")) {
					campsites.remove(i);
				}
			}
		}
		if (!parkingSpace.equals("ALL")) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (!campsites.get(i).getParkingSpace().toString().equals(parkingSpace)) {
					campsites.remove(i);
				}
			}
		}

		if (start != null && end != null) {
			for (LocalDate date : dates) {
				for (int i = campsites.size() - 1; i >= 0; i--) {
					if (campsites.get(i).hasDate(date))
						campsites.remove(i);
				}
			}
		} else if (start != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(start))
					campsites.remove(i);
			}
		} else if (end != null) {
			for (int i = campsites.size() - 1; i >= 0; i--) {
				if (campsites.get(i).hasDate(end))
					campsites.remove(i);
			}
		}

		return campsites;
	}

	@GetMapping("/campsiteCatalog")
	String allSites(Model model) {
		model.addAttribute("message", "");
		model.addAttribute("campsiteCatalog", campsiteCatalog.findByVisibility(Campsite.Visibility.VISIBLE));
		model.addAttribute("title", "campsiteCatalog.all.title");

		return "campsiteCatalog";
	}

	@GetMapping("/hiddenCampsites")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String hiddenSites(Model model) {

		model.addAttribute("campsiteCatalog", campsiteCatalog.findByVisibility(Campsite.Visibility.NOT_VISIBLE));
		model.addAttribute("title", "campsiteCatalog.hidden.title");

		return "hiddenCampsites";
	}

	@GetMapping("/impressum")
	String impressum(Model model) {

		return "impressum";
	}

	@GetMapping("/datenschutz")
	String datenschutz(Model model) {

		return "datenschutz";
	}

	// (｡◕‿◕｡)
	// Befindet sich die angesurfte Url in der Form /foo/5 statt /foo?bar=5 so muss
	// man @PathVariable benutzen
	// Lektüre: http://spring.io/blog/2009/03/08/rest-in-spring-3-mvc/
	@GetMapping("/campsite/{campsite}")
	String detail(@PathVariable Campsite campsite, Model model) {
		model.addAttribute("campsite", campsite);
		model.addAttribute("dates", campsite.getMyBookings());
		model.addAttribute("title", campsite.getName());
		return "detail";
	}

	@GetMapping("/campsiteName/{name}")
	String detailByName(@PathVariable String name, Model model) {
		Campsite campsite = null;
		for (Campsite temp : campsiteCatalog.findByName(name)) {
			campsite = temp;
		}
		
		
		var quantity = inventory.findByProductIdentifier(campsite.getId()) //
				.map(InventoryItem::getQuantity) //
				.orElse(NONE);

		model.addAttribute("campsite", campsite);
		model.addAttribute("dates", campsite.getMyBookings());
		model.addAttribute("quantity", quantity);
		model.addAttribute("orderable", quantity.isGreaterThan(NONE));
		model.addAttribute("title", campsite.getName());
		return "detail";
	}

	@GetMapping("/changeCampsite/{campsite}")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String changeCampsite(@PathVariable Campsite campsite, Model model) {

		var quantity = inventory.findByProductIdentifier(campsite.getId()) //
				.map(InventoryItem::getQuantity) //
				.orElse(NONE);

		model.addAttribute("campsite", campsite);
		model.addAttribute("quantity", quantity);
		model.addAttribute("orderable", quantity.isGreaterThan(NONE));

		return "changeCampsite";
	}

	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	@PostMapping("/campsite/change/")
	String campsiteChange(@RequestParam("id") Campsite campsite, @RequestParam("name") String name,
			@RequestParam("size") Campsite.Size size, @RequestParam("parking") Campsite.ParkingSpace parking,
			@RequestParam("info") String info, @RequestParam("price") String price,
			@RequestParam("visibility") Campsite.Visibility visibility,
			@RequestParam("permacamper") Campsite.PermaCamper permacamper) {
		price = price.replaceAll("[^0-9.]", "");
		BigDecimal validAmount = new BigDecimal(price);
		Money m2 = Money.of(validAmount, "EUR");
		campsite.setName(name);
		campsite.setPrice(m2);
		campsite.setSize(size);
		campsite.setParkingSpace(parking);
		campsite.setDescription(info);
		campsite.setVisibility(visibility);
		campsite.setPermaCamper(permacamper);
		campsiteCatalog.save(campsite);
		return "redirect:/campsiteCatalog";
	}

	@PostMapping("/campsite/remove")
	public String removeCampsite(@RequestParam("campsite") Campsite campsite) {

		inventory.delete(inventory.findByProduct(campsite).get());
		campsiteCatalog.delete(campsite);
		return "redirect:/campsiteCatalog";
	}

	@PostMapping("/extra/remove")
	public String removeExtra(@RequestParam("extra") Extras extra) {

		extrasInventory.delete(extrasInventory.findByProduct(extra).get());
		extrasRepository.deleteById(extra.getId());
		return "redirect:/extras/";
	}

	// @GetMapping("/addCampsite/")
	// public String addCampsite() {
	// return "addCampsite";
	// }

	@GetMapping("/addCampsite")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String addCampsite() {
		return "addCampsite";
	}

	@GetMapping("/addExtra")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String addExtra() {
		return "addExtra";
	}

	@GetMapping("/defect/{campsite}")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String defect(@PathVariable Campsite campsite, Model model) {
		model.addAttribute("campsite", campsite);
		return "campsiteDefect";
	}

	@GetMapping("/defectCampsites")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String defectCampsites(Model model) {
		ArrayList<Defect> pastDefects = new ArrayList<Defect>();
		ArrayList<Defect> defects = new ArrayList<Defect>();

		for (Campsite campsite2 : campsiteCatalog.findAll()) {
			if(!campsite2.getDefects().isEmpty())
				for (Defect defect : campsite2.getDefects()) {
					if(defect.getEnd().isAfter(LocalDate.now())){
						defects.add(defect);}
					else{
						pastDefects.add(defect);
					}
				}
		}
		
		model.addAttribute("defects", defects);
		model.addAttribute("pastDefects", pastDefects);
		return "defectCampsites";
	}
	

	@PostMapping("/defect/{campsite}")
	public String defect(@PathVariable Campsite campsite, @RequestParam("defect") String defect, @RequestParam("costs") Double costs,
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@RequestParam("start") LocalDate start, 	@DateTimeFormat(pattern = "yyyy-MM-dd")
 	@RequestParam("end") LocalDate end) {
		if(start.isEqual(end))end = end.plusDays(1);
		Defect tempDefect = new Defect(campsite, defect, costs, start, end.plusDays(1));
		campsite.getDefects().add(tempDefect);
		campsiteCatalog.save(campsite);

		ArrayList<UserAccount> userAccounts = new ArrayList<UserAccount>();
		for (LocalDate date : start.datesUntil(end).collect(Collectors.toList()))
		{	
			if(campsite.getMyBookings().contains(date))
			{	
				for (Reservation reservation : reservationCatalog.findAll()){
					if(reservation.getCampSite().equals(campsite.getName())){
						List<LocalDate> campsiteDates = reservation.getDatesBetween();
						for(LocalDate booked : campsiteDates){
							if(booked.isAfter(LocalDate.now())){
								if(booked.isEqual(date)){
									UserAccount userAccount = userManagement.findByName(reservation.getAccount());
									if(!userAccounts.contains(userAccount)){
										userAccounts.add(userAccount);
										reservation.setReservationOK(false, start, end);
										try {
											Email mail = EmailBuilder.startingBlank().from("CampingplatzGruppe2@gmail.com").to(userAccount.getUsername())
													.withSubject("Ihre Reservierung " +reservation.getId())
													.withPlainText("Hallo,\nleider gibt es ein Problem mit ihrer Reservierung des Platzes " + reservation.getCampSite() + ". Auf diesem werden vom " + start.getDayOfMonth() + start.getMonth() + " bis zum " + end.getDayOfMonth() + end.getMonth() + " reperaturen vorgenommen. Bitte reservieren Sie für diesen Zeitraum einen anderen Platz oder rufen uns an, damit wir ihnen helfen können.")
													.buildEmail();
											Mail.sendEMail(mail);
										} catch (Exception e) {
											System.out.println("CatalogController.defect(): Fehler beim Versand der Nachricht" + e);
										}
									}
								}
							}
						}
					}
				}
			}
			if(!campsite.getMyBookings().contains(date)){
				campsite.getMyBookings().add(date);
			} 
			if(!campsite.defectDatesContainsDate(date)){
				campsite.getDefectDates().add(date);
				
			} 
			campsiteCatalog.save(campsite);
		}

	
	
		return "redirect:/campsiteCatalog";
	}

	// @PostMapping("/defectDone")
	// public String defectDone(@RequestParam("campsite") Campsite campsite, @RequestParam("defect") Defect defect, @RequestParam("price")Double price) {
		
	// 	for (LocalDate date : start.datesUntil(end).collect(Collectors.toList()))
	// 	{	
	// 		if(!campsite.defectDatesContainsDate(date)){
	// 			campsite.getMyBookings().remove(date);
	// 		} 
	// 	}
	// 	campsiteCatalog.save(campsite);
	// 	return "redirect:/campsiteCatalog";
	// }



	//kann dann doch weg oder?
	@PostMapping("/createCampsite")
	public String createCampsite(@RequestParam("name") String name, @RequestParam("price") Double price,
			@RequestParam("size") Campsite.Size size, @RequestParam("parking") Campsite.ParkingSpace parkingSpace,
			@RequestParam("info") String info, @RequestParam("visibility") Campsite.Visibility visibility,
			@RequestParam("permacamper") Campsite.PermaCamper permacamper) {
		campsiteCatalog.save(
				new Campsite(name, "platzhalter", Money.of(price, EURO), size, parkingSpace, info, visibility, permacamper));
		// setQuantity(temp, quantity); Es gibt keine InventoryItem im inventory. Wie
		// wird dies erstellt?

		return "redirect:/campsiteCatalog";
	}

	@PostMapping("/addCampsite")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String addCampsite(@RequestParam("name") String name, @RequestParam("size") Campsite.Size size,
			@RequestParam("parking") Campsite.ParkingSpace parkingSpace, @RequestParam("info") String info,
			@RequestParam("price") Double price,
			@RequestParam("visibility") Campsite.Visibility visibility,
			@RequestParam("permacamper") Campsite.PermaCamper permacamper) {
		
		if(price < 0)return "redirect:/campsiteCatalog";
		Campsite temp = new Campsite(name, "platzhalter", Money.of(price, EURO), size, parkingSpace, info, visibility, permacamper);

		campsiteCatalog.save(temp);
		inventory.save(new UniqueInventoryItem(temp, Quantity.of(1)));

		return "redirect:/campsiteCatalog";

	}

	// Zum testen von Posts und Gets - kann in TESTER.html angepasst werden
	@PostMapping("/TESTER")
	public void TESTER(@RequestParam("test") String test) {
		System.out.println(test);
	}

	@GetMapping("/TESTER")
	public String TESTER() {
		return "TESTER";
	}
}