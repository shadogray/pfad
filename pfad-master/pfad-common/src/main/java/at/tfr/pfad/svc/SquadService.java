package at.tfr.pfad.svc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Stateless
public class SquadService {

	private SquadMapper sm = SquadMapper.INSTANCE;
	@Inject
	private SquadRepository squadRepo;
	
	public SquadDao findBy(Long id) {
		return sm.squadToDao(squadRepo.findBy(id));
	}
	
	public SquadDao update(SquadDao dao) {
		Squad m = squadRepo.findBy(dao.getId());
		sm.updateSquad(dao, m);
		return sm.squadToDao(m);
	}
	
	public SquadDao save(SquadDao dao) {
		Squad m = new Squad();
		sm.updateSquad(dao, m);
		squadRepo.save(m);
		return sm.squadToDao(m);
	}
	
	public List<SquadDao> map(Collection<Squad> squads) { 
		return squads.stream().map(s -> sm.squadToDao(s)).collect(Collectors.toList());
	}
	
}
