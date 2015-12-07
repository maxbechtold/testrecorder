package com.almondtools.testrecorder;

import static com.almondtools.testrecorder.ConfigRegistry.loadConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.almondtools.testrecorder.values.SerializedField;

public class SnapshotGenerator {

	private static ThreadFactory THREADS = new ThreadFactory() {

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		}

	};
	
	private static ThreadLocal<SnapshotGenerator> currentGenerator = new ThreadLocal<>();

	private ThreadLocal<SnapshotGenerator> stored = new ThreadLocal<>();
	
	private Object self;

	private ExecutorService executor;
	private Map<String, ContextSnapshotFactory> methodSnapshots;
	private ThreadLocal<ContextSnapshot> current = new ThreadLocal<>();
	private ThreadLocal<SerializerFacade> currentFacade = new ThreadLocal<>();
	private ThreadLocal<List<Field>> currentGlobals = new ThreadLocal<>();

	private SnapshotConsumer snapshotConsumer;
	private long timeoutInMillis;

	public SnapshotGenerator(Object self, Class<? extends SnapshotConfig> config) {
		this.snapshotConsumer = loadConfig(config).getSnapshotConsumer();
		this.timeoutInMillis = loadConfig(config).getTimeoutInMillis();

		this.self = self;

		this.executor = Executors.newSingleThreadExecutor(THREADS);
		this.methodSnapshots = new HashMap<>();
	}
	
	public static SnapshotGenerator getCurrentGenerator() {
		return currentGenerator.get();
	}

	public SnapshotConsumer getMethodConsumer() {
		return snapshotConsumer;
	}

	public void register(String signature, Method method) {
		ContextSnapshotFactory factory = new ContextSnapshotFactory(method.getDeclaringClass(), method.getAnnotation(Snapshot.class), method.getGenericReturnType(), method.getName(),
			method.getGenericParameterTypes());
		methodSnapshots.put(signature, factory);
	}

	public ContextSnapshot newSnapshot(ContextSnapshotFactory factory) {
		ContextSnapshot snapshot = factory.createSnapshot();
		current.set(snapshot);
		return snapshot;
	}

	public ContextSnapshot fetchSnapshot() {
		return current.get();
	}

	public SerializerFacade facade(ContextSnapshotFactory factory) {
		ConfigurableSerializerFacade facade = new ConfigurableSerializerFacade(factory.profile());
		currentFacade.set(facade);
		return facade;
	}

	public SerializerFacade facade() {
		SerializerFacade serializerFacade = currentFacade.get();
		serializerFacade.reset();
		return serializerFacade;
	}

	public List<Field> globals(ContextSnapshotFactory factory) {
		List<Field> globals = factory.getGlobalFields();
		currentGlobals.set(globals);
		return globals;
	}

	public List<Field> globals() {
		return currentGlobals.get();
	}

	public void setupVariables(String signature, Object... args) {
		init();
		ContextSnapshotFactory factory = methodSnapshots.get(signature);
		SerializerFacade facade = facade(factory);
		List<Field> globals = globals(factory);
		modify(newSnapshot(factory), snapshot -> {
			snapshot.setSetupThis(facade.serialize(self.getClass(), self));
			snapshot.setSetupArgs(facade.serialize(snapshot.getArgumentTypes(), args));
			snapshot.setSetupGlobals(globals.stream()
				.map(field -> facade.serialize(field, null))
				.toArray(SerializedField[]::new));
		});
	}

	public void init() {
		stored.set(currentGenerator.get());
		currentGenerator.set(this);
	}

	public void outputVariables(String method, Object... args) {
		//TODO make output available to snapshot
	}

	public void expectVariables(Object result, Object... args) {
		SerializerFacade facade = facade();
		ContextSnapshot currentSnapshot = fetchSnapshot();
		List<Field> globals = globals();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectResult(facade.serialize(snapshot.getResultType(), result));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
			snapshot.setExpectGlobals(globals.stream()
				.map(field -> facade.serialize(field, null))
				.toArray(SerializedField[]::new));
		});
		done();
		consume(currentSnapshot);
	}

	public void done() {
		currentGenerator.set(stored.get());
		stored.remove();
	}

	public void expectVariables(Object... args) {
		SerializerFacade facade = facade();
		ContextSnapshot currentSnapshot = fetchSnapshot();
		List<Field> globals = globals();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
			snapshot.setExpectGlobals(globals.stream()
				.map(field -> facade.serialize(field, null))
				.toArray(SerializedField[]::new));
		});
		consume(currentSnapshot);
	}

	public void throwVariables(Throwable throwable, Object... args) {
		SerializerFacade facade = facade();
		ContextSnapshot currentSnapshot = fetchSnapshot();
		List<Field> globals = globals();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
			snapshot.setExpectException(facade.serialize(throwable.getClass(), throwable));
			snapshot.setExpectGlobals(globals.stream()
				.map(field -> facade.serialize(field, null))
				.toArray(SerializedField[]::new));
		});
		consume(currentSnapshot);
	}

	private void consume(ContextSnapshot snapshot) {
		if (snapshot.isValid()) {
			if (snapshotConsumer != null) {
				snapshotConsumer.accept(snapshot);
			}
		}
	}

	private void modify(ContextSnapshot snapshot, Consumer<ContextSnapshot> task) {
		try {
			Future<?> future = executor.submit(() -> {
				task.accept(snapshot);
			});
			future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
			snapshot.invalidate();
		}
	}

}
