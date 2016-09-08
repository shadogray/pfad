package at.tfr.pfad.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;

import at.tfr.pfad.dao.Beans;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SimpleMember;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.view.Members;

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

	@POST
	public Response create(Member entity) {
		em.persist(entity);
		return Response.created(
				UriBuilder.fromResource(MemberEndpoint.class)
						.path(String.valueOf(entity.getId())).build()).build();
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
		Member entity = memberRepo.fetchBy(id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(Beans.copyProperties(entity, new Member())).build();
	}

	@GET
	public List<SimpleMember> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		return memberRepo.findAll(startPosition != null ? startPosition : 0, 
				maxResult != null ? maxResult : Integer.MAX_VALUE)
				.stream().map(m -> Beans.copyProperties(m, new SimpleMember())).collect(Collectors.toList());
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Response update(@PathParam("id") Long id, Member entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(entity.getId())) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (em.find(Member.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			entity = em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}
	
	@GET
	@Path("/filtered")
	public List<SimpleMember> filtered(@QueryParam("filter") String filter) {
		return members.filtered(filter)
				.stream().map(m-> Beans.copyProperties(m, new SimpleMember()))
				.collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/query")
	public List<SimpleMember> queryByExample(Member example) {
		return memberRepo.findBy(example, 0, 10, Member_.name, Member_.vorname, 
				Member_.geschlecht, Member_.email, 
				Member_.aktiv, Member_.aktivExtern, 
				Member_.bvKey, Member_.trupp, 
				Member_.gebJahr, Member_.gebMonat, Member_.gebTag, 
				Member_.strasse, Member_.ort, Member_.plz, 
				Member_.rolle, 
				Member_.free, Member_.support, Member_.trail, Member_.gilde)
				.stream().map(m-> Beans.copyProperties(m, new SimpleMember()))
				.collect(Collectors.toList());
	}

	@GET
	@Path("/siblings/{id:[0-9][0-9]*}")
	public List<SimpleMember> findSiblingsById(@PathParam("id") Long id) {
		Member member = memberRepo.fetchBy(id);
		return member.getSiblings().stream().map(m -> Beans.copyProperties(m, new SimpleMember()))
				.collect(Collectors.toList());
	}

	@GET
	@Path("/parents/{id:[0-9][0-9]*}")
	public List<SimpleMember> findParentsById(@PathParam("id") Long id) {
		Member member = memberRepo.fetchBy(id);
		return member.getParents().stream().map(m -> Beans.copyProperties(m, new SimpleMember()))
				.collect(Collectors.toList());
	}
}
