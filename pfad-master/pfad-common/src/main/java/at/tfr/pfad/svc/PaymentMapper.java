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
public interface PaymentMapper {

	PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
		@Mapping(target="contacts", ignore=true),
		@Mapping(target="details", expression="java( toString() )"),
	})
	PaymentDao toDao(Payment payment);
	
	Set<BaseDao> mapBookings(Set<Booking> bookings);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
		@Mapping(target="contacts", ignore=true),
		@Mapping(target="details", ignore=true),
	})
	BaseDao toDao(Member member);

}
