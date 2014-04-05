package smoke.examples

import akka.actor._
import smoke._
import com.typesafe.config.ConfigFactory

object BasicExampleApp extends App {
  val smoke = new BasicExampleSmoke
}

class BasicExampleSmoke extends Smoke {
  val smokeConfig = ConfigFactory.load().getConfig("smoke")
  val system = ActorSystem("BasicExampleSmoke", smokeConfig)
  val executionContext = scala.concurrent.ExecutionContext.global

  onRequest {
    case r @ GET(Path("/example")) ⇒ reply {
      Thread.sleep(1000)
      Response(Ok, body = "It took me a second to build this response.\n")
    }
    case _ ⇒ reply(Response(NotFound))
  }

  after { response ⇒
    val headers = response.headers ++ Map(
      "Server" -> "BasicExampleApp/0.0.1",
      "Connection" -> "Close")
    Response(response.status, headers, response.body)
  }
}

