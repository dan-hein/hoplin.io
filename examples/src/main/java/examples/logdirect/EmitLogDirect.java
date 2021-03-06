package examples.logdirect;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is example of a Work Queues (Direct Exchange) Messages will be discarded when no consumers
 * are present, possible loss of messages. In this one we'll server a Work Queue that will be used
 * to distribute time-consuming tasks among multiple workers.
 * <p>
 * When you run many workers the tasks will be shared between them.
 * <p>
 * <p>
 * Log producer
 */
public class EmitLogDirect extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

  private static final String EXCHANGE = "direct_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer on exchange : {}", EXCHANGE);
    final ExchangeClient client = ExchangeClient.direct(options(), EXCHANGE);

    while (true) {
      client.publish(createMessage("info"), "info");
      /*      client.publish(createMessage("debug"), "debug");
            client.publish(createMessage("warning"), "warning");
            client.publish(createMessage("error"), "error");
*/
      Thread.sleep(1000L);
    }
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
}

