package smoke.examples

import smoke._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

class Responder extends Actor {
  def receive = {
    case GET(Path("/example")) ⇒
      Thread.sleep(1000)
      sender ! Response(Ok, body = "It took me a second to build this response.\n")
    case _ ⇒ sender ! Response(NotFound)
  }
}

object ActorExampleApp extends SmokeApp {
  val executionContext = system.dispatcher
  val actor = system.actorOf(Props[Responder])

  implicit val timeout = Timeout(10.seconds)

  onRequest(actor ? _ mapTo manifest[Response])

  after { response ⇒
    val headers = response.headers :+ ("Server", "ActorExampleApp/0.0.1")
    Response(response.status, headers, response.body)
  }
}

