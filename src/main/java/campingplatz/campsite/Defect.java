package campingplatz.campsite;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.Embeddable;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.springframework.format.annotation.DateTimeFormat;

@Embeddable
public class Defect {
    private UUID id;
    private String name;
    private String campsiteID;

    private String reason;
    private Double costs;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate start, end;
    
    public Defect(){}
    public Defect(Campsite campsite, String reason, Double costs, LocalDate start, LocalDate end){
        this.id = UUID.randomUUID();
        this.name = campsite.getName();
        this.campsiteID = campsite.getId().toString();

        this.reason = reason;
        this.costs = costs;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCampsiteID() {
        return campsiteID;
    }

    public Double getCosts() {
        return costs;
    }
    public String getCostsFormatted(){
            final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
          AmountFormatQueryBuilder.of(Locale.GERMANY)
            .set(CurrencyStyle.NAME)
            .build()
        );  
            MonetaryAmount price = Money.of(costs, EURO);
            return format.format(price);
    }

    public void setCosts(Double costs) {
        this.costs = costs;
    }

    public LocalDate getEnd() {
        return end;
    }
    public LocalDate getStart() {
        return start;
    }
    public void setEnd(LocalDate end) {
        this.end = end;
    }
    public long getDuration(){
        return start.datesUntil(end).count();
    }
    public String getReason() {
        return reason;
    }
    
}
