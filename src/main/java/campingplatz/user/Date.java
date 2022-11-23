package campingplatz.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.persistence.Embeddable;

import org.javamoney.moneta.Money;


@Embeddable
public class Date implements Serializable{
    
    private LocalDate start;
    private LocalDate end;
    private String item;
    private Money price;
    private Long duration;
    private Money completePrice;

    public Date() { 
    }

    public Date(String item, LocalDate start, LocalDate end, Money price){
        this.item = item;
        this.start = start;
        this.end = end;
        this.price = price;
        this.duration = ChronoUnit.DAYS.between(start, end);
        completePrice = price.multiply(duration);
    }

    public void setDates(LocalDate start, LocalDate end){
        if(start == null) start = this.start;
        if(end == null) end = this.end;
        
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart(){
        return start;
    }

    public LocalDate getEnd(){
        return end;
    }
    public Money getPrice(){
        return price;
    }
    public String getItem(){
        return item;
    }
    public long getDuration(){
		return duration;
	}
	
	public Money getCompleteCosts(){
		return completePrice;
	}
}
