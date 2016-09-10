package at.tfr.pfad.svc;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.mapstruct.TargetType;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.PrimaryKeyHolder;
import at.tfr.pfad.model.Squad;

@ApplicationScoped
public class BaseDaoMapper {

	@Inject
	private EntityManager em;
	
    public <T extends PrimaryKeyHolder> T resolve(BaseDao reference, @TargetType Class<T> entityClass) {
        return reference != null ? em.find( entityClass, reference.getId() ) : null;
    }

    public Set<BaseDao> toReferences(Set<? extends PrimaryKeyHolder> entities) {
    	if (entities == null) {
    		return new TreeSet<>();
    	}
    	return entities.stream().map(e -> toReference(e)).collect(Collectors.toSet());
    }
    
    public BaseDao toReference(PrimaryKeyHolder entity) {
        if (entity == null)
        	return null;
        BaseDao bd = new BaseDao();
        bd.setId(entity.getId());
        bd.setLongName(entity.toString());
        if (entity instanceof Member) {
        	bd.setName(((Member)entity).getName());
        	bd.setShortName(((Member)entity).toShortString());
        }
        if (entity instanceof Payment) {
        	bd.setName(((Payment)entity).toString());
        	bd.setShortName(bd.getName());
        }
        if (entity instanceof Booking) {
        	bd.setShortName(((Booking)entity).toString());
        	bd.setShortName(bd.getName());
        }
        if (entity instanceof Squad) {
        	bd.setName(((Squad)entity).getName());
        	bd.setShortName(bd.getName());
        }
        if (entity instanceof Activity) {
        	bd.setName(((Activity)entity).getName());
        	bd.setShortName(bd.getName());
        }
        return bd;
    }
}
