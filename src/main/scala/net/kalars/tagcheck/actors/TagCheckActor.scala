package net.kalars.tagcheck.actors

import akka.actor._
import net.kalars.tagcheck._
import net.kalars.tagcheck.ScanResponse
import net.kalars.tagcheck.ScanRequest
import net.kalars.tagcheck.ScanResponseLine
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
        for (line<-sortResults) line.printLine()
        context.stop(self)
        Thread.sleep(1000)
        context.system.shutdown()
      }

    case ScanResponse(dirs) => ////////////////////////////////////////////////////////////////////
      if (immediateResponse) for (line<-dirs) line.printLine()
      else results ++= dirs
  }

  def sortResults: List[ScanResponseLine] = {
    results.sortWith { (a, b) =>
      if (a.level == b.level) a.file < b.file
      else a.level > b.level
    }
  }
}

object TagCheckActor {
  def actorRun(immediateResponse: Boolean, fileRegexp: String, maxDepth:Int, dirs: List[String]) {
    val system = ActorSystem("Main")
    // dirs ::= "N:\\mp3\\David Bowie"
    val ac = system.actorOf(Props[TagCheckActor])
    ac ! ScanRequest(maxDepth, immediateResponse, fileRegexp, dirs)
  }
}
