package com.almondtools.testrecorder;

import java.lang.reflect.Method;
import java.util.HashMap;
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

public class SnapshotGenerator {

	private static ThreadFactory THREADS = new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	
	};
	
	private Object self;

	private ExecutorService executor;
	private Map<String, GeneratedSnapshotFactory> snapshotFactories;
	private ThreadLocal<GeneratedSnapshot> current = new ThreadLocal<>();
	private ThreadLocal<SerializerFacade> currentFacade = new ThreadLocal<>();

	private Consumer<GeneratedSnapshot> consumer;
	private long timeoutInMillis;
	

	public SnapshotGenerator(Object self, Class<? extends SnapshotConfig> config) {
		SnapshotConfig snapshotConfig = loadConfig(config);
		this.consumer = snapshotConfig.getConsumer();
		this.timeoutInMillis = snapshotConfig.getTimeoutInMillis();
		
		this.self = self;

		this.executor = Executors.newSingleThreadExecutor(THREADS);
		this.snapshotFactories = new HashMap<>();
	}
	
	public Consumer<GeneratedSnapshot> getConsumer() {
		return consumer;
	}

	private static SnapshotConfig loadConfig(Class<? extends SnapshotConfig> config) {
		try {
			return config.newInstance();
		} catch (RuntimeException | ReflectiveOperationException e) {
			return new DefaultConfig();
		}
	}

	public void register(String signature, Method method) {
		GeneratedSnapshotFactory factory = new GeneratedSnapshotFactory(method.getAnnotation(Snapshot.class), method.getGenericReturnType(), method.getName(), method.getGenericParameterTypes());
		snapshotFactories.put(signature, factory);
	}

	public GeneratedSnapshot newSnapshot(String signature) {
		GeneratedSnapshot snapshot = snapshotFactories.get(signature).create();
		current.set(snapshot);
		return snapshot;
	}

	public GeneratedSnapshot fetchSnapshot() {
		return current.get();
	}

	public SerializerFacade facade(String signature) {
		ConfigurableSerializerFacade facade = new ConfigurableSerializerFacade(
				snapshotFactories.get(signature).profile());
		currentFacade.set(facade);
		return facade;
	}

	public SerializerFacade facade() {
		SerializerFacade serializerFacade = currentFacade.get();
		serializerFacade.reset();
		return serializerFacade;
	}

	public void setupVariables(String signature, Object... args) {
		SerializerFacade facade = facade(signature);
		modify(newSnapshot(signature), snapshot -> {
			snapshot.setSetupThis(facade.serialize(self.getClass(), self));
			snapshot.setSetupArgs(facade.serialize(snapshot.getArgumentTypes(), args));
		});
	}

	public void expectVariables(Object result, Object... args) {
		SerializerFacade facade = facade();
		GeneratedSnapshot currentSnapshot = fetchSnapshot();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectResult(facade.serialize(snapshot.getResultType(), result));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
		});
		consume(currentSnapshot);
	}

	public void expectVariables(Object... args) {
		SerializerFacade facade = facade();
		GeneratedSnapshot currentSnapshot = fetchSnapshot();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
		});
		consume(currentSnapshot);
	}

	public void throwVariables(Throwable throwable, Object... args) {
		SerializerFacade facade = facade();
		GeneratedSnapshot currentSnapshot = fetchSnapshot();
		modify(currentSnapshot, snapshot -> {
			snapshot.setExpectThis(facade.serialize(self.getClass(), self));
			snapshot.setExpectArgs(facade.serialize(snapshot.getArgumentTypes(), args));
			snapshot.setExpectException(facade.serialize(throwable.getClass(), throwable));
		});
		consume(currentSnapshot);
	}

	private void consume(GeneratedSnapshot snapshot) {
		if (snapshot.isValid()) {
			if (consumer != null) {
				consumer.accept(snapshot);
			}
		}
	}

	private void modify(GeneratedSnapshot snapshot, Consumer<GeneratedSnapshot> task) {
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
