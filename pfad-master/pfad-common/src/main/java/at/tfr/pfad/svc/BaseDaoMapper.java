package at.tfr.pfad.svc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.mapstruct.TargetType;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.PrimaryKeyHolder;
import at.tfr.pfad.model.Squad;

@ApplicationScoped
public class BaseDaoMapper {

	static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
	@Inject
	private EntityManager em;
	
    public <T extends PrimaryKeyHolder> T resolve(BaseDao reference, @TargetType Class<T> entityClass) {
        return reference != null ? em.find( entityClass, reference.getId() ) : null;
    }

    public Set<BaseDao> toReferences(Set<? extends PrimaryKeyHolder> entities) {
    	if (entities == null) {
    		return new TreeSet<>();
    	}
    	return entities.stream().map(e -> toReference(e)).sorted().collect(Collectors.toCollection(TreeSet::new));
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
        if (entity instanceof Function) {
        	bd.setName(((Function)entity).getName());
        	bd.setShortName(bd.getName());
        }
        return bd;
    }
    
    public Date memberGeburtstag(Member m) {
    	Calendar cal = new GregorianCalendar();
    	cal.set(Calendar.YEAR, m.getGebJahr() > 1900 ? m.getGebJahr() : 2000);
    	cal.set(Calendar.MONTH, m.getGebMonat() > 0 ? m.getGebMonat()+1 : 1);  // ATTENTION: Pfad starts with "1"!!
    	cal.set(Calendar.DAY_OF_MONTH, m.getGebTag() > 0 ? m.getGebTag() : 1);
    	return cal.getTime();
    }
    
    public int memberGebJahr(Date geburtstag) {
		if (geburtstag != null) {
			Calendar cal = getDate(geburtstag);
			return cal.get(Calendar.YEAR);
		}
		return 2000;
    }

    public int memberGebMonat(Date geburtstag) {
		if (geburtstag != null) {
			Calendar cal = getDate(geburtstag);
			return cal.get(Calendar.MONTH)+1; // ATTENTION: Pfad starts with "1"!!
		}
		return 1;
    }

    public int memberGebTag(Date geburtstag) {
		if (geburtstag != null) {
			Calendar cal = getDate(geburtstag);
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		return 1;
    }

    private Calendar getDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal;
    }
    
	private Calendar parseDate(String geburtstag) {
		Calendar cal = new GregorianCalendar();
		try {
			cal.setTime(sdf.parse(geburtstag));
			return cal;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		return cal;
	}

}
