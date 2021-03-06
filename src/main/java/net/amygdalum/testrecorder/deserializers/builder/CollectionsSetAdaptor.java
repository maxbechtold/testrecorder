package net.amygdalum.testrecorder.deserializers.builder;

import static java.util.Arrays.asList;
import static net.amygdalum.testrecorder.TypeSelector.innerClasses;
import static net.amygdalum.testrecorder.TypeSelector.startingWith;
import static net.amygdalum.testrecorder.deserializers.Templates.assignLocalVariableStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.callLocalMethod;
import static net.amygdalum.testrecorder.util.Types.equalTypes;
import static net.amygdalum.testrecorder.util.Types.parameterized;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.amygdalum.testrecorder.DeserializationException;
import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.deserializers.TypeManager;
import net.amygdalum.testrecorder.values.SerializedSet;

public class CollectionsSetAdaptor implements SetupGenerator<SerializedSet> {

	private DefaultSetAdaptor adaptor;

	public CollectionsSetAdaptor() {
		this.adaptor = new DefaultSetAdaptor();
	}

	@Override
	public Class<SerializedSet> getAdaptedClass() {
		return SerializedSet.class;
	}

	@Override
	public Class<? extends SetupGenerator<SerializedSet>> parent() {
		return DefaultSetAdaptor.class;
	}

	@Override
	public boolean matches(Type type) {
		return innerClasses(Collections.class)
			.filter(startingWith("Unmodifiable", "Synchronized", "Checked", "Empty", "Singleton"))
			.filter(element -> Set.class.isAssignableFrom(element))
			.anyMatch(element -> equalTypes(element, type));
	}

	@Override
	public Computation tryDeserialize(SerializedSet value, SetupGenerators generator) {
		TypeManager types = generator.getTypes();
		types.registerImport(Set.class);

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

	private Computation createOrdinarySet(SerializedSet value, SetupGenerators generator) {
		SerializedSet baseValue = new SerializedSet(parameterized(LinkedHashSet.class, null, value.getComponentType()));
		baseValue.addAll(value);
		return adaptor.tryDeserialize(baseValue, generator);
	}

	private Computation tryDeserializeEmpty(SerializedSet value, SetupGenerators generator) {
		String factoryMethod = "emptySet";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(Set.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod));

			return new Computation(local.getName(), value.getResultType(), asList(decoratingStatement));
		});
	}

	private Computation tryDeserializeSingleton(SerializedSet value, SetupGenerators generator) {
		String factoryMethod = "singleton";
		TypeManager types = generator.getTypes();
		types.registerImport(Set.class);
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(Set.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = value.iterator().next().accept(generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

	private Computation tryDeserializeUnmodifiable(SerializedSet value, SetupGenerators generator) {
		String factoryMethod = "unmodifiableSet";

		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(Set.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinarySet(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

	private Computation tryDeserializeSynchronized(SerializedSet value, SetupGenerators generator) {
		String factoryMethod = "synchronizedSet";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(Set.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinarySet(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

	private Computation tryDeserializeChecked(SerializedSet value, SetupGenerators generator) {
		String factoryMethod = "checkedSet";
		TypeManager types = generator.getTypes();
		types.staticImport(Collections.class, factoryMethod);

		Type resultType = parameterized(Set.class, null, value.getComponentType());
		return generator.forVariable(value, resultType, local -> {

			Computation computation = createOrdinarySet(value, generator);
			List<String> statements = new LinkedList<>(computation.getStatements());
			String resultBase = computation.getValue();
			String checkedType = types.getRawTypeName(value.getComponentType());

			String decoratingStatement = assignLocalVariableStatement(types.getBestName(resultType), local.getName(), callLocalMethod(factoryMethod, resultBase, checkedType));
			statements.add(decoratingStatement);

			return new Computation(local.getName(), value.getResultType(), statements);
		});
	}

}
