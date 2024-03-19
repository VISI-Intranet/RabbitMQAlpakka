package RabbitMQ.RabbitMQOperation.Operations

import RabbitMQ.RabbitMQModel.RabbitMQModel
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.amqp.{AmqpLocalConnectionProvider, AmqpWriteSettings, WriteMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.rabbitmq.client.AMQP.BasicProperties

import scala.concurrent.{ExecutionContext, Future}

object SendMessageWithCorrelationId {

  def sendMessageWithCorrelationId(message: String, modelMQ: RabbitMQModel, correlationIdArg: String = "", messageType: String = "Event")(implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext): Future[Unit] = {

    var correlationId = correlationIdArg;

    val exchange = modelMQ.exchangeName
    val routingKey = modelMQ.routingKeyName

    if (correlationId.isEmpty)
      correlationId = java.util.UUID.randomUUID().toString

    val amqpConnectionProvider = AmqpLocalConnectionProvider
    val amqpWriteSettings = AmqpWriteSettings(amqpConnectionProvider)
      .withExchange(exchange)
      .withRoutingKey(routingKey)

    val properties = new BasicProperties.Builder().correlationId(correlationId).build()

    val amqpSink = AmqpSink.apply(amqpWriteSettings)
      .contramap[WriteMessage](writeMessage => writeMessage.withProperties(properties))

    // Отправляем сообщение
    val writing: Future[Unit] =
      Source.single(WriteMessage(ByteString(message)))
        .runWith(amqpSink)
        .map(_ => ())

    writing.foreach { _ =>
      println(s"\nMessage sent successfully")
      println(s"Message Type: $messageType")
      println(s"Message Body: $message")
      println(s"Correlation Id: $correlationId")
      println(s"Routing Key: $routingKey \n")
    }

    writing
  }

}
