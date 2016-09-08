package at.tfr.pfad.svc;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

@Mapper(uses={BaseDaoMapper.class})
public interface BookingMapper {

	BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	BookingDao bookingToDao(Booking booking);
	
	Set<BaseDao> mapPayments(Set<Payment> bookings);
	
	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	BaseDao memberToDao(Member member);

}
