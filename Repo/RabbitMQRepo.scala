package RabbitMQ.Repo

import RabbitMQ.RabbitMQModel.RabbitMQModel
import RabbitMQ.RabbitMQOperation.Operations.Formatter.extractContent
import RabbitMQ.RabbitMQOperation.Operations.{Formatter, SendMessageAndWaitForResponse, SendMessageWithCorrelationId}
import RabbitMQ.RabbitMQTemple.CreateUserCommand
import akka.actor.ActorSystem
import akka.stream.Materializer
import model._
import connection.MongoDBConnection
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonNull, BsonString}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import com.rabbitmq.client.ConnectionFactory
import model.importModel.ImportStudents
import org.mongodb.scala.model.Filters

object RabbitMQRepo {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def getTeacherById(teacherId: String)(implicit mat: Materializer, system: ActorSystem): Future[Option[TeacherWithStudents]] = {
    val filter = Filters.eq("_id", teacherId)
    val futureTeacher = MongoDBConnection.teacherCollection.find(filter).headOption()

    futureTeacher.map {
      case Some(doc) =>
        Some(
          TeacherWithStudents(
            _id = doc.getString("_id"),
            name = Option(doc.getString("name")),
            age = Option(doc.getInteger("age")).map(_.toInt),
            email = Option(doc.getString("email")),
            phoneNumber = Option(doc.getString("phoneNumber")),
            education = Option(doc.getString("education")),
            studentsId = Option(Option(doc.getList("studentsId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
            qualification = Option(doc.getString("qualification")),
            experience = Option(doc.getInteger("experience")).map(_.toInt),
            scheduleId = Option(doc.getInteger("scheduleId")).map(_.toInt),
            disciplinesId = Option(Option(doc.getList("disciplinesId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
            salary = Option(doc.getInteger("salary")).map(_.toInt),
            position = Option(doc.getString("position")),
            awards = Option(doc.getString("awards")),
            certificationId = Option(doc.getString("certificationId")),
            attestationId = Option(doc.getString("attestationId")),
            students = studentsTakeFun(Option(Option(doc.getList("studentsId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)))

          )
        )
      case None => None
    }
  }

  val pubMQModel:RabbitMQModel=RabbitMQModel("TeacherPublisher","UniverSystem","univer.teacher-api.studentsByIdGet")
  val replyMQModel:RabbitMQModel=RabbitMQModel("TeacherSubscription","UniverSystem","univer.teacher-api.studentsByIdGet")

  def studentsTakeFun(ids:Option[List[String]])(implicit mat: Materializer, system: ActorSystem):Option[List[ImportStudents]] = {

    SendMessageAndWaitForResponse.sendMessageAndWaitForResponse(Formatter.extractContentList(ids),pubMQModel,replyMQModel)

    None
  }

}
