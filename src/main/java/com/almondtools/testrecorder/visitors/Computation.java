package com.almondtools.testrecorder.visitors;

import java.util.ArrayList;
import java.util.List;

public class Computation {

	public static final Computation NULL = new Computation("");
	
	private List<String> statements;
	private String value;
	private boolean stored;

	public Computation(String value) {
		this(value, false, new ArrayList<>());
	}

	public Computation(String value, boolean stored) {
		this(value, stored, new ArrayList<>());
	}

	public Computation(String value, List<String> statements) {
		this(value, false, statements);
	}
	
	public Computation(String value, boolean stored, List<String> statements) {
		this.value = value;
		this.stored = stored;
		this.statements = statements;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isStored() {
		return stored;
	}
	
	public List<String> getStatements() {
		return statements;
	}

}