package io.hoplin;

import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

/**
 * Fanout Exchange Client
 */
public class FanoutExchangeClient extends AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(FanoutExchangeClient.class);

    public FanoutExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        this(options, binding, false);
    }

    public FanoutExchangeClient(final RabbitMQOptions options, final Binding binding, boolean consume)
    {
        super(options, binding);
        bind(consume, "fanout");
    }

    void subscribe()
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
     * Create new {@link FanoutExchangeClient} given supplied options and {@link Binding}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Direct Exchange client setup in server mode
     */
    public static ExchangeClient publisher(final RabbitMQOptions options, final Binding binding)
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
    public static ExchangeClient publisher(final RabbitMQOptions options, final String exchange)
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
    public static ExchangeClient subscriber(final RabbitMQOptions options, final String exchangeName)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchangeName);

        final Binding binding = BindingBuilder
                .bind()
                .to(new FanoutExchange(exchangeName));

        return new FanoutExchangeClient(options, binding, true);
    }
}
