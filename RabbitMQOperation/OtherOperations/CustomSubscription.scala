//package RabbitMQ.RabbitMQOperation.OtherOperations
//
//import RabbitMQ.RabbitMQModel.RabbitMQModel
//import akka.NotUsed
//import akka.stream.Materializer
//import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider, NamedQueueSourceSettings, QueueDeclaration}
//import akka.stream.alpakka.amqp.scaladsl.{AmqpSource, CommittableReadResult}
//import akka.stream.scaladsl.{Flow, Sink, Source}
//
//import scala.concurrent.ExecutionContext
//
//object CustomSubscription {
//
//  def customSubscription(modelMQ: RabbitMQModel, messgaeType: String = "Event")(implicit mat: Materializer, ex: ExecutionContext): Unit = {
//    val queueName = modelMQ.queueName
//    val connectionProvider: AmqpConnectionProvider = AmqpLocalConnectionProvider
//
//    val amqpSource: Source[CommittableReadResult, NotUsed] = AmqpSource.committableSource(
//      NamedQueueSourceSettings(connectionProvider, queueName)
//        .withDeclaration(QueueDeclaration(queueName).withDurable(true))
//        .withAckRequired(true),
//      bufferSize = 10
//    )
//
//    val processMessage: Flow[CommittableReadResult, Unit, NotUsed] =
//      Flow[CommittableReadResult].mapAsync(1) { committableReadResult =>
//        val routingKey = committableReadResult.message.envelope.getRoutingKey
//        val message = new String(committableReadResult.message.bytes.toArray, "UTF-8")
//        val correlationId = committableReadResult.message.properties.getCorrelationId
//        println(s"MessageType:$messgaeType  Body: $message, CorrelationId: $correlationId, Routing Key: $routingKey")
//
//        routingKey match {
//          case "univer.teacher-api.createUserPost" =>
//          case _ => // Handle other routing keys here if needed
//        }
//
//        committableReadResult.ack().map(_ => ())
//      }
//
//    amqpSource
//      .via(processMessage)
//      .runWith(Sink.ignore)
//  }
//
//
//}
