package campingplatz.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import campingplatz.booking.Booking;
import campingplatz.booking.Booking.Payment;
import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;
import campingplatz.extras.Extras;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import campingplatz.extras.ExtrasRepository;
import campingplatz.reservation.Reservation;
import campingplatz.reservation.ReservationCatalog;
@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTests {
	@Autowired
	MockMvc mvc;

	@Autowired
	private UserAccountManagement userAccountManagement;
	@Autowired
	private CampsiteCatalog campsiteCatalog;
	@Autowired
	private ReservationCatalog reservationCatalog;

	@Autowired
	private ExtrasRepository extrasRepository;
    @Autowired
    private OrderManagement<Booking> orderManagement;


	// @Test
	// @WithMockUser(username = "boss", roles = "BOSS")
	// void performReservation() throws Exception {

	// 	Campsite campsite = null;
	// 	for (Campsite temp : campsiteCatalog.findByName("Im Parkhaus 22")) {
	// 		campsite = temp;
	// 	}
    //     UserAccount useraccount = userAccountManagement.findByUsername("a").get();
	// 		mvc.perform(post("/cart")
    //         .param("pid", campsite.getId().toString())
    //         .param("start", LocalDate.now().plusDays(1).toString())
	// 		.param("end", LocalDate.now().plusDays(15).toString()))
	// 		.andExpect(status().is3xxRedirection());


	// 		mvc.perform(post("/reserve")
    //         .param("userAccount", useraccount.getId().toString()));

	// 		Reservation reservation = null;
	// 		for (Reservation temp : reservationCatalog.findAll()) {
	// 			logger.info("reservation: " + temp.toString());
	// 			reservation = temp;
	// 		}

	// 		//Nullpointer
	// 		logger.info(campsite.toString());
	// 		String reservationID = reservation.getId().toString();


	// }

	@Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performCheckIn() throws Exception {

        Campsite campsite = null;
		for (Campsite temp : campsiteCatalog.findByName("Am Hang 5")) {
			campsite = temp;

		}
		LocalDate now = LocalDate.now();
        UserAccount userAccount = userAccountManagement.findByUsername("a").get();

		Reservation reservation = new Reservation(userAccount.getId(), Money.of(campsite.getPrice().getNumber(), EURO), campsite.isPermaCamper(), campsite.getName(), now.plusDays(1), now.plusDays(15));
        reservationCatalog.save(reservation);
			mvc.perform(post("/checkin/" + reservation.getId().toString())
			.param("otherPeople", "Vorname Nachname")
			.param("isInGroup", "true"))
			.andExpect(status().is3xxRedirection());

            Booking open = null;
            for (Booking temp :  orderManagement.findBy(OrderStatus.OPEN)) {
                open = temp;
            }
            assert (open.getStartDate().equals(now.plusDays(1)));            
            assert (open.getDiscount().isPositive());
            assert (open.getFamily().getOtherPeople().contains("Vorname Nachname"));

	}


    @Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performCheckOut() throws Exception {

        Campsite campsite = null;
		for (Campsite temp : campsiteCatalog.findByName("In der Nische 76")) {
			campsite = temp;

		}
		Extras extra = extrasRepository.findExtraByName("Fahrrad");
		LocalDate now = LocalDate.now();
		Booking booking = new Booking(userAccountManagement.findByUsername("a").get(), campsite.getName(),
				campsite.isPermaCamper(), Cash.CASH, campsite.getPrice(), now.plusDays(1),
				now.plusDays(1).plusWeeks(2));
		orderManagement.save(booking);
  		
			
            mvc.perform(post("/bookextra")
			.param("order", booking.getId().toString())
			.param("quantity", "1")
			.param("extra", extra.getId().toString()))
			.andExpect(status().is3xxRedirection());

			mvc.perform(post("/checkout")
			.param("order", booking.getId().toString())
			.param("paid", "Bezahlt")
			.param("payment", "Bar"))
			.andExpect(status().is3xxRedirection());

            Booking completed = null;
            for (Booking temp :  orderManagement.findBy(OrderStatus.COMPLETED)) {
                completed = temp;
            }
            assert (completed.getCampingSite().equals("In der Nische 76"));
            assert (completed.getPayment().equals(Payment.Bar));
	}





}