package playground

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import kamon.metric.SubscriptionsDispatcher.TickMetricSnapshot

object WebServer extends App {

  Kamon.start()

  val config = ConfigFactory.load()

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)

  val routes = { // logRequestResult("akka-http-with-kamon") {
    path("ok") {
      get {
        complete {
          "ok"
        }
      }
    } ~
    path("go-outside") {
      get {
        complete {
          Http().singleRequest(HttpRequest(uri = s"http://${config.getString("services.ip-api.host")}:${config.getString("services.ip-api.port")}/"))
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  Kamon.metrics.subscribe("**", "**", system.actorOf(Props[PrintAllMetrics], "printer"))
}

class PrintAllMetrics extends Actor {

  def receive = {
    case TickMetricSnapshot(from, to, metrics) =>
      println("================================================================================")
      println(metrics.map({
        case (entity, snapshot) ⇒
          s"""${entity.category.padTo(20, ' ')} > ${entity.name}   ${entity.tags}
             |      Metrics:
             |      ${snapshot.metrics}""".stripMargin
      }).toList.sorted.mkString("\n"))
  }
}
