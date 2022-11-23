package campingplatz.extras;


import java.io.Serializable;
import java.util.Locale;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;

import javax.money.NumberValue;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.Entity;

import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;


@Entity
public class Extras extends Product implements Serializable{
	
	private String image;
	private Quantity quantity;
	private boolean returnable;
	private boolean required;
	private boolean paidDaily;
	@SuppressWarnings({ "unused", "deprecation" })
	private Extras() {}


	public Extras(String name, String image, Money price, Quantity quantity, boolean returnable, boolean required, boolean paidDaily) {

		super(name,price);
		this.image = image;
		this.quantity = quantity;
		this. returnable = returnable;
		this.required = required;
		this.paidDaily = paidDaily;
	}

	@Override
	public Money getPrice() {
		return (Money) super.getPrice();
	}
	public String getPriceRounded() {
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
      AmountFormatQueryBuilder.of(Locale.GERMANY)
        .set(CurrencyStyle.NAME)
        .build()
    );
		return format.format(super.getPrice());
	}
	public Extras getExtra() {
		return this;
	}

	public String getImage() {
		return image;
	}
	public Quantity getQuantity() {
		return quantity;
	}
	public boolean isReturnable() {
		return returnable;
	}
	public NumberValue getPriceNumber() {
		return super.getPrice().getNumber();
	}
	public void setPrice(Money price){
		super.setPrice(price);
	}

	public void setQuantity(Quantity quantity){
		this.quantity = quantity;
	}

	public void returnExtra(Quantity rquantity){
		this.quantity = this.quantity.add(rquantity);
	}
	public void bookExtra(Quantity rquantity){
		this.quantity = this.quantity.subtract(rquantity);
	}
	public void setReturnable(boolean returnable){
		this.returnable = returnable;
	}
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isPaidDaily() {
		return paidDaily;
	}
	public void setPaidDaily(boolean paidDaily) {
		this.paidDaily = paidDaily;
	}
}

