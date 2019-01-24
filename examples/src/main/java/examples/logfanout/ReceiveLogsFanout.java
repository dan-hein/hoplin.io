package examples.logfanout;

import com.rabbitmq.client.AMQP;
import examples.BaseExample;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchangeClient;
import io.hoplin.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanout extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogFanout.class);

    private static final String EXCHANGE = "fanout_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final ExchangeClient client = FanoutExchangeClient.create(options(), EXCHANGE);

        client.subscribe("Test", String.class, ReceiveLogsFanout::handle);
        Thread.currentThread().join();
    }

    private static void handle(final String msg, final MessageContext context)
    {
        log.info("Incoming context >  {}", context);
        log.info("Incoming msg     >  {}", msg);
    }
}
