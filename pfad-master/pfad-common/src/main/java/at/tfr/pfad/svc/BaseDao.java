package at.tfr.pfad.svc;

import java.io.Serializable;

import at.tfr.pfad.model.PrimaryKeyHolder;

public class BaseDao implements PrimaryKeyHolder, Serializable {

	protected Long id;
	protected String shortName;
	protected String longName;
	
	public Long getId() {
		return id;
	};
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}
}
