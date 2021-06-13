package at.tfr.pfad.model;
// Generated Feb 2, 2021, 9:27:45 PM by Hibernate Tools 5.2.11.Final


import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * MemberMemberAudId generated by hbm2java
 */
@Embeddable
public class MemberMemberAudId  implements java.io.Serializable {


     private int rev;
     private long memberId;
     private long siblingsId;

    public MemberMemberAudId() {
    }

    public MemberMemberAudId(int rev, long memberId, long siblingsId) {
       this.rev = rev;
       this.memberId = memberId;
       this.siblingsId = siblingsId;
    }
   


    @Column(name="REV", nullable=false)
    public int getRev() {
        return this.rev;
    }
    
    public void setRev(int rev) {
        this.rev = rev;
    }


    @Column(name="MEMBER_ID", nullable=false)
    public long getMemberId() {
        return this.memberId;
    }
    
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }


    @Column(name="SIBLINGS_ID", nullable=false)
    public long getSiblingsId() {
        return this.siblingsId;
    }
    
    public void setSiblingsId(long siblingsId) {
        this.siblingsId = siblingsId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof MemberMemberAudId) ) return false;
		 MemberMemberAudId castOther = ( MemberMemberAudId ) other; 
         
		 return (this.getRev()==castOther.getRev())
 && (this.getMemberId()==castOther.getMemberId())
 && (this.getSiblingsId()==castOther.getSiblingsId());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + this.getRev();
         result = 37 * result + (int) this.getMemberId();
         result = 37 * result + (int) this.getSiblingsId();
         return result;
   }   


}

