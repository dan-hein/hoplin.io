package io.hoplin.rpc;

import com.rabbitmq.client.*;
import io.hoplin.*;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * On the reception of each RPC request, this consumer will
 *
 * <ul>
 *     <li>Perform the action required in the RPC request</li>
 *     <li>Prepare the reply message Set the correlation ID in the reply properties</li>
 *     <li>Publish the answer on the reply queue</li>
 *     <li>Send the ack to the RPC request</li>
 * </ul>
 *
 * @param <I>
 * @param <O>
 */
public class RpcResponderConsumer<I, O> extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(RpcResponderConsumer.class);

    private final Executor executor;

    private final Function<I, O> handler;

    private JsonCodec codec;

    private ConsumerErrorStrategy errorStrategy;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *  @param channel the channel to which this consumer is attached
     * @param handler
     * @param executor
     */

    public RpcResponderConsumer(final Channel channel,
                                       final Function<I, O> handler,
                                       final Executor executor)
    {
        super(channel);

        this.executor = Objects.requireNonNull(executor);
        this.handler = Objects.requireNonNull(handler);
        this.codec = new JsonCodec();
        this.errorStrategy = new DeadLetterErrorStrategy(channel);
    }

    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body)
    {

        log.info("RPC handleDelivery Envelope   : {}", envelope);
        log.info("RPC handleDelivery Properties : {}", properties);

        // 1 : Perform the action required in the RPC request
        CompletableFuture
                .supplyAsync(()-> dispatch(body), executor)
                .whenComplete((reply, throwable) ->
        {
            final MessageContext context = MessageContext.create(consumerTag, envelope, properties);

            try
            {
                byte[] replyMessage  = reply;

                //0 : there was unhandled exception while processing message
                if(throwable != null)
                {
                    log.warn("Error dispatching message : {}", throwable);
                    replyMessage = createErrorMessage(throwable);
                }

                // 2 : Prepare the reply message Set the correlation ID in the reply properties
                final AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                // 3 : Publish the answer on the reply queue
                final String replyTo = properties.getReplyTo();
                log.info("replyTo, correlationId :  {}, {}", replyTo, properties.getCorrelationId());

                getChannel().basicPublish("", replyTo, replyProperties, replyMessage);
                // 4 : Send the ack to the RPC request

                // Invoke ACK
                AckStrategy.acknowledge(getChannel(), context, AcknowledgmentStrategies.BASIC_ACK.strategy());
            }
            catch (final Exception e1)
            {
                log.error("Unable to acknowledge execution", e1);
            }
        });
    }


    @SuppressWarnings("unchecked")
    private byte[] dispatch(final byte[] body)
    {
        try
        {
            final MessagePayload<?> requestMsg = codec.deserialize(body, MessagePayload.class);

            try
            {
                final O reply = handler.apply((I) requestMsg.getPayload());
                return codec.serialize(new MessagePayload(reply), MessagePayload.class);
            }
            catch (final Exception e)
            {
                log.warn("Handling message error : {} ", e);
                return codec.serialize(MessagePayload.error(e), MessagePayload.class);
            }
        }
        catch (final Exception e)
        {
            log.error("Unable to apply reply handler", e);
            throw new HoplinRuntimeException("Unable to apply reply handler", e);
        }
    }

    private byte[] createErrorMessage(Throwable throwable)
    {
        try
        {
            return codec.serialize(MessagePayload.error(throwable), MessagePayload.class);
        }
        catch (final Exception e)
        {
            log.error("Unable to serialize message", e);
            throw new HoplinRuntimeException("Unable to serialize message", e);
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig)
    {
       log.warn("Handle Shutdown Signal :{} , {}", consumerTag, sig);
    }
}
