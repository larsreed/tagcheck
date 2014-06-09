package net.kalars.tagcheck.actors

import akka.actor._
import net.kalars.tagcheck._
import net.kalars.tagcheck.ScanResponse
import net.kalars.tagcheck.ScanRequest
import net.kalars.tagcheck.ScanResponseLine
import net.kalars.tagcheck.ScanUtils.sortResults
import net.kalars.tagcheck.DirSearch

/** The initial recipient & director. */
class TagCheckActor extends Actor with ActorLogging {

  var children= Set.empty[String]
  var results= List.empty[ScanResponseLine]
  var immediateResponse= false

  override def receive= {
    case ScanRequest(maxLevels, immediate, fileRegexp, dirList) => ////////////////////////////////
      immediateResponse= immediate
      for (dir <- dirList) {
        log.debug(s"Requesting scan of $dir")
        children += dir
        var child= context.actorOf(Props(new DirScanner(self, fileRegexp)))
        child ! DirSearch(dir, maxLevels)
      }

    case DoneDir(name) => /////////////////////////////////////////////////////////////////////////
      children -= name
      if (children.isEmpty) {
        log.debug("Scan done")
        for (line<-sortResults(results)) line.printLine()
        context.stop(self)
        Thread.sleep(1000)
        context.system.shutdown()
      }

    case ScanResponse(dir, dirs) => ////////////////////////////////////////////////////////////////////
      log.debug(s"Reply from $dir")
      if (immediateResponse) for (line<-sortResults(dirs)) line.printLine()
      else results ++= dirs
  }
}

object TagCheckActor {
  def actorRun(immediateResponse: Boolean, fileRegexp: String, maxDepth:Int, dirs: List[String]) {
    val system = ActorSystem("Main")
    val ac = system.actorOf(Props[TagCheckActor])
    ac ! ScanRequest(maxDepth, immediateResponse, fileRegexp, dirs)
  }
}
