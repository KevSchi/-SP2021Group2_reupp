package campingplatz.booking;
import org.salespointframework.order.OrderIdentifier;
import org.springframework.data.util.Streamable;

public interface BookingRepository extends org.springframework.data.repository.Repository<Booking,OrderIdentifier>{  
	Streamable<Booking> findAll();

}