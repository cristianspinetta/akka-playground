package playground

import akka.actor.{ Actor, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import kamon.metric.SubscriptionsDispatcher.TickMetricSnapshot
import kamon.spray.KamonTraceDirectives
import spray.client.pipelining._
import spray.http._
import spray.httpx.RequestBuilding
import spray.routing.SimpleRoutingApp

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object SprayServer extends App with SimpleRoutingApp with RequestBuilding with KamonTraceDirectives {

  val config = ConfigFactory.load()

  Kamon.start()

  implicit val system = ActorSystem("test-spray-with-kamon")
  implicit def executor = system.dispatcher

  val counter = Kamon.metrics.counter("requests")

  val pipeline = sendReceive

  startServer(interface = config.getString("http.interface"), port = config.getInt("http.port")) {
    path("hello") {
      get {
        traceName("GetHello") {
          dynamic {
            counter.increment()
            complete {
              "helo world!!"
            }
          }
        }
      }
    } ~
      path("go-outside") {
        get {
          traceName("GetGoOutside") {
            complete {
              pipeline(Get(s"http://${config.getString("services.ip-api.host")}:${config.getString("services.ip-api.port")}/"))
            }
          }
        }

      }
  }

  Kamon.metrics.subscribe("**", "**", system.actorOf(Props[PrintAllMetrics], "printer"))

}

class PrintAllMetrics extends Actor {

  def receive = {
    case TickMetricSnapshot(from, to, metrics) ⇒
      println("================================================================================")
      println(metrics.map({
        case (entity, snapshot) ⇒
          s"""${entity.category.padTo(20, ' ')} > ${entity.name}   ${entity.tags}
             |      Metrics:
             |      ${snapshot.metrics}""".stripMargin
      }).toList.sorted.mkString("\n"))
  }
}
