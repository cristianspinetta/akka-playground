package playground

import akka.http.scaladsl.model.HttpRequest
import kamon.akka.http.NameGenerator

class AkkaHttpNameGenerator extends NameGenerator {
  def generateTraceName(request: HttpRequest): String = request.getUri().path()
  def generateRequestLevelApiSegmentName(request: HttpRequest): String = "request-level " + request.uri.path.toString()
  def generateHostLevelApiSegmentName(request: HttpRequest): String = "host-level " + request.uri.path.toString()
}
