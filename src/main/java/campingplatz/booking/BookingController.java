package campingplatz.booking;

import campingplatz.booking.Booking.Payment;
import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;
import campingplatz.campsite.Campsite.PermaStatus;
import campingplatz.campsite.Campsite.Visibility;
import campingplatz.extras.Extras;
import campingplatz.extras.ExtrasCatalog;
import campingplatz.family.FamilyService;
import campingplatz.invoice.InvoiceGenerator;
import campingplatz.reservation.*;
import campingplatz.user.Date;
import campingplatz.user.RegistrationForm;
import campingplatz.user.User;
import campingplatz.user.UserManagement;
import campingplatz.user.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.money.MonetaryAmount;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.itextpdf.text.DocumentException;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
public class BookingController {
	@Autowired
	private UserRepository users;
	@Autowired
	BookingValidationService validationService;
	@Autowired
	UserManagement userManagement;
	@Autowired
	ReservationCatalog reservationCatalog;
	@Autowired
	ExtrasCatalog extrasCatalog;
	@Autowired
	UserAccountManagement userAccounts;
	@Autowired
	CampsiteCatalog catalog;
	@Autowired
	private final OrderManagement<Booking> orderManagement;
	@Autowired
	UniqueInventory<UniqueInventoryItem> inventory;
	@Autowired
	FamilyService familyService;

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
	private static final Quantity NONE = Quantity.of(0);

	BookingController(OrderManagement<Booking> orderManagement) {

		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderManagement = orderManagement;

	}

	@ModelAttribute("cart")
	Cart initializeCart() {
		return new Cart();
	}

	@PreAuthorize("isAuthenticated()")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@PostMapping("/cart")
	String addCampsite(@ModelAttribute("pid") Campsite campsite, BindingResult result,
			@LoggedIn Optional<UserAccount> userAccount, @ModelAttribute Cart cart,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("start") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("end") LocalDate end, Model model) {
				if(campsite.isPermaCamper()){
					end = LocalDate.of(end.getYear(), end.getMonthValue(), end.getDayOfMonth()).plusDays(1);
				}
		String err = validationService.validateDate(cart, campsite, start, end);
		if (!err.isEmpty()) {
			ObjectError error = new ObjectError("globalError", err);
			result.addError(error);
		}
		if (result.hasErrors()) {

			var quantity = inventory.findByProductIdentifier(campsite.getId()) //
					.map(InventoryItem::getQuantity) //
					.orElse(NONE);
			model.addAttribute("error", err);
			model.addAttribute("campsite", campsite);
			model.addAttribute("quantity", quantity);
			model.addAttribute("orderable", quantity.isGreaterThan(NONE));
			model.addAttribute("title", campsite.getName());
			return "detail";
		}
		if(campsite.isPermaCamper()){
			Calendar calendar = Calendar.getInstance();
			calendar.set(start.getYear(), start.getMonthValue(), start.getDayOfMonth());
			if(start.isBefore(LocalDate.of(start.getYear(), 4, 1)))start = LocalDate.of(start.getYear(), 04, 01);
			else if(start.isAfter(LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))){
				start = LocalDate.of(start.getYear(), 04, 01);
			}
			else{start = LocalDate.now();}
			end = LocalDate.of(end.getYear(), 10, 31);
			}

		campsite.setDatesBetween(start, end);
		
		CartItem temp = cart.addOrUpdateItem(campsite, Quantity.of(ChronoUnit.DAYS.between(start, end)));
		User tempUser = userManagement.findByUserAccount(userAccount.get());
		tempUser.getDates().add(new Date(temp.getId(), start, end, Money.of(campsite.getPrice().getNumber(), EURO)));
		userManagement.save(tempUser);
		return "redirect:/";
	}

	@GetMapping("/cart")
	String basket(@LoggedIn UserAccount userAccount, Model model, Locale locale) {
		model.addAttribute("user", userManagement.findByUserAccount(userAccount));
		return "cart";
	}

	@PostMapping("/checkin/{reservation}")
	String buy(@ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount,
			@PathVariable Reservation reservation, @RequestParam("otherPeople") ArrayList<String> name,
			@RequestParam(value = "isInGroup", defaultValue = "false") boolean group) {

		return userAccount.map(account -> {

			var order = new Booking(userAccounts.findByUsername(reservation.getAccount()).get(),
					reservation.getCampSite(), reservation.isPermaCamper(),Cash.CASH, reservation.getPrice(), reservation.getStartDate(),
					reservation.getEndDate());
			cart.addItemsTo(order);
			orderManagement.save(order);
			familyService.addFamily(order, name, group);
			

			cart.clear();
			reservationCatalog.delete(reservation);
			return "redirect:/orders";
		}).orElse("redirect:/cart");

	}

	@PostMapping("/checkout")
	String checkin(@RequestParam("order") Booking order, @LoggedIn Optional<UserAccount> userAccount,
			@RequestParam(value = "paid", required = false) String paid, @RequestParam(value = "payment", required = false ) String payment) {
		Payment paymentMethod = null;
		boolean orderPaid = false;
		if(paid == null){return "redirect:/orders";}
		switch (paid) {
			case "Bezahlt":
				orderPaid = true;
				break;
		
			case "Nicht_Bezahlt":
				orderPaid = false;
				break;
		}

		switch (payment) {
			case "Bar":
			paymentMethod = Payment.Bar;
				break;
		
			case "EC-Karte":
			paymentMethod = Payment.EC_Karte;
				break;

			case "Kreditkarte":
			paymentMethod = Payment.Kreditkarte;
				break;

			case "Überweisung":
			paymentMethod = Payment.Überweisung;
				break;
		}
		for (RentedObject object : order.getExtras()) {
			object.setDateCheckOut(LocalDate.now());
		}
		order.setTimeStamp(LocalDate.now());
		orderManagement.save(order);
		order.setPayment(paymentMethod);
		if (!orderPaid) {
			return userAccount.map(account -> {
				orderManagement.payOrder(order);
				orderManagement.save(order);

				return "redirect:/orders";
			}).orElse("redirect:/orders");

		} else {
			return userAccount.map(account -> {
				orderManagement.payOrder(order);
				orderManagement.save(order);
				orderManagement.completeOrder(order);
				orderManagement.save(order);

				return "redirect:/orders";
			}).orElse("redirect:/orders");
		}
	}

	@PostMapping("/paid")
	String paid(@RequestParam("order") Booking order, @LoggedIn Optional<UserAccount> userAccount) {
		return userAccount.map(account -> {
			orderManagement.completeOrder(order);
			orderManagement.save(order);

			return "redirect:/orders";
		}).orElse("redirect:/orders");
	}

	Campsite temp;

	@PostMapping("/cancel")
	String cancel(@ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount,
			@RequestParam("pid") Reservation reservation, @RequestParam("reason") String reason) {

		return userAccount.map(account -> {

			temp = null;
			var order = new Booking(userAccounts.findByUsername(reservation.getAccount()).get(),
					reservation.getCampSite(), reservation.isPermaCamper() ,Cash.CASH, reservation.getPrice(), reservation.getStartDate(),
					reservation.getEndDate()); // hier
			cart.addItemsTo(order);
			orderManagement.save(order);
			cart.clear();
			reservationCatalog.delete(reservation);

			for (Campsite campsite : catalog.findByName(reservation.getCampSite())) {
				temp = campsite;
			}

			for (LocalDate date : temp.getDatesBetween(order.getStartDate(), order.getEndDate())) {
				temp.deleteDate(date);
			}
			ArrayList<String> tempList = new ArrayList<String>();
			tempList.add(reservation.getAccount());
			familyService.addFamily(order, tempList, false);
			order.setReason(reason);
			orderManagement.cancelOrder(order, reason);
			orderManagement.save(order);

			return "redirect:/reservation";
		}).orElse("redirect:/reservation");
	}

	@PostMapping("/remove")
	String remove(@RequestParam("identifier") String identifier, @ModelAttribute Cart cart,
			@LoggedIn Optional<UserAccount> userAccount) {
		temp = null;
		CartItem cartItem = cart.getItem(identifier).get();
		LocalDate start = userManagement.findByUserAccount(userAccount.get()).getStart(cartItem);
		LocalDate end = userManagement.findByUserAccount(userAccount.get()).getEnd(cartItem);

		for (Campsite campsite : catalog.findByName(cartItem.getProduct().getName())) {
			temp = campsite;
		}
		for (LocalDate date : temp.getDatesBetween(start, end)) {
			temp.deleteDate(date);
		}
		
		catalog.save(temp);
		cart.removeItem(identifier);

		return ("redirect:/cart");
	}

	private boolean isRequiredBooked(Booking booking){
		if(!booking.isPermaCamper)return true;
		ArrayList<String> objects = new ArrayList<String>();
		for (Extras extra : extrasCatalog.findAll()) {
			if(extra.isRequired())objects.add(extra.getName() + ";" + extra.getId());
		}
		
		for (RentedObject rentedObject : booking.getExtras()) {
			
			String temp = rentedObject.getName() + ";" + rentedObject.getExtraID();
			if(objects.contains(temp))objects.remove(temp);
		}
		if(objects.isEmpty()) return true;
		booking.setMissing(objects);
		return false;
	}



	@GetMapping("/orders")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String orders(Model model) {
		for (Booking booking : orderManagement.findBy(OrderStatus.OPEN)) {
			booking.setRequiredBooked(isRequiredBooked(booking));
		}
		
		model.addAttribute("ordersPaid", orderManagement.findBy(OrderStatus.PAID));
		model.addAttribute("ordersRunning", orderManagement.findBy(OrderStatus.OPEN));
		model.addAttribute("ordersCancelled", orderManagement.findBy(OrderStatus.CANCELLED));
		model.addAttribute("ordersCompleted", orderManagement.findBy(OrderStatus.COMPLETED));
		return "orders";
	}

	@GetMapping("/getcreateReservation")
	String getcreateReservation(Model model, RegistrationForm form) {
		return "createReservation";
	}
	
	@PostMapping("/createReservation")
	public String createReservation(@ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount,
			@Valid RegistrationForm form, Errors result) {
		if (result.hasErrors()) {
			return "createReservation";
		}
		User customer = userManagement.createUser(form);
		List<CartItem> temp = new ArrayList<CartItem>();
		for (CartItem item : cart) {
			temp.add(item);
		}
		cart.clear();
		for (CartItem item : temp) {
			cart.addOrUpdateItem(item.getProduct(), Quantity.of(1));

			LocalDate start = userManagement.findByUserAccount(userAccount.get()).getStart(item);
			LocalDate end = userManagement.findByUserAccount(userAccount.get()).getEnd(item);

			customer.getDates().add(userManagement.findByUserAccount(userAccount.get()).getDate(item));
			userManagement.save(customer);
			reservation(cart, Optional.of(customer.getUserAccount()), item.getProductName(), start, end, item);
		}

		return "redirect:/";
	}
	
	@PostMapping("/reserve")
	public String reserve(@ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount,
			@RequestParam(value = "userAccount", required = false) Optional<UserAccount> user) {
				
		if(user != null && (!userAccount.equals(user))){
			if(!userAccount.get().hasRole(Role.of("BOSS")) && !userAccount.get().hasRole(Role.of("EMPLPOYEE")))
			return "redirect:/";
		}

		List<CartItem> temp = new ArrayList<CartItem>();
		for (CartItem item : cart) {
			temp.add(item);
		}

		cart.clear();
		User tempUser = userManagement.findByUserAccount(user.get());
		for (CartItem item : temp) {

			tempUser.getDates().add(userManagement.findByUserAccount(userAccount.get()).getDate(item));
			userManagement.save(tempUser);

			cart.addOrUpdateItem(item.getProduct(), Quantity.of(1));
			LocalDate start = userManagement.findByUserAccount(userAccount.get()).getStart(item);
			LocalDate end = userManagement.findByUserAccount(userAccount.get()).getEnd(item);
			reservation(cart, user, item.getProductName(), start, end, item);
		}
		userManagement.findByUserAccount(user.get()).clearDates();
		userManagement.save(userManagement.findByUserAccount(user.get()));
		return "redirect:/";
	}

	public String reservation(@ModelAttribute Cart cart, Optional<UserAccount> userAccount, String campsite,
			LocalDate start, LocalDate end, CartItem item) {
			
		return userAccount.map(account -> {
			Campsite site = null;
			for(Campsite tempSite : catalog.findByName(item.getProductName())){
				site = tempSite;
			}
			if(site.isPermaCamper()){
				LocalDate pstart;
				LocalDate pend;
				Calendar calendar = Calendar.getInstance();
				calendar.set(start.getYear(), 10, 1);
				if(start.isBefore(LocalDate.of(start.getYear(), 4, 1)))pstart = LocalDate.of(start.getYear(), 04, 01);
				else if(start.isAfter(LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))){
					pstart = LocalDate.of(start.getYear()+1, 04, 01);
				}
				else{pstart = LocalDate.now();}
				pend = LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				site.setStatus(PermaStatus.BOOKED);
				site.setBookedBy(userAccount.get().getUsername());
				System.out.println("Booked by : " + site.getBookedBy());
				site.setVisibility(Visibility.NOT_VISIBLE);
				site.setStartDate(pstart);
				catalog.save(site);
				cart.clear();
				

				Reservation test = new Reservation(userAccount.get().getId(),
					userManagement.findByUserAccount(userAccount.get()).getDate(item).getPrice(), site.isPermaCamper(), site.getName(), start, end);
			reservationCatalog.save(test);

            var order = new Booking(userAccount.get(),
            test.getCampSite(), site.isPermaCamper(), Cash.CASH, test.getPrice(), test.getStartDate(),
            test.getEndDate());
			cart.addItemsTo(order);
			orderManagement.save(order);
			ArrayList<String> tempList = new ArrayList<String>();
			familyService.addFamily(order, tempList, false);
			cart.clear();
			reservationCatalog.delete(test);



			return "redirect:/";
			}
			Reservation test = new Reservation(userAccount.get().getId(),
					userManagement.findByUserAccount(userAccount.get()).getDate(item).getPrice(), site.isPermaCamper(), site.getName(), start, end);
			reservationCatalog.save(test);
			cart.clear();
			return "redirect:/";
		}).orElse("redirect:/cart");

	}

	@PostMapping("/cancelPerma/{user}")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')"+ "|| hasRole('CUSTOMER')")
	String cancelPerma(@RequestParam("campsite") Campsite campsite, @PathVariable UserAccount user, @LoggedIn Optional<UserAccount> userAccount) {
		UserAccount temp = userAccount.get();
		
		if(user != userAccount.get()){
			if(user != userAccount.get() && userAccount.get().hasRole(Role.of("CUSTOMER")))
		{
			return "redirect:/";
		} 	
			
			userAccount = Optional.of(user);
		}

		
			if(campsite.getBookedBy() != null){
				System.out.println("Booked by : " + campsite.getBookedBy());
				if(campsite.getBookedBy().equals(userAccount.get().getUsername())){
					campsite.setStatus(PermaStatus.CANCELED);
			}}
			catalog.save(campsite);
		
		if(temp.hasRole(Role.of("CUSTOMER")))return "redirect:/profile";
		return "redirect:/users";
	}

	@GetMapping("/findPermaByAccount/{userAccount}")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')"+ "|| hasRole('CUSTOMER')")
	public String permaByAccount(Model model, @LoggedIn UserAccount loggedIUserAccount, @PathVariable UserAccount userAccount) {
		if(userAccount != loggedIUserAccount && loggedIUserAccount.hasRole(Role.of("CUSTOMER")))
		{
			return "redirect:/profile";
		} 


		ArrayList<Campsite> perma = new ArrayList<Campsite>();
		for (Campsite campsite : catalog.findAll()) {
			if(campsite.getBookedBy() != null){
				if(campsite.getBookedBy().equals(userAccount.getUsername())){
			 	perma.add(campsite);
			}}
		}
		model.addAttribute("perma", perma);
		model.addAttribute("userAccount", userAccount);
		model.addAttribute("user", userManagement.findByUserAccount(userAccount));
		return "perma";
	}

	@GetMapping("/findOrdersByAccount")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String ordersByAccount(Model model, @LoggedIn UserAccount userAccount) {
		ArrayList<Reservation> reservations = new ArrayList<Reservation>();
		for (Reservation reservation : reservationCatalog.findAll()) {
			if(reservation.getAccount().equals(userAccount.getUsername())){
				reservations.add(reservation);
			}
		}
		
		model.addAttribute("reservations", reservations);
		model.addAttribute("orders", orderManagement.findBy(userAccount));
		model.addAttribute("userAccount", userAccount);
		model.addAttribute("user", userManagement.findByUserAccount(userAccount));
		return "orderhistory";
	}


	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	@GetMapping("/findOrders/{userAccount}")
	public String orderhistory(Model model, @PathVariable UserAccount userAccount) {
		ArrayList<Reservation> reservations = new ArrayList<Reservation>();
		for (Reservation reservation : reservationCatalog.findAll()) {
			if(reservation.getAccount().equals(userAccount.getUsername())){
				reservations.add(reservation);
			}
		}
		model.addAttribute("reservations", reservations);
		model.addAttribute("orders", orderManagement.findBy(userAccount));
		model.addAttribute("userAccount", userAccount);
		model.addAttribute("user", userManagement.findByUserAccount(userAccount));
		return "orderhistory";

	}

	@PostMapping("/bookextra")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String bookextra(@RequestParam("order") Booking order, @LoggedIn Optional<UserAccount> userAccount,
			@RequestParam("extra") Extras extra, @RequestParam("quantity") Quantity quantity) {
		if(quantity.isLessThan(Quantity.of(1)))	return "redirect:/orders";
		order.getExtras().add(new RentedObject(extra, quantity));
		orderManagement.save(order);
		extra.bookExtra(quantity);
		extrasCatalog.save(extra);
		return "redirect:/orders";

	}

	

	@GetMapping("/printInvoice")
	public void printInvoice(@RequestParam("order") Booking order, HttpServletResponse response)
			throws DocumentException, IOException {
		System.out.println("printInvoice");
		response.setContentType("application/pdf");

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Invoice_" + order.getId() + ".pdf";
		response.setHeader(headerKey, headerValue);
		InvoiceGenerator.createInvoice(response, order, userManagement.findByUserAccount(order.getUserAccount()));
	}

	Integer mostBookedDay = 0;
	LocalDate mostBookedDate;

	@GetMapping("/statistics")
	public String statistics(Model model) {

		Campsite mostBooked = null;
		MonetaryAmount income = Money.of(0, EURO);
		MonetaryAmount open = Money.of(0, EURO);
		MonetaryAmount lost = Money.of(0, EURO);
		MonetaryAmount all = Money.of(0, EURO);
		HashMap<LocalDate, Integer> days = new HashMap<LocalDate, Integer>();
		int usercount = 0;
		
		List<Integer> montlyIncomeList = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0); 
		List<Integer> montlyLostList = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0); 

		for (Campsite site : catalog.findAll()) {
			for (LocalDate date : site.getMyBookings()) {
				if (days.containsKey(date))
					days.replace(date, days.get(date) + 1);
				else {
					days.put(date, 1);
				}
			}

			
			days.forEach((key, value) -> {
				if (value > mostBookedDay) {
					mostBookedDay = value;
					mostBookedDate = key;
				}
			});
			if (mostBooked == null)
				mostBooked = site;
			if (mostBooked.getMyBookings().size() < site.getMyBookings().size())
				mostBooked = site;
		}
		for (User user : userManagement.findAll()) {

			usercount++;
		}
		int completedOrders = 0;
		for (Booking order : orderManagement.findBy(OrderStatus.COMPLETED)) {
			income = income.add(order.getCompleteCostsDiscountedMoney());
			completedOrders++;
			montlyIncomeList.set(order.getDateCreated().getMonthValue() - 1, montlyIncomeList.get(order.getDateCreated().getMonthValue() - 1) + order.getCompleteCostsDiscountedMoney().getNumber().intValue());
		}
		int canceledOrders = 0;
		for (Booking order : orderManagement.findBy(OrderStatus.CANCELLED)) {
			lost = lost.add(order.getCompleteCostsDiscountedMoney());
			canceledOrders++;
			montlyLostList.set(order.getDateCreated().getMonthValue() - 1, montlyLostList.get(order.getDateCreated().getMonthValue() - 1) + order.getCompleteCostsDiscountedMoney().getNumber().intValue());
		}
		int waiting = 0;
		for (Booking order : orderManagement.findBy(OrderStatus.PAID)) {
			open = open.add(order.getCompleteCostsDiscountedMoney());
			waiting++;
		}
		int openOrders = 0;
		for (Booking order : orderManagement.findBy(OrderStatus.OPEN)) {
			open = open.add(order.getCompleteCostsDiscountedMoney());
			openOrders++;
		}
		int reservations = 0;
		for(Reservation reservation : reservationCatalog.findAll()){
			open = open.add(reservation.getCompleteCosts());
			System.out.println(reservation.getPrice());
			reservations++;
		}
		all = all.add(open.add(income));
		double costs = all.getNumber().doubleValue();

		model.addAttribute("all", costs);
		model.addAttribute("completedOrders", completedOrders);
		model.addAttribute("canceledOrders", canceledOrders);
		model.addAttribute("openOrders", openOrders);
		model.addAttribute("reservations", reservations);
		model.addAttribute("day", mostBookedDate);
		model.addAttribute("income", income);
		model.addAttribute("usercount", usercount);
		model.addAttribute("mostBooked", mostBooked);
		model.addAttribute("orders", orderManagement);
		model.addAttribute("montlyIncomeList", montlyIncomeList);
		model.addAttribute("montlyLostList", montlyLostList);


		return "statistics";

	}

	@GetMapping("/getOrders")
	String findOrders(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("start") LocalDate start,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("end") LocalDate end,
			@RequestParam("name") String name) {
		
		if ((start != null && end != null) && start.isAfter(end)) {
			LocalDate temp = start;
			start = end;
			end = temp;
		}
		List<Booking> ordersOpen = new ArrayList<Booking>();
		List<Booking> ordersPaid = new ArrayList<Booking>();
		List<Booking> ordersCompleted = new ArrayList<Booking>();
		List<Booking> ordersCanceled = new ArrayList<Booking>();
		for (Booking booking : orderManagement.findBy(OrderStatus.OPEN)) {
			booking.setRequiredBooked(isRequiredBooked(booking));
		}
		if (name == "") {
			for (Booking order : orderManagement.findBy(OrderStatus.OPEN)) {
				ordersOpen.add(order);
			}
			for (Booking order : orderManagement.findBy(OrderStatus.PAID)) {
				ordersPaid.add(order);
			}
			for (Booking order : orderManagement.findBy(OrderStatus.COMPLETED)) {
				ordersCompleted.add(order);
			}
			for (Booking order : orderManagement.findBy(OrderStatus.CANCELLED)) {
				ordersCanceled.add(order);
			}
		} else {
			try {
				for (Booking order : orderManagement.findBy(userAccounts.findByUsername(name).get())) {
					switch (order.getOrderStatus()) {
						case OPEN:
							ordersOpen.add(order);
							break;
						case PAID:
							ordersPaid.add(order);
							break;
						case COMPLETED:
							ordersCompleted.add(order);
							break;
						case CANCELLED:
							ordersCanceled.add(order);
							break;
					}
				}
			} catch (Exception e) {
				System.out.println("BookingController.findOrders()" + e);
			}
			
		}

		if(start != null){
		for(int i = ordersOpen.size()-1; i>=0;i--){
			if(!ordersOpen.get(i).getStartDate().equals(start)){
				ordersOpen.remove(ordersOpen.get(i));
			}
		}
		for(int i = ordersPaid.size()-1; i>=0;i--){
			if(!ordersPaid.get(i).getStartDate().equals(start)){
				ordersPaid.remove(ordersPaid.get(i));
			}
		}
		for(int i = ordersCanceled.size()-1; i>=0;i--){
			if(!ordersCanceled.get(i).getStartDate().equals(start)){
				ordersCanceled.remove(ordersCanceled.get(i));
			}
		}
		for(int i = ordersCompleted.size()-1; i>=0;i--){
			if(!ordersCompleted.get(i).getStartDate().equals(start)){
				ordersCompleted.remove(ordersCompleted.get(i));
			}
		}}



		if(end != null){
		for(int i = ordersOpen.size()-1; i>=0;i--){
			if(!ordersOpen.get(i).getEndDate().equals(end)){
				ordersOpen.remove(ordersOpen.get(i));
			}
		}
		for(int i = ordersPaid.size()-1; i>=0;i--){
			if(!ordersPaid.get(i).getEndDate().equals(end)){
				ordersPaid.remove(ordersPaid.get(i));
			}
		}
		for(int i = ordersCanceled.size()-1; i>=0;i--){
			if(!ordersCanceled.get(i).getEndDate().equals(end)){
				ordersCanceled.remove(ordersCanceled.get(i));
			}
		}
		for(int i = ordersCompleted.size()-1; i>=0;i--){
			if(!ordersCompleted.get(i).getEndDate().equals(end)){
				ordersCompleted.remove(ordersCompleted.get(i));
			}
		}}

		model.addAttribute("title", "campsiteCatalog.all.title");
		model.addAttribute("ordersPaid", ordersPaid);
		model.addAttribute("ordersRunning", ordersOpen);
		model.addAttribute("ordersCancelled", ordersCanceled);
		model.addAttribute("ordersCompleted", ordersCompleted);
		return "orders";
	}

	@PostMapping("/testbooking")
	public String testbooking(@ModelAttribute Cart cart, @RequestParam(value = "userAccount") UserAccount user) {
		Campsite temp = null;
		
		for (CartItem item : cart) {
			for(Campsite site : catalog.findByName(item.getProduct().getName()))
			temp = site;
		}


		LocalDate start = LocalDate.of(2020, 10, 10);
		LocalDate end = LocalDate.of(2020, 10, 24);

		Reservation test = new Reservation(user.getId(),
				Money.of(temp.getPrice().getNumber(), EURO),temp.isPermaCamper(), temp.getName(), start, end);
		reservationCatalog.save(test);

		var order = new Booking(user,
		test.getCampSite(), test.isPermaCamper(),Cash.CASH, test.getPrice(), test.getStartDate(),
		test.getEndDate());
			orderManagement.save(order);
			ArrayList<String> tempList = new ArrayList<String>();
			tempList.add(test.getAccount());
			familyService.addFamily(order, tempList, false);
			reservationCatalog.delete(test);
		orderManagement.payOrder(order);
		orderManagement.save(order);
		order.setTimeStamp(LocalDate.of(2020, 10, 24));
		LocalDate start2 = LocalDate.of(2021, 6, 7);
		LocalDate end2 = LocalDate.of(2021, 6, 16);

		Reservation test2 = new Reservation(userManagement.findByName("a").getId(),
				Money.of(temp.getPrice().getNumber(), EURO),temp.isPermaCamper(), temp.getName(), start2, end2);
		reservationCatalog.save(test2);
		cart.clear();

		return "redirect:/reservation";
	}

}
