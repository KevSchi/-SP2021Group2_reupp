package campingplatz.booking;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Embeddable;
import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Quantity;
import campingplatz.extras.Extras;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import org.javamoney.moneta.format.CurrencyStyle;
import java.util.Locale;

@Embeddable
public class RentedObject implements Serializable{
    private UUID id;
    private String extraID;
    private String name;
    private Money price;
    private Quantity quantity;
    private Boolean returnedItem;
    private boolean returnable;
    private boolean required;
    private boolean paidDaily;
    private LocalDate dateRented, dateReturned, dateCheckOut;
    public RentedObject() {
        
    }

    public RentedObject(Extras extra, Quantity quantity) {
        this.id = UUID.randomUUID();
        this.paidDaily = extra.isPaidDaily();
        name = extra.getName();
        price = extra.getPrice();
        extraID = extra.getId().toString();
        this.quantity = quantity;
        dateRented = LocalDate.now();
        this.required = extra.isRequired();
        returnable = extra.isReturnable();
        if(extra.isReturnable())returnedItem = false;
        else{returnedItem = true;}
    }

    public UUID getId(){return id;}
    public String getName(){return name;}
    
    public Money getPrice(){
        long days;
        if(paidDaily){
            if(!returnedItem){
                days = dateRented.datesUntil(LocalDate.now()).count()+1;        
            }else if(returnable){
                days = dateRented.datesUntil(dateReturned).count()+1;        
            }else {
                if(dateCheckOut != null) days = dateRented.datesUntil(dateCheckOut).count()+1;        
                else{days = dateRented.datesUntil(LocalDate.now()).count()+1;}
            }
        return (price.multiply(days));}
        else{return price;}
    }
    
    public String getPriceRounded() {
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
      AmountFormatQueryBuilder.of(Locale.GERMANY)
        .set(CurrencyStyle.NAME)
        .build()
    );   
        long days;
        if(paidDaily){
        if(!returnedItem){
                days = dateRented.datesUntil(LocalDate.now()).count()+1;        
            }else if(returnable){
                days = dateRented.datesUntil(dateReturned).count()+1;        
            }else {
                if(dateCheckOut != null) days = dateRented.datesUntil(dateCheckOut).count()+1;        
                else{days = dateRented.datesUntil(LocalDate.now()).count()+1;}
            }
            return format.format(price.multiply(days));}
            else{return format.format(price);}
	}
    
    public boolean isRequired() {
        return required;
    }
    public String getExtraID() {
        return extraID;
    }
    public Quantity getQuantity(){return quantity;}
    public boolean isReturnable(){return returnable;}
    public void setReturned(boolean returned){this.returnedItem = returned;}
    public Boolean hasReturnedItem() {return returnedItem; }
    public Money getTotal(){return price.multiply(quantity.getAmount());}
    public LocalDate getDateRented() {return dateRented; }
    public LocalDate getDateReturned() {return dateReturned;}
    public void setDateReturned(LocalDate dateReturned) {this.dateReturned = dateReturned;}
    public void setPaidDaily(boolean paidDaily) {this.paidDaily = paidDaily;}
    public void setDateCheckOut(LocalDate dateCheckOut) {
        this.dateCheckOut = dateCheckOut;
    }
}
