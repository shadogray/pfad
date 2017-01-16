package at.tfr.pfad.svc;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Mapper(uses={BaseDaoMapper.class})
public interface BookingMapper {

	BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
		@Mapping(target="contacts", ignore=true),
		@Mapping(target="details", expression="java( toString() )"),
	})
	BookingDao toDao(Booking booking);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
		@Mapping(target="vollzahler", ignore=true),
		@Mapping(target="siblings", ignore=true),
		@Mapping(target="parents", ignore=true),
		@Mapping(target="funktionen", ignore=true),
		@Mapping(target="geburtstag", expression="java(baseDaoMapper.memberGeburtstag(member))"),
		@Mapping(target="assisting", ignore=true),
		@Mapping(target="responsible", ignore=true),
		@Mapping(target="contacts", ignore=true),
		@Mapping(target="details", source="longString"),
	})
	MemberDao toMemberDao(Member member);

	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
		@Mapping(target="contacts", ignore=true),
		@Mapping(target="details", ignore=true),
	})
	SquadDao toDao(Squad squad);

}
