package us.byteb.playground.threadparty.executor;

import us.byteb.playground.threadparty.Message;
import us.byteb.playground.threadparty.MessageConsumer;
import us.byteb.playground.threadparty.MessageProcessor;
import us.byteb.playground.threadparty.MessageProducer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GroupingMultithreadedExecutor implements Executor {
  final ExecutorService executorService = new ForkJoinPool(8);

  @Override
  public void execute(
      final MessageProducer producer,
      final MessageProcessor processor,
      final MessageConsumer consumer,
      final int totalMessages,
      final int batchSize) {
    final int numBatches = totalMessages / batchSize;

    int messagesReceived = 0;
    while (messagesReceived < totalMessages) {
      final List<Message> messages = producer.nextBatch(batchSize);
      messagesReceived += messages.size();
      executeBlockingWithOwnExecutorService(
          () -> {
            final Map<Integer, List<Message>> messagesBySource =
                messages.stream().collect(Collectors.groupingBy(Message::getSource));

            messagesBySource
                .entrySet()
                .parallelStream()
                .map(Map.Entry::getValue)
                .forEach(msgs -> consumer.consumeBatch(msgs, processor::process));
          });
      ;
    }
  }

  private void executeBlockingWithOwnExecutorService(final Runnable runnable) {
    try {
      executorService.submit(runnable).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
  }

  @Override
  public void close() throws Exception {
    executorService.shutdown(); // Disable new tasks from being submitted
    try {
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
          System.err.println("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
