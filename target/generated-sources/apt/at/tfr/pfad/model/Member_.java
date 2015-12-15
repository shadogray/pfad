package at.tfr.pfad.model;

import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Member.class)
public abstract class Member_ {

	public static volatile SingularAttribute<Member, Squad> Trupp;
	public static volatile SingularAttribute<Member, String> Email;
	public static volatile SingularAttribute<Member, String> Name;
	public static volatile SingularAttribute<Member, Integer> GebMonat;
	public static volatile SingularAttribute<Member, String> Ort;
	public static volatile SingularAttribute<Member, Boolean> Aktiv;
	public static volatile SingularAttribute<Member, Long> id;
	public static volatile SingularAttribute<Member, Sex> Geschlecht;
	public static volatile SingularAttribute<Member, String> Strasse;
	public static volatile SingularAttribute<Member, Member> Vollzahler;
	public static volatile SingularAttribute<Member, String> PLZ;
	public static volatile SingularAttribute<Member, ScoutRole> Rolle;
	public static volatile SingularAttribute<Member, String> BVKey;
	public static volatile SingularAttribute<Member, String> GruppenSchluessel;
	public static volatile SingularAttribute<Member, String> Religion;
	public static volatile SetAttribute<Member, Function> Funktionen;
	public static volatile SingularAttribute<Member, Integer> version;
	public static volatile SingularAttribute<Member, String> Anrede;
	public static volatile SingularAttribute<Member, Boolean> AltER;
	public static volatile SingularAttribute<Member, Boolean> Gilde;
	public static volatile SingularAttribute<Member, Integer> GebTag;
	public static volatile SingularAttribute<Member, Boolean> Trail;
	public static volatile SingularAttribute<Member, String> Titel;
	public static volatile SingularAttribute<Member, Integer> GebJahr;
	public static volatile SingularAttribute<Member, Long> PersonenKey;
	public static volatile SingularAttribute<Member, String> Telefon;
	public static volatile SingularAttribute<Member, String> Vorname;

}

