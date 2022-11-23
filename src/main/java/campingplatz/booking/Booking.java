package campingplatz.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.salespointframework.order.Order;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import campingplatz.extras.Extras;
import campingplatz.family.Family;


@Entity
public class Booking extends Order{
	public static enum Payment {
        Bar, EC_Karte, Kreditkarte, Überweisung;
    }
	Payment payment;
	String campingSite;
	String reason;
	Double tax = 0.07;
	MonetaryAmount price;
    LocalDate startDate, endDate, timeStamp;
	boolean isPermaCamper;
	boolean isRequiredBooked;
	ArrayList<String> missing;
	@ElementCollection
	private List<RentedObject> rented;
	Family family;
	//ArrayList<RentedObject> rented = new ArrayList<RentedObject>();
	
    public Booking(UserAccount userAccount, String campingSite, boolean isPermaCamper, PaymentMethod paymentMethod, MonetaryAmount price, LocalDate startDate, LocalDate endDate) {
		super(userAccount, paymentMethod);
		this.campingSite = campingSite;
		this.price = price;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isPermaCamper = isPermaCamper;
	}
		
	public void addFamily(Family family){
		this.family = family;
		 
	}

	public Family getFamily() {
		return family;
	}
	@SuppressWarnings({"unused", "deprecated"})
	private Booking() {
		
	}
	public LocalDate getStartDate(){
		return startDate;
	}
	public LocalDate getEndDate(){
		return endDate;
	}
	public LocalDate getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(LocalDate timeStamp) {
		this.timeStamp = timeStamp;
	}
	//Prüft ob eine Zahlung seit zwei Wochen nicht bezahlt wurde
	public boolean isTimeStampOK(){
		return LocalDate.now().isBefore(timeStamp.plusWeeks(2));
	}
	//Prüft ob eine Zahlung seit einem Monat nicht bezahlt wurde
	public boolean isTimeStampOK2(){
		return LocalDate.now().isBefore(timeStamp.plusWeeks(4));
	}
	public MonetaryAmount getPrice(){
		return price;
	}
 
	public Quantity getQuantity(Extras extra){
		Quantity temp = Quantity.of(0);
		for (RentedObject item : rented) {
			if(item.getName().equals(extra.getName()))temp.add(item.getQuantity());
		}
		return temp;
	}
	public List<RentedObject> getExtras(){
		return rented;
	}
	public void setPayment(Payment payment){
		this.payment = payment;
	}
	
	public String getExtrasTotalRounded(){
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
      AmountFormatQueryBuilder.of(Locale.GERMANY)
        .set(CurrencyStyle.NAME)
        .build()
    );

		BigDecimal total = new BigDecimal(0);
		for(RentedObject item: rented){
			BigDecimal big = (item.getPrice()).getNumberStripped();
			total = total.add(big.multiply(item.getQuantity().getAmount()));
		}
		return format.format(Money.of(total, EURO));
	}

	public Money getExtrasTotal(){
		BigDecimal total = new BigDecimal(0);
		for(RentedObject item: rented){
			BigDecimal big = (item.getPrice()).getNumberStripped();
			total = total.add(big.multiply(item.getQuantity().getAmount()));
		}
		return Money.of(total, EURO);
	}

	public Money getTotal(Money price){
		BigDecimal total = new BigDecimal(0);
		for(RentedObject item: rented){
			BigDecimal big = (item.getTotal()).getNumberStripped();
			total = total.add(big);
		}
		total = total.add(price.getNumberStripped());
		return Money.of(total, EURO);
	}
	
	public String getCampingSite(){
		return campingSite;
	}
	public void printList(){
		System.out.println("MyOrder.printList()");
		rented.forEach(item -> System.out.println(item));
	}
	public Payment getPayment(){
		return payment;
	}
	public long getDuration(){
		return ChronoUnit.DAYS.between(startDate, endDate);
	}

	public MonetaryAmount getCompleteSiteCostsUndiscounted(){
		return price.multiply(getDuration());
	}
	public MonetaryAmount getCompleteSiteCostsDiscounted(){
		Money costs = Money.of(price.multiply(getDuration()).getNumber(),EURO);
		costs = costs.divide(1+tax);
		if(isInGroup()) return (costs.multiply(0.95)).multiply(1+tax);
		return costs.multiply(1+tax);
	}


	//Gibt die Kosten des Platzes mit Rabatt
	public String getCompleteCostsRounded(){
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
		Money costs = Money.of(price.multiply(getDuration()).getNumber(),EURO);
		costs = costs.divide(1+tax); //Kosten ohne Extras ohne Steuer
		if(isInGroup()) costs = costs.multiply(0.95); // Platz mit Rabatt
		costs = costs.multiply(1+tax);
		return format.format(costs);
	}

	//Gibt die Kosten des Platzes und der Extras ohne Rabatt mit Steuer
	public Money getCompleteCostsUndiscountedMoney(){
		Money costs = Money.of(price.multiply(getDuration()).getNumber(),EURO);
		costs = costs.add(getExtrasTotal());
		return costs;
	}
	//Gibt den Preis des Platzes und der Extras ohne Steuer und ohne Rabatt
	public Money getCompleteCostsUndiscountedWithoutTaxMoney(){
		Money costs = Money.of(price.multiply(getDuration()).getNumber(),EURO);
		costs = costs.divide(1+tax); //Kosten ohne Extras ohne Steuer
		costs = costs.add(getExtrasTotal().divide(1+tax));// Plus Extras ohne Steuer
		return costs;
	}
	//Gibt den Preis des Platzes und der Extras mit Rabatt ohne Steuer
	public MonetaryAmount getCompleteCostsDiscountedMoney(){
		MonetaryAmount test = price.multiply(getDuration());
		try {
			test = test.divide(1+tax); //Kosten ohne Extras ohne Steuer
			if(isInGroup()){
				test = test.multiply(0.95); //Rabatt auf Platz ohne Steuer
			}
			test = test.add(getExtrasTotal().divide(1+tax)); // Plus Extras ohne Steuer
			test = test.multiply(1+tax);
		} catch (Exception e) {
			System.out.println("getCompleteCostsDiscountedMoney" + e);		
}
		return test;
	}
	//Gibt den Preis des Platzes mit Extras ohne Steuer
	public String getCompleteCostsDiscountedMoneyRounded(){
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
		MonetaryAmount test = price.multiply(getDuration());
		try {
			test = test.divide(1+tax); //Kosten ohne Extras ohne Steuer
			if(isInGroup()){
				test = test.multiply(0.95); //Rabatt auf Platz ohne Steuer
			}
			test = test.add(getExtrasTotal().divide(1+tax)); // Plus Extras ohne Steuer
			test = test.multiply(1+tax);
		} catch (Exception e) {
			System.out.println("getCompleteCostsDiscountedMoney" + e);		
}
		return format.format(test);
	}
	//Gibt den Rabatt auf den Platz
	public MonetaryAmount getDiscount(){
		if(isInGroup()){
		MonetaryAmount costs = price.multiply(getDuration());
		costs = costs.divide(1+tax); //komplette Kosten ohne Steuer
		costs = costs.multiply(0.05); //Rabatt auf den Preis ohne Steuer
		return costs;
		}
		else return Money.of(0,EURO);
	}
	//Gibt die Steuer auf Platz und Extras
	public MonetaryAmount getTax(){
		
		MonetaryAmount costs = getCompleteCostsDiscountedMoney();
		MonetaryAmount taxes = costs.divide(1+tax);
		taxes = costs.subtract(taxes);
		return taxes;
	}

	public void setReason(String reason){
		this.reason = reason;
	}
	public String getReason(){
		return reason;
	}
	public UserAccountIdentifier getName(){
		return super.getUserAccount().getId();
	}
	public Boolean isInGroup(){
		return family.isIsInGroup();
	}
	public boolean isEverythingReturned(){
		boolean isEverythingReturned = true;
		for (RentedObject rentedObject : rented) {
			if(!rentedObject.hasReturnedItem())isEverythingReturned = false;
		}
		return isEverythingReturned;
	}
	public boolean isRequiredBooked() {
		return isRequiredBooked;
	}
	public void setRequiredBooked(boolean isRequiredBooked) {
		this.isRequiredBooked = isRequiredBooked;
	}
	public void setMissing(ArrayList<String> missing) {
		this.missing = missing;
	}
	public ArrayList<String> getMissing() {
		return missing;
	}
	public String getMissingName(String missing){
		int index = 0;
		try {
			index = missing.indexOf(";");
		} catch (Exception e) {
			//TODO: handle exception
		}
		if(index < 0)return "";
		return missing.substring(0, index);
	}

	public String getMissingID(String missing){
		int index = 0;
		try {
			index = missing.indexOf(";")+1;
		} catch (Exception e) {
			//TODO: handle exception
		}
		if(index < 0)return "";
		return missing.substring(index);
	}
}	