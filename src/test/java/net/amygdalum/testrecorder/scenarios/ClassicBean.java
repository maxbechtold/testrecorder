package net.amygdalum.testrecorder.scenarios;

import net.amygdalum.testrecorder.Snapshot;

public class ClassicBean {

	private int i;
	private Object o;
	
	public ClassicBean() {
	}
	
	public void setI(int i) {
		this.i = i;
	}
	
	public void setO(Object o) {
		this.o = o;
	}
	
	@Snapshot
	@Override
	public int hashCode() {
		int j = o == null ? 1 : o.hashCode();
		return i + 13 * j;
	}

}