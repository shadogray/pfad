package at.tfr.pfad.svc;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.mapstruct.TargetType;

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

    public BaseDao toReference(PrimaryKeyHolder entity) {
        if (entity == null)
        	return null;
        BaseDao bd = new BaseDao();
        bd.setId(entity.getId());
        bd.setLongName(entity.toString());
        if (entity instanceof Member) {
        	bd.setShortName(((Member)entity).toShortString());
        }
        if (entity instanceof Payment) {
        	bd.setShortName(((Payment)entity).toString());
        }
        if (entity instanceof Booking) {
        	bd.setShortName(((Booking)entity).toString());
        }
        if (entity instanceof Squad) {
        	bd.setShortName(((Squad)entity).getName());
        }
        return bd;
    }
}
