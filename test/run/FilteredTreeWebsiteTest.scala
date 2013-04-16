package org.w3.vs.run

import org.w3.vs.util._
import org.w3.vs.util.website._
import org.w3.vs.model._
import org.w3.vs.util.akkaext._
import org.w3.vs.http.Http._
import scala.concurrent.ExecutionContext.Implicits.global
import org.w3.vs.util.Util._
import play.api.libs.iteratee._
import org.w3.vs.util.TestData
import org.w3.vs._
import play.api.Mode

/**
  * Server 1 -> Server 2
  * 1 GET       10 HEAD
  **/
class FilteredTreeWebsiteTest extends VSTest[ActorSystem with HttpClient with Database with RunEvents] with TestData with ServersTest {

  implicit val vs = new ValidatorSuite(Mode.Test) with DefaultActorSystem with DefaultDatabase with DefaultHttpClient with DefaultRunEvents

  val servers = Seq(Webserver(9001, Website.tree(4).toServlet()))

  val job = TestData.job.copy(strategy = TestData.strategy.copy(entrypoint = URL("http://localhost:9001/1/")))

  val maxResources = TestData.strategy.maxResources

  "test FilteredTreeWebsiteTest" in {

    val runningJob = job.run().getOrFail()
    val Running(runId, actorPath) = runningJob.status

    val events = (runningJob.runEvents() &> Enumeratee.mapConcat(_.toSeq) |>>> Iteratee.getChunks[RunEvent]).getOrFail()

    val completeRunEvent = events.collectFirst { case e: DoneRunEvent => e }.get
    completeRunEvent.resources must be(maxResources)

    val rrs = events.collect { case ResourceResponseEvent(_, _, _, rr, _) => rr }
    rrs foreach { rr =>
      rr.url.toString must startWith regex ("http://localhost:9001/[13]")
    }

  }

}
