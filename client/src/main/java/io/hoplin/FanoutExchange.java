package io.hoplin;

/**
 * Fanout exchange
 * <p>
 * A fanout exchange routes messages to all of the queues that are bound to it and the routing key
 * is ignored. If N queues are bound to a fanout exchange, when a new message is published to that
 * exchange a copy of the message is delivered to all N queues.
 * <p>
 * Fanout exchanges are ideal for the broadcast routing of messages.
 */
public class FanoutExchange extends AbstractExchange {

  public FanoutExchange(final String name) {
    super(name);
  }

  @Override
  public ExchangeType getType() {
    return ExchangeType.FANOUT;
  }
}
