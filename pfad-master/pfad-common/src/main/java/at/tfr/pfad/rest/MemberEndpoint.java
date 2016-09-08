package at.tfr.pfad.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import at.tfr.pfad.dao.Beans;
import at.tfr.pfad.dao.Members;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.svc.MemberDao;
import at.tfr.pfad.svc.MemberMapper;
import at.tfr.pfad.svc.MemberService;

/**
 * 
 */
@Stateless
@Path("/members")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MemberEndpoint extends EndpointBase<Member> {

	@Inject
	private Members members;
	@Inject
	private MemberService memberSvc;
	private MemberMapper mm = MemberMapper.INSTANCE;

	@POST
	public Response create(MemberDao dao) {
		MemberDao saved = memberSvc.save(dao);
		return Response.created(
				UriBuilder.fromResource(MemberEndpoint.class)
						.path(String.valueOf(saved.getId())).build()).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		Member entity = em.find(Member.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Response findById(@PathParam("id") Long id) {
		MemberDao dao = memberSvc.findBy(id);
		if (dao == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(dao).build();
	}

	@GET
	public List<MemberDao> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		return memberSvc.map(memberRepo.findAll(startPosition != null ? startPosition : 0, 
				maxResult != null ? maxResult : Integer.MAX_VALUE));
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Response update(@PathParam("id") Long id, MemberDao dao) {
		if (dao == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(dao.getId())) {
			return Response.status(Status.CONFLICT).entity(dao).build();
		}
		if (em.find(Member.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			dao = memberSvc.update(dao);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.ok(dao).build();
	}
	
	@GET
	@Path("/filtered")
	public List<MemberDao> filtered(@QueryParam("filter") String filter) {
		return memberSvc.map(members.filtered(filter));
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/query")
	public List<MemberDao> queryByExample(Member example) {
		return memberSvc.map(memberRepo.findBy(example, 0, 10, Member_.name, Member_.vorname, 
				Member_.geschlecht, Member_.email, 
				Member_.aktiv, Member_.aktivExtern, 
				Member_.bvKey, Member_.trupp, 
				Member_.gebJahr, Member_.gebMonat, Member_.gebTag, 
				Member_.strasse, Member_.ort, Member_.plz, 
				Member_.rolle, 
				Member_.free, Member_.support, Member_.trail, Member_.gilde));
	}

	@GET
	@Path("/siblings/{id:[0-9][0-9]*}")
	public List<MemberDao> findSiblingsById(@PathParam("id") Long id) {
		Member member = memberRepo.fetchBy(id);
		return memberSvc.map(member.getSiblings());
	}

	@GET
	@Path("/parents/{id:[0-9][0-9]*}")
	public List<MemberDao> findParentsById(@PathParam("id") Long id) {
		Member member = memberRepo.fetchBy(id);
		return memberSvc.map(member.getParents());
	}
}
