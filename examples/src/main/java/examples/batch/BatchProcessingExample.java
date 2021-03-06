package examples.batch;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.DirectExchange;
import io.hoplin.batch.BatchClient;
import io.hoplin.batch.DefaultBatchClient;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessingExample extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(BatchProcessingExample.class);

  public static void main(final String... args) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final BatchClient client = new DefaultBatchClient(options(), bind());

    client.startNew(context ->
    {
      for (int i = 0; i < 2; ++i) {
        context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "info"));
        context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "warn"));
      }
    })
    .whenComplete((context, throwable) ->
    {
      log.info("Batch completed in : {}", context.duration());
      latch.countDown();
    });

    latch.await();
  }

  private static Binding bind() {
    return BindingBuilder
        .bind("batch.documents")
        .to(new DirectExchange("exchange.batch"))
        .build()
        ;
  }
}
