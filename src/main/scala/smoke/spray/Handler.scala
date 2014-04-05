package smoke.spray

import smoke.{ Request, Response, UTF8Data, RawData }
import scala.concurrent._
import akka.actor._
import akka.pattern._
import akka.io.IO
import spray.can.Http
import spray.http._
import spray.http.parser.HttpParser
import spray.http.HttpHeaders._
import java.net.URI
import com.typesafe.config.Config
import collection.JavaConversions._

case class SprayRequest(sprayRequest: HttpRequest) extends Request {

  val version = sprayRequest.protocol.toString
  val method = sprayRequest.method.toString

  val uri = new URI(sprayRequest.uri.toString)
  val headers = sprayRequest.headers map { e ⇒ (e.name, e.value) } toSeq

  val keepAlive = !(headers.collect { case ("connection", "keep-alive") ⇒ }.isEmpty)

  val requestIp = (headers.collect { case ("Remote-Address", ip) ⇒ ip }.head)

  val cookies = sprayRequest.cookies.map { case c: HttpCookie ⇒ (c.name -> c.content) }.toMap

  val body = sprayRequest.entity.asString
  val contentLength = sprayRequest.entity.data.length.toInt
}

class Handler(application: (Request ⇒ Future[Response]), config: Config) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val system = context.system

  config.getIntList("http.ports") map { p ⇒
    IO(Http) ! Http.Bind(self, interface = "localhost", port = p)
  }

  def receive = {
    case _: Http.Connected ⇒ sender() ! Http.Register(self)
    case r: HttpRequest ⇒
      application(SprayRequest(r)) map {
        response ⇒
          HttpResponse(
            status = response.statusCode,
            headers = response.headers.map {
              case (key, value) ⇒ HttpParser.parseHeader(RawHeader(key, value))
            }.collect { case Right(header) ⇒ header }.toList,
            entity = response.body match {
              case UTF8Data(data) ⇒ data
              case RawData(data)  ⇒ data
            })
      } pipeTo sender
  }
}
