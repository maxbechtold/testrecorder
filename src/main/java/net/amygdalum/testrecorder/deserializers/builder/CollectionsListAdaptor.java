package net.amygdalum.testrecorder.deserializers.builder;

import static java.util.Arrays.asList;
import static net.amygdalum.testrecorder.TypeSelector.innerClasses;
import static net.amygdalum.testrecorder.TypeSelector.startingWith;
import static net.amygdalum.testrecorder.deserializers.Templates.assignLocalVariableStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.callLocalMethod;
import static net.amygdalum.testrecorder.util.Types.equalTypes;
import static net.amygdalum.testrecorder.util.Types.parameterized;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.amygdalum.testrecorder.DeserializationException;
import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.deserializers.TypeManager;
import net.amygdalum.testrecorder.values.SerializedList;

public class CollectionsListAdaptor implements SetupGenerator<SerializedList> {

	private DefaultListAdaptor adaptor;

	public CollectionsListAdaptor() {
		this.adaptor = new DefaultListAdaptor();
	}

	@Override
	public Class<SerializedList> getAdaptedClass() {
		return SerializedList.class;
	}

	@Override
	public Class<? extends SetupGenerator<SerializedList>> parent() {
		return DefaultListAdaptor.class;
	}

	@Override
	public boolean matches(Type type) {
		return innerClasses(Collections.class)
			.filter(startingWith("Unmodifiable", "Synchronized", "Checked", "Empty", "Singleton"))
			.filter(element -> List.class.isAssignableFrom(element))
			.anyMatch(element -> equalTypes(element, type));
	}

	@Override
	public Computation tryDeserialize(SerializedList value, SetupGenerators generator) {
		TypeManager types = generator.getTypes();
		types.registerImport(List.class);

		String name = types.getSimpleName(value.getType());
		if (name.contains("Empty")) {
			return tryDeserializeEmpty(value, generator);
		} else if (name.contains("Singleton")) {
			return tryDeserializeSingleton(value, generator);
		} else if (name.contains("Unmodifiable")) {
			return tryDeserializeUnmodifiable(value, generator);
		} else if (name.contains("Synchronized")) {
			return tryDeserializeSynchronized(value, generator);
		} else if (name.contains("Checked")) {
			return tryDeserializeChecked(value, generator);
		} else {
			throw new DeserializationException(value.toString());
		}
	}

	private Computation createOrdinaryList(SerializedList value, SetupGenerators generator) {
		SerializedList baseValue = new SerializedList(parameterized(ArrayList.class, null, value.getComponentType()));
		baseValue.addAll(value);
		return adaptor.tryDeserialize(baseValue, generator);
	}

	private Computation tryDeserializeEmpty(SerializedList value, SetupGenerators generator) {
		String factoryMethod = "emptyList";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(List.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod));

			return new Computation(local.getName(), value.getResultType(), asList(decoratingStatement));
		});
	}

	private Computation tryDeserializeSingleton(SerializedList value, SetupGenerators generator) {
		String factoryMethod = "singletonList";
		TypeManager types = generator.getTypes();
		types.registerImport(List.class);
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(List.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = value.get(0).accept(generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

	private Computation tryDeserializeUnmodifiable(SerializedList value, SetupGenerators generator) {
		String factoryMethod = "unmodifiableList";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(List.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinaryList(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

	private Computation tryDeserializeSynchronized(SerializedList value, SetupGenerators generator) {
		String factoryMethod = "synchronizedList";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(List.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinaryList(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});

	}

	private Computation tryDeserializeChecked(SerializedList value, SetupGenerators generator) {
		String factoryMethod = "checkedList";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(List.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinaryList(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();
			String checkedType = types.getRawTypeName(value.getComponentType());

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase, checkedType));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

}
