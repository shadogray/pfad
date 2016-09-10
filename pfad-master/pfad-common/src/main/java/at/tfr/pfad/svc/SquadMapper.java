package at.tfr.pfad.svc;

import java.util.Set;

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
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	SquadDao squadToDao(Squad squad);
	
	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	BaseDao memberToDao(Member member);

	@Mappings({
	@Mapping(target="created", ignore=true),
	@Mapping(target="createdBy", ignore=true),
	@Mapping(target="changed", ignore=true),
	@Mapping(target="changedBy", ignore=true),
	})
	void updateSquad(SquadDao dao, @MappingTarget Squad squad);
}
