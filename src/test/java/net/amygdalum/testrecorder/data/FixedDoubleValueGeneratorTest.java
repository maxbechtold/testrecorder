package net.amygdalum.testrecorder.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class FixedDoubleValueGeneratorTest {

	private TestDataGenerator generator;

	@Before
	public void before() throws Exception {
		generator = new TestDataGenerator();
	}
	
	@Test
	public void testCreate() throws Exception {
		assertThat(new FixedDoubleValueGenerator(0.01348e-7d).create(generator), is(0.01348e-7d));
		assertThat(new FixedDoubleValueGenerator(1267.434e2d).create(generator), is(1267.434e2d));
	}

}
