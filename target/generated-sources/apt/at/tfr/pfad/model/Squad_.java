package at.tfr.pfad.model;

import at.tfr.pfad.SquadType;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Squad.class)
public abstract class Squad_ {

	public static volatile SetAttribute<Squad, Member> assistants;
	public static volatile SingularAttribute<Squad, Member> leaderMale;
	public static volatile SingularAttribute<Squad, String> name;
	public static volatile SingularAttribute<Squad, Long> id;
	public static volatile SingularAttribute<Squad, Member> leaderFemale;
	public static volatile SingularAttribute<Squad, SquadType> type;
	public static volatile SingularAttribute<Squad, Integer> version;

}

