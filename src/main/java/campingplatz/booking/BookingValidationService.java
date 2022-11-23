package campingplatz.booking;

import java.time.LocalDate;

import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.springframework.stereotype.Service;

import campingplatz.campsite.Campsite;

@Service
public class BookingValidationService {
    public String validateDate(Cart cart, Campsite campsite, LocalDate start, LocalDate end) {
        String message = "";
       
        if(start == null || end == null) message = message.concat("Es muss ein Start- und ein Enddatum ausgewählt werden\n");	
		else{
			for(LocalDate temp : campsite.getDatesBetween(start, end)){
				if(campsite.hasDate(temp)){
					message = message.concat("Der Platz ist zur gewählten Zeit leider nicht mehr frei\n");
				}
			}
			if(start.equals(end))message = message.concat("Das Startdatum kann nicht gleich dem Abreisedatum sein\n");
			if(start.isBefore(LocalDate.now()))message = message.concat("Das Startdatum kann nicht in der Vergangenheit liegen\n");
			if(end.isBefore(start))message = message.concat("Das Enddatum kann nicht vor dem Startdatum liegen\n");
			for (CartItem cartItem : cart) {
			
				if(cartItem.getProduct().equals(campsite))
				message = message.concat("Sie reservieren diesen Platz bereits. Schließen Sie zunächst den laufenden Vorgang ab\n");
			}
		}
        return message;
    }
}
