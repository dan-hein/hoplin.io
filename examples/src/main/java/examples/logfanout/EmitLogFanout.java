package examples.logfanout;

import examples.BaseExample;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is example of a Fanout Exchange
 * <p>
 * Log producer
 */
public class EmitLogFanout extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitLogFanout.class);

  private static final String EXCHANGE = "fanout_logs";

  public static void main(final String... args) throws InterruptedException {
    final Binding binding = bind();
    log.info("Binding : {}", binding);

    final ExchangeClient client = ExchangeClient.fanout(options(), binding);

    long msgId = 0;
    int i = 0;
//    while (i++ < 1000) {
    while(true){

      for(int j = 0; j< 100; j++) {
        client.publish("Msg : "+ (++msgId) +" > " + System.currentTimeMillis());
      }
      Thread.sleep(10L);
//            break;
    }

//    Thread.currentThread().join();
  }

  private static Binding bind() {
    return BindingBuilder
        .bind()
        .to(new FanoutExchange(EXCHANGE));
  }

}

