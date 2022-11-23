package campingplatz.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import campingplatz.booking.Booking;
import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;
import campingplatz.extras.Extras;
import campingplatz.extras.ExtrasRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ExtrasControllerIntegrationTest {
	@Autowired
	MockMvc mvc;
	@Autowired
	ExtrasRepository extrasRepository;
	@Autowired
	OrderManagement orderManagement;
	@Autowired
	UserAccountManagement userAccountManagement;
	@Autowired
	CampsiteCatalog campsiteCatalog;

	@Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performCreateExtra() throws Exception {

		mvc.perform(post("/createExtra").param("name", "test").param("price", "5.00").param("quantity", "10")
				.param("returnable", "true").param("imagename", "ball").param("isRequired", "false"))
				.andExpect(status().is3xxRedirection());
		assert (extrasRepository.findExtraByName("test").getQuantity().equals(Quantity.of(10)));
	}

	@Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performBookExtra() throws Exception {
		Campsite campsite = null;
		for (Campsite temp : campsiteCatalog.findByName("Am Hang 5")) {
			campsite = temp;

		}
		Extras extra = extrasRepository.findExtraByName("Ball");
		LocalDate now = LocalDate.now();
		Quantity quantity = extrasRepository.findExtraByName("Ball").getQuantity();
		Booking booking = new Booking(userAccountManagement.findByUsername("a").get(), campsite.getName(),
				campsite.isPermaCamper(), Cash.CASH, campsite.getPrice(), now.plusDays(1),
				now.plusDays(1).plusWeeks(2));
		orderManagement.save(booking);
		mvc.perform(post("/bookextra").param("order", booking.getId().toString())
				.param("extra", extra.getId().toString())
				.param("paidDaily", "true")
				.param("quantity", "2"))
				.andExpect(status().is3xxRedirection());

		assert (extrasRepository.findExtraByName("Ball").getQuantity().equals(quantity.subtract(Quantity.of(2))));
	}

	@Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performChangeExtra() throws Exception {

		Extras extra = extrasRepository.findExtraByName("Ball");

		mvc.perform(post("/extra/" + extra.getId().toString() + "/").param("extra", extra.getId().toString())
				.param("price", "2").param("extraquantity", "5")).andExpect(status().is3xxRedirection());

		assert (extrasRepository.findExtraByName("Ball").getQuantity().equals(Quantity.of(5)));
		assert (extrasRepository.findExtraByName("Ball").getPrice().equals(Money.of(2, EURO)));

	}

}