package net.amygdalum.testrecorder.values;

import static net.amygdalum.testrecorder.deserializers.TypeManager.getBase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.amygdalum.testrecorder.SerializedReferenceType;
import net.amygdalum.testrecorder.SerializedValue;
import net.amygdalum.testrecorder.deserializers.ValuePrinter;
import net.amygdalum.testrecorder.Deserializer;

public class SerializedMap implements SerializedReferenceType, Map<SerializedValue, SerializedValue> {

	private Type type;
	private Class<?> valueType;
	private Map<SerializedValue, SerializedValue> map;

	public SerializedMap(Type type, Class<?> valueType) {
		this.type = type;
		this.valueType = valueType;
		this.map = new LinkedHashMap<>();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Class<?> getValueType() {
		return getBase(valueType);
	}

	public Type getMapKeyType() {
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments()[0];
		} else {
			return Object.class;
		}
	}

	public Type getMapValueType() {
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments()[1];
		} else {
			return Object.class;
		}
	}

	@Override
	public <T> T accept(Deserializer<T> visitor) {
		return visitor.visitReferenceType(this);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public SerializedValue get(Object key) {
		return map.get(key);
	}

	public SerializedValue put(SerializedValue key, SerializedValue value) {
		return map.put(key, value);
	}

	public SerializedValue remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends SerializedValue, ? extends SerializedValue> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<SerializedValue> keySet() {
		return map.keySet();
	}

	public Collection<SerializedValue> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<SerializedValue, SerializedValue>> entrySet() {
		return map.entrySet();
	}

	@Override
	public String toString() {
		return accept(new ValuePrinter());
	}

}
