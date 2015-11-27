package com.almondtools.testrecorder.values;

import static com.almondtools.conmatch.conventions.EqualityMatcher.satisfiesDefaultEquality;
import static com.almondtools.testrecorder.values.SerializedLiteral.literal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.testrecorder.visitors.TestValueVisitor;

public class SerializedFieldTest {

	@Test
	public void testGetName() throws Exception {
		assertThat(new SerializedField("field", String.class, literal(String.class, "stringvalue")).getName(), equalTo("field"));
	}

	@Test
	public void testGetType() throws Exception {
		assertThat(new SerializedField("field", String.class, literal(String.class, "stringvalue")).getType(), equalTo(String.class));
	}

	@Test
	public void testGetValue() throws Exception {
		assertThat(new SerializedField("field", String.class, literal(String.class, "stringvalue")).getValue(), equalTo(literal(String.class, "stringvalue")));
	}

	@Test
	public void testAccept() throws Exception {
		assertThat(new SerializedField("f", String.class, literal(String.class, "sv"))
			.accept(new TestValueVisitor()), equalTo("field"));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new SerializedField("f", String.class, literal(String.class, "sv")).toString(), equalTo("java.lang.String f: sv"));
	}

	@Test
	public void testEquals() throws Exception {
		assertThat(new SerializedField("f", String.class, literal(String.class, "sv")), satisfiesDefaultEquality()
			.andEqualTo(new SerializedField("f", String.class, literal(String.class, "sv")))
			.andNotEqualTo(new SerializedField("nf", String.class, literal(String.class, "sv")))
			.andNotEqualTo(new SerializedField("f", Object.class, literal(String.class, "sv")))
			.andNotEqualTo(new SerializedField("f", String.class, literal(String.class, "nsv"))));
	}

}