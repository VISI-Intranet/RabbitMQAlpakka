package RabbitMQ.RabbitMQOperation.Operations

import RabbitMQ.RabbitMQModel.RabbitMQModel
import RabbitMQ.RabbitMQOperation.Operations.SendMessageWithCorrelationId.sendMessageWithCorrelationId
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.alpakka.amqp.scaladsl.{AmqpSource, CommittableReadResult}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContext

object ListenAndRespond {

  def listenAndRespond(pubModelMQ: RabbitMQModel, replyModelMQ: RabbitMQModel, messageType: String = "Response")(implicit mat: Materializer, ex: ExecutionContext, system: ActorSystem): Unit = {
    val queueName = pubModelMQ.queueName
    val connectionProvider: AmqpConnectionProvider = AmqpLocalConnectionProvider

    val amqpSource: Source[CommittableReadResult, NotUsed] = AmqpSource.committableSource(
      NamedQueueSourceSettings(connectionProvider, queueName)
        .withDeclaration(QueueDeclaration(queueName).withDurable(true))
        .withAckRequired(true),
      bufferSize = 10
    )

    val processMessage: Flow[CommittableReadResult, Unit, NotUsed] =
      Flow[CommittableReadResult].mapAsync(1) { committableReadResult =>
        val message = new String(committableReadResult.message.bytes.toArray, "UTF-8")

        val correlationId = committableReadResult.message.properties.getCorrelationId

        val routingKey = committableReadResult.message.envelope.getRoutingKey

        println(s"MessageType: $messageType - Body:$message  - CorrelationId: $correlationId - Routing Key: $routingKey")


        // Отправка сообщения с тем же CorrelationId в очередь "replyQueue"
        val replyMessage = s"Reply to: $message"
        sendMessageWithCorrelationId(replyMessage, replyModelMQ, correlationId)

        committableReadResult.ack().map(_ => ())
      }

    amqpSource
      .via(processMessage)
      .runWith(Sink.ignore)
  }

}
