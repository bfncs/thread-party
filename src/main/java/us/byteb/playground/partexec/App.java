package us.byteb.playground.partexec;

import us.byteb.playground.partexec.executor.Executor;
import us.byteb.playground.partexec.executor.SyncExecutor;

public class App {

  private static final int NUM_BATCHES = 5;
  private static final int NUM_SOURCES = 16;
  private static final int BATCH_SIZE = 100;

  public static void main(String[] args) throws InterruptedException {
    benchmark(SyncExecutor.class);
  }

  private static void benchmark(final Class<? extends Executor> executorClass) {
    final Executor executor;
    try {
      executor = executorClass.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    final MessageProducer producer = new MessageProducer(NUM_SOURCES);
    final MessageProcessor processor = new MessageProcessor();
    final MessageConsumer consumer = new MessageConsumer();

    final long startTime = System.nanoTime();
    executor.execute(producer, processor, consumer, NUM_BATCHES, BATCH_SIZE);
    final long endTime = System.nanoTime();

    if (!producer.getSourceStates().equals(consumer.getSourceStates())) {
      throw new IllegalStateException("Final producer and consumer states are not equal!");
    }

    final float durationMs = (endTime - startTime) / 1000000.0f;
    System.out.printf("%s done in %.3f ms%n", executorClass.getSimpleName(), durationMs);
  }

}