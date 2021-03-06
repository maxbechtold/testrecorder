package net.amygdalum.testrecorder.deserializers.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.values.SerializedEnum;

public class DefaultEnumAdaptorTest {

	private DefaultEnumAdaptor adaptor;

	@Before
	public void before() throws Exception {
		adaptor = new DefaultEnumAdaptor();
	}

	@Test
	public void testParentNull() throws Exception {
		assertThat(adaptor.parent(), nullValue());
	}

	@Test
	public void testMatchesOnlyEnum() throws Exception {
		assertThat(adaptor.matches(MyEnum.class), is(true));
		assertThat(adaptor.matches(MyHiddenEnum.class), is(true));
		assertThat(adaptor.matches(Enum.class), is(false));
		assertThat(adaptor.matches(Object.class), is(false));
	}

	@Test
	public void testTryDeserialize() throws Exception {
		SerializedEnum value = new SerializedEnum(MyEnum.class);
		value.setName("VALUE1");
		SetupGenerators generator = new SetupGenerators(getClass());

		Computation result = adaptor.tryDeserialize(value, generator);

		assertThat(result.getStatements(), empty());
		assertThat(result.getValue(), equalTo("MyEnum.VALUE1"));
	}

	@Test
	public void testTryDeserializeHidden() throws Exception {
		SerializedEnum value = new SerializedEnum(MyHiddenEnum.class);
		value.setName("VALUE2");
		SetupGenerators generator = new SetupGenerators(getClass());
		
		Computation result = adaptor.tryDeserialize(value, generator);
		
		assertThat(result.getStatements(), empty());
		assertThat(result.getValue(), equalTo("Wrapped.enumType(\"net.amygdalum.testrecorder.deserializers.builder.DefaultEnumAdaptorTest$MyHiddenEnum\", \"VALUE2\").value()"));
	}
	
	public static enum MyEnum {
		VALUE1, VALUE2;
	}

	private static enum MyHiddenEnum {
		VALUE1, VALUE2;
	}

}
