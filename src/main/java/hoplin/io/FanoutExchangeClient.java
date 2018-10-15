package hoplin.io;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Client is bound to individual {@link Binding}
 */
public class FanoutExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(FanoutExchangeClient.class);

    private final Binding binding;

    private RabbitMQClient client;

    public FanoutExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        this(options, binding, false);
    }

    public FanoutExchangeClient(final RabbitMQOptions options, final Binding binding, boolean consume)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.binding = binding;

        bind(consume);
    }

    /**
     * This will actively declare:
     *
     * a durable, non-autodelete exchange of "fanout" type
     * a durable, non-exclusive, non-autodelete queue with a well-known name
     *
     * @param consume
     */
    private void bind(final boolean consume)
    {
        final String exchangeName = binding.getExchange();
        // prevent changing default queues
        if(Strings.isNullOrEmpty(exchangeName))
            throw new IllegalArgumentException("Exchange name can't be empty");

        try
        {
            final String type = "fanout";
            // survive a server restart
            final boolean durable = true;
            // keep it even if not in user
            final boolean autoDelete = false;
            // no special arguments
            final Map<String, Object> arguments = null;

            // Make sure that the Exchange is declared
            client.exchangeDeclare(exchangeName, type, durable, autoDelete);

            // setup consumer options
            if (consume)
                consume();
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    private void consume()
    {
        final String exchangeName = binding.getExchange();
        final String queueName = binding.getQueue();

        try
        {
            // Continuing to receive following error
            // reply-code=403, reply-text=ACCESS_REFUSED - queue name 'amq.gen-qRYgATyDl3sFndOO7bSq0w' contains reserved prefix 'amq.*',
            // when using temporary queue
            //final String queueName = client.queueDeclareTemporary();
            //final String queueName = binding.getQueue();

            // Declaring a Temporary Exclusive Queue
            // Exclusive queues may only be accessed by the current connection and are deleted when that connection closes
            final AMQP.Queue.DeclareOk declare = client
                    .queueDeclare(queueName, true, true, false, Collections.emptyMap());

            final String queue = declare.getQueue();
            binding.setQueue(queue);

            // Autocreate a new temporary queue
            client.queueBind(queue, exchangeName, "");
            log.info("Binding client [exchangeName, queueName, bindingKey] : {}, {}", exchangeName, queueName);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to setup consumer", e);
        }
    }

    /**
     * Publish message to the queue with defined routingKey
     *
     * @param message
     */
    public <T> void publish(final T message)
    {
        client.basicPublish(binding.getExchange(), "", message);
    }

    /**
     * Consume message from the queue.
     * This methods should not block
     *
     * @param clazz
     * @param handler
     * @param <T>
     */
    public <T> void consume(final Class<T> clazz, final Consumer<T> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);

        log.info("consuming from queue : {}" ,binding.getQueue());
        client.basicConsume(binding.getQueue(), clazz, handler);
    }

    /**
     * Create new {@link FanoutExchangeClient}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Direct Exchange client setup in server mode
     */
    public static FanoutExchangeClient publisher(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new FanoutExchangeClient(options, binding);
    }

    /**
     * Create new {@link FanoutExchangeClient}
     *
     * @param options the connection options to use
     * @param exchange the exchange to use
     * @return
     */
    public static FanoutExchangeClient publisher(final RabbitMQOptions options, final String exchange)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchange);

        // Producer does not bind to the queue only to the exchange when using FanoutExchange
        final Binding binding = BindingBuilder
                .bind()
                .to(new FanoutExchange(exchange));

        return publisher(options, binding);
    }

    /**
     *Create new {@link FanoutExchangeClient} client, this will create default RabbitMQ queues.
     *
     * @param options the options used for connection
     * @param exchangeName
     * @param exchangeName the exchangeName to use
     * @return
     */
    public static FanoutExchangeClient subscriber(final RabbitMQOptions options, final String exchangeName)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchangeName);

        final Binding binding = BindingBuilder
                .bind()
                .to(new FanoutExchange(exchangeName));

        return new FanoutExchangeClient(options, binding, true);
    }
}
