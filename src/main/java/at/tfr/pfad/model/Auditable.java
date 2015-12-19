package at.tfr.pfad.model;

import java.util.Date;

public interface Auditable {

	Long getId();
	
	Date getChanged();

	void setChanged(Date changed);

	Date getCreated();

	void setCreated(Date created);

	String getChangedBy();

	void setChangedBy(String changedBy);

	String getCreatedBy();

	void setCreatedBy(String createdBy);

}