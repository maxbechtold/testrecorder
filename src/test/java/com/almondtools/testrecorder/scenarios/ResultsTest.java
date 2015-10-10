package com.almondtools.testrecorder.scenarios;

import static com.almondtools.testrecorder.SnapshotGenerator.setSnapshotConsumer;
import static com.almondtools.testrecorder.dynamiccompile.CompilableMatcher.compiles;
import static com.almondtools.testrecorder.dynamiccompile.TestsRunnableMatcher.testsRuns;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almondtools.testrecorder.SnapshotInstrumentor;
import com.almondtools.testrecorder.generator.TestGenerator;

public class ResultsTest {

	private static SnapshotInstrumentor instrumentor;

	private TestGenerator testGenerator;

	@BeforeClass
	public static void beforeClass() throws Exception {
		instrumentor = new SnapshotInstrumentor();
		instrumentor.register("com.almondtools.testrecorder.scenarios.Results");
	}
	
	@Before
	public void before() throws Exception {
		testGenerator = new TestGenerator();
		setSnapshotConsumer(testGenerator);
	}

	@Test
	public void testNumberOfGeneratedTests() throws Exception {
		List<Double> results = new ArrayList<>();
		Results pow = new Results();
		for (int i = 1; i <= 10; i++) {
			results.add(pow.pow(i));
		}
		assertThat(results, hasSize(10));
		assertThat(testGenerator.getTests(Results.class), hasSize(10));
	}

	@Test
	public void testAssertsInEachTest() throws Exception {
		Results pow = new Results();
		for (int i = 1; i <= 10; i++) {
			pow.pow(i);
		}
		assertThat(testGenerator.getTests(Results.class), everyItem(containsString("assert")));
	}

	@Test
	public void testCompilable() throws Exception {
		Results pow = new Results();
		for (int i = 1; i <= 10; i++) {
			pow.pow(i);
		}
		assertThat(testGenerator.renderTest(Results.class), compiles());
	}
	
	@Test
	public void testRunnable() throws Exception {
		Results pow = new Results();
		for (int i = 1; i <= 10; i++) {
			pow.pow(i);
		}
		assertThat(testGenerator.renderTest(Results.class), testsRuns());
	}
	
}