package playground

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl._
import akka.stream.{ ActorAttributes, ActorMaterializer, Materializer, Supervision }
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object WebServer extends App {

  Kamon.start()

  val config = ConfigFactory.load()

  val port: Int = config.getInt("http.port")
  val interface: String = config.getString("http.interface")

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
      } ~
      path("fail") {
        get {
          complete {
            throw new RuntimeException("This is an intentionally runtime exception.")
          }
        }
      }
  }

  val bindingFuture = Http().bindAndHandle(routes, interface, port)

  val matGraph = ProduceMetrics.activate(500 millis, Vector(
    s"/ok",
    s"/go-outside",
    s"/fail"
  ))

  println(s"Server online at http://$interface:$port/\nPress RETURN to stop...")

  Console.readLine() // for the future transformations

//  matGraph.cancel()

  val stopServerFut = bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .foreach(_ ⇒ system.terminate()) // and shutdown when done

}

object ProduceMetrics {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)

  def activate(tickInterval: FiniteDuration, endpoints: Vector[String], interface: String = "localhost", port: Int = 8080)
              (implicit materializer: Materializer, system: ActorSystem): Unit/*Cancellable*/ = {

    implicit val materializer = ActorMaterializer()

    val timeout = 2 seconds

    val decider: Supervision.Decider = {
      case exc =>
        logger.error(s"Exception: $exc")
        Supervision.Resume
    }

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = {
      Http().outgoingConnection(interface, port)
    }

    val tickSource =
      Source.tick(tickInterval, tickInterval, NotUsed)
      .map(_ => {
        val httpRequest = HttpRequest(uri = endpoints(Random.nextInt(endpoints.size)))
        logger.info(s"Request: ${httpRequest.getUri()}")
        httpRequest
      })

    val cancellable = tickSource
      .viaMat(connectionFlow)(Keep.right)
      .toMat(Sink.foreach { case httpResponse => httpResponse.toStrict(timeout) })(Keep.left)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .run()

  }
}
