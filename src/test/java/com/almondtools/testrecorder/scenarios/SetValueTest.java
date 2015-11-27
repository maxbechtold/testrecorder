package com.almondtools.testrecorder.scenarios;

import static com.almondtools.testrecorder.TypeHelper.parameterized;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.almondtools.testrecorder.CodeSerializer;

public class SetValueTest {

	@Test
	public void testHashSet() throws Exception {
		CodeSerializer codeSerializer = new CodeSerializer();

		Set<String> m = new HashSet<String>();
		m.add("foo");
		m.add("bar");

		assertThat(codeSerializer.serialize(m), allOf(
			containsString("HashSet set1 = new HashSet<>();"),
			containsString("set1.add(\"foo\");"),
			containsString("set1.add(\"bar\");")));
	}

	@Test
	public void testResultType() throws Exception {
		CodeSerializer codeSerializer = new CodeSerializer();
		
		Set<String> m = new HashSet<String>();
		m.add("foo");
		m.add("bar");
		
		assertThat(codeSerializer.serialize(parameterized(Set.class, null, String.class), m), allOf(
			containsString("Set<String> set1 = new HashSet<>();"),
			containsString("set1.add(\"foo\");"),
			containsString("set1.add(\"bar\");")));
	}
	
}