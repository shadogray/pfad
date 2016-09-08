package at.tfr.pfad.svc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.model.Member;

@Stateless
public class MemberService {

	private MemberMapper mm = MemberMapper.INSTANCE;
	@Inject
	private MemberRepository memberRepo;
	
	public MemberDao findBy(Long id) {
		return mm.memberToDao(memberRepo.fetchBy(id));
	}
	
	public MemberDao update(MemberDao dao) {
		Member m = memberRepo.findBy(dao.getId());
		mm.updateMember(dao, m);
		return mm.memberToDao(m);
	}
	
	public MemberDao save(MemberDao dao) {
		Member m = new Member();
		mm.updateMember(dao, m);
		memberRepo.save(m);
		return mm.memberToDao(m);
	}
	
	public List<MemberDao> map(Collection<Member> members) { 
		return members.stream().map(m -> memberRepo.fetchBy(m.getId()))
				.map(m -> mm.memberToDao(m)).collect(Collectors.toList());
	}
}
