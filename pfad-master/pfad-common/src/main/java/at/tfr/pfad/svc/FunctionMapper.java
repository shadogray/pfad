package at.tfr.pfad.svc;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

@Mapper(uses={BaseDaoMapper.class})
public interface FunctionMapper {

	FunctionMapper INSTANCE = Mappers.getMapper(FunctionMapper.class);
	
	@Mappings({
		@Mapping(target="shortName", source="shortString"),
		@Mapping(target="longName", source="longString"),
	})
	FunctionDao functionToDao(Function function);
	
}
