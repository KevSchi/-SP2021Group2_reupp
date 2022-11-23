package campingplatz.controller;



import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import campingplatz.booking.Booking;
import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;
import campingplatz.campsite.Campsite.ParkingSpace;
import campingplatz.campsite.Campsite.Size;
import campingplatz.campsite.Campsite.Visibility;

import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import campingplatz.reservation.Reservation;
import campingplatz.reservation.ReservationCatalog;

@SpringBootTest
@AutoConfigureMockMvc
class CampsiteControllerIntegrationTests {
	@Autowired
	MockMvc mvc;

	@Autowired
	private UserAccountManagement userAccountManagement;
	@Autowired
	private CampsiteCatalog campsiteCatalog;
	@Autowired
	private ReservationCatalog reservationCatalog;
    @Autowired
    private OrderManagement<Booking> orderManagement;


    @Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void performChangeCampsite() throws Exception {

        Campsite campsite = null;
		for (Campsite temp : campsiteCatalog.findByName("Auf der Insel 97")) {
			campsite = temp;

		}

        assert (campsite.getParkingSpace().equals(ParkingSpace.TRAILER));            
        assert (campsite.getSize().equals(Size.MEDIUM));
        assert (campsite.getVisibility().equals(Visibility.VISIBLE));

			mvc.perform(post("/campsite/change/")
			.param("id", campsite.getId().toString())
            .param("name", "name")
            .param("size", "SMALL")
            .param("parking", "BIKE")
            .param("info", "text")
            .param("price", "9.99")
            .param("visibility", "NOT_VISIBLE")
            .param("permacamper", "NOT_PERMACAMPER"))
			.andExpect(status().is3xxRedirection());

          Campsite changed = null;
		for (Campsite temp : campsiteCatalog.findByName("name")) {
			changed = temp;
		}
            assert (changed.getParkingSpace().equals(ParkingSpace.BIKE));            
            assert (changed.getSize().equals(Size.SMALL));
            assert (changed.getVisibility().equals(Visibility.NOT_VISIBLE));
            assert (changed.getDescription().equals("text"));

            campsite = null;
            for (Campsite temp : campsiteCatalog.findByName("Auf der Insel 97")) {
                campsite = temp;
            }
            assert (campsite == null);

	}



    
}
