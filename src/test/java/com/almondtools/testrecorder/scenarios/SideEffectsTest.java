package com.almondtools.testrecorder.scenarios;

import static com.almondtools.testrecorder.SnapshotGenerator.setSnapshotConsumer;
import static com.almondtools.testrecorder.dynamiccompile.CompilableMatcher.compiles;
import static com.almondtools.testrecorder.dynamiccompile.TestsRunnableMatcher.testsRuns;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almondtools.testrecorder.SnapshotInstrumentor;
import com.almondtools.testrecorder.generator.TestGenerator;

public class SideEffectsTest {

	private static SnapshotInstrumentor instrumentor;

	private TestGenerator testGenerator;

	@BeforeClass
	public static void beforeClass() throws Exception {
		instrumentor = new SnapshotInstrumentor();
		instrumentor.register("com.almondtools.testrecorder.scenarios.SideEffects");
	}

	@Before
	public void before() throws Exception {
		testGenerator = new TestGenerator();
		setSnapshotConsumer(testGenerator);
	}

	@Test
	public void testSideEffectsOnThis() throws Exception {
		SideEffects sideEffects = new SideEffects();
		for (int i = 0; i < 100; i += sideEffects.getI()) {
			sideEffects.methodWithSideEffectOnThis(i);
		}
		assertThat(testGenerator.getTests(SideEffects.class), hasSize(7));
		assertThat(testGenerator.getTests(SideEffects.class), everyItem(containsString("assert")));
	}

	@Test
	public void testSideEffectsOnArgument() throws Exception {
		int[] array = new int[] { 0 };
		SideEffects sideEffects = new SideEffects();
		for (int i = 0; i < 10; i++) {
			sideEffects.methodWithSideEffectOnArgument(array);
		}
		assertThat(testGenerator.getTests(SideEffects.class), hasSize(10));
		assertThat(testGenerator.getTests(SideEffects.class), everyItem(containsString("assert")));
	}

	@Test
	public void testSideEffectsCompilable() throws Exception {
		int[] array = new int[] { 0 };
		SideEffects sideEffects = new SideEffects();
		for (int i = 0; i < 100; i += sideEffects.getI()) {
			sideEffects.methodWithSideEffectOnThis(i);
		}
		for (int i = 0; i < 10; i++) {
			sideEffects.methodWithSideEffectOnArgument(array);
		}
		assertThat(testGenerator.renderTest(SideEffects.class), compiles());
		assertThat(testGenerator.renderTest(SideEffects.class), testsRuns());
	}

}