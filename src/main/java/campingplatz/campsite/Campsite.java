package campingplatz.campsite;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.money.NumberValue;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.salespointframework.catalog.Product;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Campsite extends Product {
    // Test
    public static enum Size {
        SMALL, MEDIUM, LARGE;

        public static Size of(String size) {
           switch (size) {
               case "SMALL":
                   return SMALL;
                   case "MEDIUM":
                   return MEDIUM;
                   case "LARGE":
                   return LARGE;
                default:
                    return null;
           }
        }
    }

    public static enum ParkingSpace {
        NO, BIKE, CAR, TRAILER;
    }

    public static enum Visibility {
        VISIBLE, NOT_VISIBLE;
    }

    public static enum PermaCamper {
        PERMACAMPER, NOT_PERMACAMPER;
    }
    private String bookedBy;
    public static enum PermaStatus {
        OPEN, BOOKED, CANCELED;
    }
    private PermaStatus permaStatus;
    private String image;
    private Size size;
    private ParkingSpace parkingSpace;
    private String description;
    private Visibility visibility;
    private PermaCamper permacamper;
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ElementCollection
	private List<LocalDate> myBookings;

    @ElementCollection
    private List<Defect> defects;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ElementCollection
	private List<LocalDate> defectDates;
    @SuppressWarnings({"deprecated"})
    public Campsite() {

    }

    public Campsite(String name, String image, Money price, Size size, ParkingSpace parkingSpace, String description, Visibility visibility, PermaCamper permacamper) {
        super(name, price);
        this.image = image;
        this.size = size;
        this.parkingSpace = parkingSpace;
        this.description = description;
        this.visibility = visibility;
        this.permacamper = permacamper; 
    }
 

    public List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        if(startDate.isAfter(endDate)){return endDate.datesUntil(startDate).collect(Collectors.toList());}
        return startDate.datesUntil(endDate).collect(Collectors.toList());
    }

    public void setDatesBetween(LocalDate startDate, LocalDate endDate) {
        for (LocalDate date : getDatesBetween(startDate, endDate)) {
            myBookings.add(date);
        }
    }

    public boolean hasDate(LocalDate date) {
        return myBookings.contains(date);
    }

    public void setPrice(Money price) {
        super.setPrice(price);
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Size getSize() {
        return this.size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public ParkingSpace getParkingSpace() {
        return this.parkingSpace;
    }

    public void setParkingSpace(ParkingSpace parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public PermaCamper getPermaCamper() {
        return this.permacamper;
    }
    public boolean isPermaCamper() {
        if(permacamper.toString().equals("PERMACAMPER"))return true;
        return false;
    }
    public void setPermaCamper(PermaCamper permacamper) {
        this.permacamper = permacamper;
    }

    public List<LocalDate> getMyBookings() {
        return this.myBookings;
    }
    public List<Defect> getDefects() {
        return defects;
    }
    public void deleteDate(LocalDate date){
        myBookings.remove(date);
    }
    public String getPriceRounded() {
		final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
      AmountFormatQueryBuilder.of(Locale.GERMANY)
        .set(CurrencyStyle.NAME)
        .build()
    );
		return format.format(super.getPrice());
	}

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setStatus(PermaStatus status) {
        this.permaStatus = status;
    }
    public PermaStatus getStatus() {
        return permaStatus;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getPermaPrice(){
        final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
        LocalDate start = LocalDate.now();
        if(startDate != null)start = startDate;
        LocalDate end;
        Calendar calendar = Calendar.getInstance();
			calendar.set(start.getYear(), 10, 1);
			if(start.isBefore(LocalDate.of(start.getYear(), 4, 1)))start = LocalDate.of(start.getYear(), 04, 01);
			else if(start.isAfter(LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))){
				start = LocalDate.of(start.getYear()+1, 04, 01);
			}
			else{start = LocalDate.now();}
            calendar.set(start.getYear(), 10, 1);
			end = LocalDate.of(start.getYear(), 10, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			long days = ChronoUnit.DAYS.between(start, end);
            return format.format(super.getPrice().multiply(days+1));
    }

    public String getPermaSeasonPrice(){
        final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
			AmountFormatQueryBuilder.of(Locale.GERMANY)
			  .set(CurrencyStyle.NAME)
			  .build()
		  );
        
        LocalDate start = LocalDate.now();
        LocalDate end;
        start = LocalDate.of(start.getYear(), 4, 1);
		end = LocalDate.of(start.getYear(), 10, 31);
			
			long days = ChronoUnit.DAYS.between(start, end);
            return format.format(super.getPrice().multiply(days));
    }
    
    public boolean isCanceled() {
        switch (permaStatus.toString()) {
            case "CANCELED":
                return true;
                
            default:
            return false;
        }
    }

    public NumberValue getPriceNumber() {
        return super.getPrice().getNumber();
    }

    public List<LocalDate> getDefectDates() {
        return defectDates;
    }

    public boolean defectDatesContainsDate(LocalDate date){
        return defectDates.contains(date);
    }


}