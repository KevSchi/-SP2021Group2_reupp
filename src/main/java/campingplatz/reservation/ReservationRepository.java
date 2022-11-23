package campingplatz.reservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

@Repository("reservationRepository")
public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	@Override
	Streamable<Reservation> findAll();
	Streamable<Reservation> findByUser(String user);
}