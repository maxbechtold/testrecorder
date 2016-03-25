package net.amygdalum.testrecorder.deserializers.matcher;

import static java.util.Collections.emptyList;
import static net.amygdalum.testrecorder.deserializers.Templates.asLiteral;
import static net.amygdalum.testrecorder.deserializers.Templates.equalToMatcher;
import static net.amygdalum.testrecorder.deserializers.Templates.newObject;
import static net.amygdalum.testrecorder.deserializers.TypeManager.parameterized;

import java.math.BigDecimal;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import net.amygdalum.testrecorder.deserializers.Adaptor;
import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.deserializers.DefaultAdaptor;
import net.amygdalum.testrecorder.deserializers.TypeManager;
import net.amygdalum.testrecorder.values.SerializedImmutable;


public class DefaultBigDecimalAdaptor extends DefaultAdaptor<SerializedImmutable<BigDecimal>, ObjectToMatcherCode> implements Adaptor<SerializedImmutable<BigDecimal>, ObjectToMatcherCode> {

	@Override
	public boolean matches(Class<?> clazz) {
		return clazz.equals(BigDecimal.class);
	}
	
	@Override
	public Computation tryDeserialize(SerializedImmutable<BigDecimal> value, TypeManager types, ObjectToMatcherCode generator) {
		types.registerImport(BigDecimal.class);
		types.staticImport(Matchers.class, "equalTo");

		String literal = asLiteral(value.getValue().toPlainString());

		String bigDecimalLiteral = newObject("BigDecimal", literal);

		String equalToMatcher = equalToMatcher(bigDecimalLiteral);
		return new Computation(equalToMatcher, parameterized(Matcher.class, null, value.getValueType()), emptyList());
	}

}
