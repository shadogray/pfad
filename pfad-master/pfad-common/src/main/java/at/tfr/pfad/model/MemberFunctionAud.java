package at.tfr.pfad.model;
// Generated Feb 2, 2021, 9:27:45 PM by Hibernate Tools 5.2.11.Final


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * MemberFunctionAud generated by hbm2java
 */
@Entity
@Table(name="MEMBER_FUNCTION_AUD")
public class MemberFunctionAud  implements java.io.Serializable {


     private MemberFunctionAudId id;
     private Revinfo revinfo;
     private Byte revtype;

    public MemberFunctionAud() {
    }

	
    public MemberFunctionAud(MemberFunctionAudId id, Revinfo revinfo) {
        this.id = id;
        this.revinfo = revinfo;
    }
    public MemberFunctionAud(MemberFunctionAudId id, Revinfo revinfo, Byte revtype) {
       this.id = id;
       this.revinfo = revinfo;
       this.revtype = revtype;
    }
   
     @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="rev", column=@Column(name="REV", nullable=false) ), 
        @AttributeOverride(name="memberId", column=@Column(name="MEMBER_ID", nullable=false) ), 
        @AttributeOverride(name="funktionenId", column=@Column(name="FUNKTIONEN_ID", nullable=false) ) } )
    public MemberFunctionAudId getId() {
        return this.id;
    }
    
    public void setId(MemberFunctionAudId id) {
        this.id = id;
    }

@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="REV", nullable=false, insertable=false, updatable=false)
    public Revinfo getRevinfo() {
        return this.revinfo;
    }
    
    public void setRevinfo(Revinfo revinfo) {
        this.revinfo = revinfo;
    }

    
    @Column(name="REVTYPE")
    public Byte getRevtype() {
        return this.revtype;
    }
    
    public void setRevtype(Byte revtype) {
        this.revtype = revtype;
    }




}


