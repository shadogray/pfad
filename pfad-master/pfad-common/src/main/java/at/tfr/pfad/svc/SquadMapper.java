package at.tfr.pfad.svc;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Mapper(uses={BaseDaoMapper.class})
public interface SquadMapper {

	SquadMapper INSTANCE = Mappers.getMapper(SquadMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
	})
	SquadDao squadToDao(Squad squad);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
	})
	BaseDao memberToDao(Member member);

	@Mappings({
		@Mapping(target="scouts", ignore=true),
		@Mapping(target="created", ignore=true),
		@Mapping(target="createdBy", ignore=true),
		@Mapping(target="changed", ignore=true),
		@Mapping(target="changedBy", ignore=true),
	})
	void updateSquad(SquadDao dao, @MappingTarget Squad squad);
}
