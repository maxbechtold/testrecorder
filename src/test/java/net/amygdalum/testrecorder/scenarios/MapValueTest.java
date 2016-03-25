package net.amygdalum.testrecorder.scenarios;

import static com.almondtools.conmatch.strings.WildcardStringMatcher.containsPattern;
import static net.amygdalum.testrecorder.deserializers.TypeManager.parameterized;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.amygdalum.testrecorder.CodeSerializer;

public class MapValueTest {

	@Test
	public void testHashMap() throws Exception {
		CodeSerializer codeSerializer = new CodeSerializer();

		Map<String, Integer> m = new HashMap<String, Integer>();
		m.put("bar", new Integer(21));

		assertThat(codeSerializer.serialize(m), containsPattern(""
			+ "HashMap map1 = new HashMap<>();*"
			+ "map1.put(\"bar\", 21);"));
	}

	@Test
	public void testResultType() throws Exception {
		CodeSerializer codeSerializer = new CodeSerializer();

		Map<String, Integer> m = new HashMap<String, Integer>();
		m.put("bar", new Integer(21));

		assertThat(codeSerializer.serialize(parameterized(Map.class, null, String.class, Integer.class), m), containsPattern(""
			+ "Map<String, Integer> map1 = new HashMap<>();*"
			+ "map1.put(\"bar\", 21);"));
	}

}
