Connecting to RabbitMQ
======================

Connecting to RabbitMQ can be accomplished in tow different ways, any of the options can be set through either of the
methods. Hoplin uses one connection per client with one dedicated channel.

First method uses a JDBC like connection string. The connection string is made up of key/value pairs `key=value`.
Where keys are case insensitive and values are case sensitive.

Basic connection will look as follow

.. code-block:: java
   :linenos:

    host=localhost;virtualHost=vhost1;username=user;password=secret

Below is the list of connection string options. When constructing a connection string, these options should be separated by a semicolon.



Available keys
--------------

host
++++
The host name of the broker.
This Host value is required.

virtualHost
+++++++++++
The virtual host to use.
Default: /

Port
++++
The port where the broker listens.
Default: 5671 if the UseTls setting is set to true, otherwise the default value is 5672

UserName
++++++++
The user name to use to connect to the broker.
Default: guest

Password
++++++++
The password to use to connect to the broker.
Default: guest

RequestedHeartbeat
++++++++++++++++++
The interval for the heartbeats between the client and the server.
Default: 3 seconds

ConnectionRetries
+++++++++++++++++++
Number of connection retries
Default: 3 seconds

ConnectionRetryDelay
+++++++++++++++++++
The time to wait before trying to reconnect to the broker if the connection is lost.
Default: 10 seconds


To set properties via code. Sensible default will be provided when new `RabbitMQOptions` object is created.

.. code-block:: java
   :linenos:

    RabbitMQOptions options = new RabbitMQOptions();
    options.setConnectionRetries(3);
    options.setConnectionRetryDelay(250L);


Instantiating new client
-------------------------

Creating simple RabbitMQ client can be done in couple different ways.

The simplest way with minimal configuration, this creates new Exchange client bound to a Topic exchange.

.. code-block:: java
   :linenos:

    ExchangeClient client = ExchangeClient.topic(RabbitMQOptions.from("host=localhost"))



This creates new Exchange client bound to a Topic exchange.
We can also specify which queue and which routing key we want to handle.

.. code-block:: java
   :linenos:

    RabbitMQOptions options = RabbitMQOptions.from("host=localhost");
    ExchangeClient client = ExchangeClient.topic(options, "my.exchange", "log.critical", "log.critical.*")


For complete control we can use the Exchange to Queue Binding builder.

.. code-block:: java
   :linenos:

     RabbitMQOptions options = RabbitMQOptions.from("host=localhost");
     Binding binding = BindingBuilder
                    .bind(queue)
                    .to(new TopicExchange(exchange))
                    .withAutoAck(true)
                    .withPrefetchCount(1)
                    .withPublisherConfirms(true)
                    .with(routingKey)
                    .build();

      ExchangeClient client = ExchangeClient.topic(options(), binding);



Client Types
--------------


* Direct
* Fanout
* Topic
* Header
* Exchange
