package net.amygdalum.testrecorder.scenarios;

import net.amygdalum.testrecorder.Snapshot;

public class SubBean extends SuperBean {

	private Object o;
	
	public SubBean() {
	}
	
	public void setO(Object o) {
		this.o = o;
	}
	
	@Snapshot
	@Override
	public int hashCode() {
		int j = o == null ? 1 : o.hashCode();
		return getI() + 13 * j;
	}

}