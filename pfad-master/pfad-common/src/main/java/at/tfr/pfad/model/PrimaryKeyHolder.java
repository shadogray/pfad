package at.tfr.pfad.model;

public interface PrimaryKeyHolder {
	Long getId();
	
	public default boolean checkIds(Long id, PrimaryKeyHolder other) {
		if (id == other.getId()) {
			return true;
		}
		if (id != null) {
			return id.equals(other.getId());
		}
		return false;
	}

}
