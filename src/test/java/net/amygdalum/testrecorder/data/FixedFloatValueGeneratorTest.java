package net.amygdalum.testrecorder.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class FixedFloatValueGeneratorTest {

	private TestDataGenerator generator;

	@Before
	public void before() throws Exception {
		generator = new TestDataGenerator();
	}
	
	@Test
	public void testCreate() throws Exception {
		assertThat(new FixedFloatValueGenerator(0.134e-5f).create(generator), is(0.134e-5f));
		assertThat(new FixedFloatValueGenerator(12.434e2f).create(generator), is(12.434e2f));
	}

}
