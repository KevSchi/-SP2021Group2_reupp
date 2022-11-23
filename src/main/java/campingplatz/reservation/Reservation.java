package campingplatz.reservation;



import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import org.salespointframework.catalog.Product;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Reservation extends Product{
	
	String user;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@ElementCollection
	private List<LocalDate> myBookings = new ArrayList<LocalDate>();
	LocalDate startDate, endDate;
	PaymentMethod payment;
	String campSite;
	boolean permacamper;
	boolean isReservationOK;
	LocalDate start, end;
	public List<LocalDate> getDatesBetween() {
	   
		  return startDate.datesUntil(endDate)
			.collect(Collectors.toList());
	  }


	@SuppressWarnings({ "unused", "deprecation" })
	private Reservation() {}


	public Reservation(UserAccountIdentifier account,Money money,boolean permacamper, String campSite, LocalDate startDate, LocalDate endDate) {
		
		super(account.toString(), money);
		user = account.toString();
		this.startDate = startDate;
		this.endDate = endDate;
		this.campSite = campSite;
		this.permacamper = permacamper;
		isReservationOK = true;
		for(LocalDate date : getDatesBetween()){
			try {
				myBookings.add(date);
			} catch (Exception e) {
				System.out.println("Fehler in Reservation: " + e);
			}
			
		}
	}

	//Testfunktion
	public void printReservedDays(){
		for(LocalDate date : myBookings){
			System.out.println(date);
		}
	}

	public void setPrice(Money price){
		super.setPrice(price);
	}

	public void setStartDate(LocalDate date){
		this.startDate = date;
	}

	public void setEndDate(LocalDate date){
		this.endDate = date;
	}

	public void setPaymentMethod(PaymentMethod payment){
		this.payment = payment;
	}
	
	public MonetaryAmount getPrice(){
		return super.getPrice();
	}
	public String getPriceRounded(){
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
		return format.format(super.getPrice());
	}
	public LocalDate getStartDate(){
		return startDate;
	}

	public LocalDate getEndDate(){
		return endDate;
	}

	public long getDuration(){	
		return ChronoUnit.DAYS.between(startDate, endDate);
	}
	public PaymentMethod getPayment(){
		return payment;
	}
	public MonetaryAmount getCompleteCosts(){
		return super.getPrice().multiply(getDuration());
	}
	public String getCompleteCostsRounded(){
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
		return format.format(super.getPrice().multiply(getDuration()));
	}
	public String getAccount(){
		return  user;	
	}
	public String getCampSite(){
		return campSite;
	}

	public boolean isPermaCamper() {
        return permacamper;
    }
	public boolean isReservationOK() {
		return isReservationOK;
	}
	public void setReservationOK(boolean isReservationOK, LocalDate start, LocalDate end) {
		this.isReservationOK = isReservationOK;
		this.start = start;
		this.end = end;
	}
	public LocalDate getStart() {
		return start;
	}
	public LocalDate getEnd() {
		return end;
	}
	public void setStart(LocalDate start) {
		this.start = start;
	}
	public void setEnd(LocalDate end) {
		this.end = end;
	}
}

