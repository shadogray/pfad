package at.tfr.pfad.dao;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;

@Repository
public abstract class BookingRepository implements EntityRepository<Booking, Long>{

	@Query
	public abstract List<Booking> findByActivity(Activity activity);
	
}
