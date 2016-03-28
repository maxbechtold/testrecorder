package net.amygdalum.testrecorder.deserializers.matcher;

import static java.util.Collections.emptyList;
import static net.amygdalum.testrecorder.deserializers.Templates.asLiteral;
import static net.amygdalum.testrecorder.deserializers.Templates.equalToMatcher;
import static net.amygdalum.testrecorder.deserializers.TypeManager.parameterized;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import net.amygdalum.testrecorder.deserializers.Adaptor;
import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.deserializers.DefaultAdaptor;
import net.amygdalum.testrecorder.deserializers.TypeManager;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class DefaultLiteralAdaptor extends DefaultAdaptor<SerializedLiteral, ObjectToMatcherCode> implements Adaptor<SerializedLiteral, ObjectToMatcherCode> {

	@Override
	public Computation tryDeserialize(SerializedLiteral value, TypeManager types, ObjectToMatcherCode generator) {
		types.staticImport(Matchers.class, "equalTo");

		String valueExpression = asLiteral(value.getValue());

		String equalToMatcher = equalToMatcher(valueExpression);
		return new Computation(equalToMatcher, parameterized(Matcher.class, null, value.getValueType()), emptyList());
	}

}