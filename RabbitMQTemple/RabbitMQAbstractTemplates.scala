package RabbitMQ.RabbitMQTemple

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

abstract class RequestReplyMessage(val requestId: String)

abstract class RecieveReplyMessage(val requestId: String)

abstract class CommandMessage

abstract class NotificationMessage

abstract class DataMessage[T](val data: T)

case class CreateUserCommand(
                              _id:String,
                              name: Option[String],
                              age: Option[Int],
                              email: Option[String],
                              phoneNumber: Option[String],
                              checkCreate:Boolean = false,
                              userType:String = "Teacher"
  )



object CreateUserCommand {
  // Реализация для автоматической сериализации и десериализации с использованием Circe
  implicit val createUserCommandEncoder: Encoder[CreateUserCommand] = deriveEncoder[CreateUserCommand]
  implicit val createUserCommandDecoder: Decoder[CreateUserCommand] = deriveDecoder[CreateUserCommand]
}
