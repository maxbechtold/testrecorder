package net.amygdalum.testrecorder.values;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Stream;

import net.amygdalum.testrecorder.SerializedValue;
import net.amygdalum.testrecorder.deserializers.ValuePrinter;

public class SerializedOutput {

	private Class<?> clazz;
	private String name;
	private Type[] types;
	private SerializedValue[] values;

	public SerializedOutput(Class<?> clazz, String name, Type[] types, SerializedValue... values) {
		this.clazz = clazz;
		this.name = name;
		this.types = types;
		this.values = values;
	}

	public Class<?> getDeclaringClass() {
		return clazz;
	}

	public String getName() {
		return name;
	}
	
	public Type[] getTypes() {
		return types;
	}

	public SerializedValue[] getValues() {
		return values;
	}

	@Override
	public String toString() {
		ValuePrinter printer = new ValuePrinter();
		return ">> " + clazz.getTypeName() + "." + name + Stream.of(values)
			.map(value -> value.accept(printer))
			.collect(joining(", ", "(", ")"));
	}

	@Override
	public int hashCode() {
		return clazz.hashCode() * 37 
			+ name.hashCode() * 29
			+ Arrays.hashCode(types) * 11
			+ Arrays.hashCode(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SerializedOutput that = (SerializedOutput) obj;
		return this.clazz.equals(that.clazz)
			&& this.name.equals(that.name)
			&& Arrays.equals(this.types,that.types)
			&& Arrays.equals(this.values,that.values);
	}

}
