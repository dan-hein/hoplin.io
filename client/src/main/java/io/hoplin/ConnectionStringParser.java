package io.hoplin;

import java.util.Arrays;
import java.util.Objects;

/**
 * Connection string parser Parse the connection string in format key1=value;key2=value
 * <pre>host=localhost;virtualHost=vhost1;username=user;password=secret</pre>
 *
 * <ul>
 *     <li>host (e.g. host=localhost or host=192.168.2.56) </li?>
 *     <li>virtualHost (e.g. virtualHost=myVirtualHost) default is the default virtual host '/' </li?>
 *     <li>prefetchcount (e.g. prefetchcount=1) default is 10. This is the number of messages that will be delivered by RabbitMQ before an ack is sent </li?>
 *     <li>publisherConfirms (e.g. publisherConfirms=true) default is false.</li?>
 * </ul>
 */
public class ConnectionStringParser {

  /**
   * Parse the connection string in format key1=value;key2=value
   * <pre>host=localhost;virtualHost=vhost1;username=user;password=secret</pre>
   *
   * @param connectionString the connection string to parse
   * @return the {@link RabbitMQOptions} created from the string
   */
  public static RabbitMQOptions parse(final String connectionString) {
    Objects.requireNonNull(connectionString);
    final String[] parts = connectionString.split(";");
    final RabbitMQOptions options = new RabbitMQOptions();

    // check for required fields
    final boolean hostPresent = Arrays.stream(parts).anyMatch(p -> p.equalsIgnoreCase("host"));
    if (!hostPresent) {
      throw new HoplinRuntimeException("Required field 'host' is not present");
    }

    for (final String part : parts) {
      final String[] kv = part.split("=");
      if (kv.length != 2) {
        throw new HoplinRuntimeException(
            "Invalid KeyValue pair, expected connection string is in format 'host=localhost;virtualHost=vhost1' but got : "
                + part);
      }

      final String key = kv[0].trim();
      final String value = kv[1].trim();

      switch (key.toLowerCase()) {
        case "host":
          options.setHost(value);
          break;
        case "virtualhost":
          options.setVirtualHost(value);
          break;
        case "username":
          options.setUser(value);
          break;
        case "password":
          options.setPassword(value);
          break;
        case "requestedheartbeat":
          options.setRequestedHeartbeat(Integer.parseInt(value));
          break;
        case "timeout":
          options.setConnectionTimeout(Integer.parseInt(value));
          break;
        case "product":
          options.setClientProperty("product", value, true);
          break;
        case "platform":
          options.setClientProperty("platform", value, true);
          break;
        case "connectionretries":
          options.setConnectionRetries(Integer.parseInt(value));
          break;
        case "connectionretrydelay":
          options.setConnectionRetryDelay(Long.parseLong(value));
          break;
        default:
          throw new IllegalArgumentException("Unknown option : " + key);
      }
    }

    return options;
  }

}
