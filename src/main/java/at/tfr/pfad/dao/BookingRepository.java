package at.tfr.pfad.dao;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Booking;

@Repository
public abstract class BookingRepository implements EntityRepository<Booking, Long>{

}
