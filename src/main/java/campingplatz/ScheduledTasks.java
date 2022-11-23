package campingplatz;

import org.javamoney.moneta.Money;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.useraccount.UserAccount;
import org.simplejavamail.api.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.simplejavamail.email.EmailBuilder;
import campingplatz.booking.Booking;
import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;
import campingplatz.campsite.Campsite.PermaStatus;
import campingplatz.campsite.Campsite.Visibility;
import campingplatz.family.FamilyService;
import campingplatz.reservation.Reservation;
import campingplatz.reservation.ReservationCatalog;
import campingplatz.user.User;
import campingplatz.user.UserManagement;
import static org.salespointframework.core.Currencies.*;
import campingplatz.mail.Mail;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

@Component
public class ScheduledTasks {
    @Autowired
	CampsiteCatalog catalog;
    @Autowired
    ReservationCatalog reservationCatalog;
    @Autowired
	UserManagement userManagement;
    @Autowired
	OrderManagement<Booking> orderManagement;
    @Autowired
	FamilyService familyService;
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    //Löscht um 0:00Uhr alle nicht angetretenen Reservierungen
    //@Scheduled(fixedRate = 100000)
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkReservations(){
        logger.info("ScheduledTasks: Checked Reservations");
        for (Reservation reservation : reservationCatalog.findAll()) {
			if(reservation.getStartDate().isBefore(LocalDate.now())){
				reservationCatalog.delete(reservation);
                logger.info("Reservierung gelöscht " + reservation);
			}
		}
    }

    //@Scheduled(fixedRate = 100000)
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkCampsitePayment(){
        logger.info("ScheduledTasks: Checked Payment 2 weeks");
        for (Booking booking : orderManagement.findBy(OrderStatus.PAID)) {
            if(!booking.isTimeStampOK()){
                
                try {
                    Email mail = EmailBuilder.startingBlank().from("CampingplatzGruppe2@gmail.com")
                            .to(booking.getUserAccount().getUsername()).withSubject("Zahlungserrinnerung")
                            .withPlainText("Sehr geehrter Kunde "+booking.getUserAccount().getLastname() + 
                            ",\nsicherlich haben Sie die Rechnung vom "+ booking.getTimeStamp()+ " in Höhe von "+ booking.getCompleteCostsRounded() +" lediglich übersehen und daher den Betrag noch nicht ausgeglichen.\n"+
                            "Wir bitten Sie, den Ausgleich schnellstmöglich vorzunehmen.\n"+
                            "Mit freundlichen Grüßen"
                            )
                            .buildEmail();
                    Mail.sendEMail(mail);
                    logger.info("Benachrichtigung verschickt " + booking.getUserAccount().getUsername());

                } catch (Exception e) {
                    logger.info("Fehler beim Versand der Benachrichtigung " + booking.getUserAccount().getUsername() +"\n" + e);
                }
                
                
            }
             
        }
    }

     //@Scheduled(fixedRate = 100001)
     @Scheduled(cron = "0 0 0 * * ?")
     public void checkCampsitePayment2(){
         logger.info("ScheduledTasks: Checked Payment 1 Month");
         for (Booking booking : orderManagement.findBy(OrderStatus.PAID)) {
             if(!booking.isTimeStampOK2()){
                 UserAccount userAccount = booking.getUserAccount();
                User user = userManagement.findByUserAccount(userAccount);
                 try {
                    logger.info(userAccount.getUsername());
                    logger.info(userAccount.getFirstname() + " " + userAccount.getLastname() );
                    logger.info(user.getStreet() + "\n"+user.getPlz() + " "+ user.getCity());
                    logger.info(booking.getCampingSite() + " " + booking.getCompleteCostsRounded());
                    logger.info(booking.getFamily().getOtherPeople().toString());
 
                 } catch (Exception e) {
                     logger.info("Fehler beim Versand der Benachrichtigung " + booking.getUserAccount().getUsername() +"\n" + e);
                 }
                 
                 
             }
              
         }
     }
   
    //@Scheduled(fixedRate = 100000)
    @Scheduled(cron = "0 0 0 01 11 ?")
    public void scheduleTaskWithFixedRate() {
    for(Campsite campsite : catalog.findAll()){
       
        if(campsite.getStatus() == PermaStatus.CANCELED){
            System.out.println("Platz nicht verlängert");
            campsite.setStartDate(null);
            campsite.setBookedBy(null);
            campsite.setVisibility(Visibility.VISIBLE);
            campsite.setStatus(PermaStatus.OPEN);
        }
        else if(campsite.getStatus() == PermaStatus.BOOKED){
            System.out.println("Platzverlängert");
            LocalDate start = LocalDate.now();            
            start = LocalDate.of(start.getYear()+1, 04, 01);
            Calendar calendar = Calendar.getInstance();
			calendar.set(start.getYear(), 10, 1);
            LocalDate end = LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            campsite.setStartDate(start);
            UserAccount userAccount = userManagement.findByName(campsite.getBookedBy());
            Reservation test = new Reservation(userAccount.getId(), Money.of(campsite.getPrice().getNumber(), EURO), campsite.isPermaCamper(), campsite.getName(), start, end);
			reservationCatalog.save(test);

            var order = new Booking(userAccount,
            test.getCampSite(), true, Cash.CASH, test.getPrice(), test.getStartDate(),
            test.getEndDate());
            Cart cart = new Cart();
			cart.addItemsTo(order);
			orderManagement.save(order);
            ArrayList<String> tempList = new ArrayList<String>();
			familyService.addFamily(order, tempList, false);
			cart.clear();
			reservationCatalog.delete(test);
        }
        catalog.save(campsite);
        
    }
}
}

