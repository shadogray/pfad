package at.tfr.pfad.svc;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Mapper(uses={BaseDaoMapper.class})
public interface MemberMapper {

	MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
		@Mapping(target="siblings", expression="java(baseDaoMapper.toReferences(member.getSiblings()))"),
		@Mapping(target="parents", expression="java(baseDaoMapper.toReferences(member.getParents()))"),
	})
	MemberDao memberToDao(Member member);

	@Mappings({
		@Mapping(target="shortName", ignore=true),
		@Mapping(target="longName", ignore=true),
	})
	BaseDao mapSquad(Squad squad);
	
	@Mappings({
	@Mapping(target="created", ignore=true),
	@Mapping(target="createdBy", ignore=true),
	@Mapping(target="changed", ignore=true),
	@Mapping(target="changedBy", ignore=true),
	@Mapping(target="payments", ignore=true),
	@Mapping(target="bookings", ignore=true),
	@Mapping(target="paymentsIds", ignore=true),
	@Mapping(target="bookingsIds", ignore=true),
	})
	void updateMember(MemberDao dao, @MappingTarget Member member);
	Set<Member> mapDaos(Set<MemberDao> members);

}
