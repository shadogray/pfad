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
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	PaymentDao paymentToDao(Payment Paymenrt);
	
	Set<BaseDao> mapBookings(Set<Booking> bookings);
	
	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	BaseDao memberToDao(Member member);

}
