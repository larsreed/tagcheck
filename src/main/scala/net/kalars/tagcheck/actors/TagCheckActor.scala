package net.kalars.tagcheck.actors

import net.kalars.tagcheck.ScanUtils.sortResults
import net.kalars.tagcheck.{DirSearch, DoneDir, ScanResponse, ScanRequest, ScanResponseLine}
import akka.actor.{ActorSystem, Props, ActorLogging, Actor}

/**
 * The initial recipient & director for the actor solution.
 *
 * Message passing:
 *  1.  -> TagCheckActor: ScanRequest (start dirs)
 *  2.  TagCheckActor -> DirScanner: DirSearch (dir)
 *  3a. DirScanner -> DirScanner: DirSearch (dir)
 *  3b. DirScanner -> FileScanner: FileSearch (dir)
 *  4.  FileScanner -> DirScanner: FileResult (file, meta data)
 *  5.  DirScanner -> TagCheckActor: ScanResponse (n*ScanResponseLine = dir, file, level, warning)
 *  6.  DirScanner -> TagCheckActor / DirScanner: DoneDir(dir)
 */
class TagCheckActor extends Actor with ActorLogging {

  // Internal state for actor. Not externally visible.
  private var children= Set.empty[String] // The directories we are waiting for
  private var results= List.empty[ScanResponseLine] // Accumulating responses

  override def receive= {
    case ScanRequest(fileRegexp, dirList) => // Start here /////////////////////////////////////////
      for (dir <- dirList) {
        log.debug(s"Requesting scan of $dir")
        children += dir
        var child= context.actorOf(Props(new DirScanner(self, fileRegexp)))
        child ! DirSearch(dir)
      }

    case DoneDir(name) => // One child done ////////////////////////////////////////////////////////
      children -= name
      if (children.isEmpty) {
        log.debug("Scan done")
        shutDown()
      }

    case ScanResponse(dir, dirs) => // Results directly from a directory ///////////////////////////
      log.debug(s"Reply from $dir")
      results ++= dirs
  }

  private def shutDown() {
    for (line <- sortResults(results)) line.printLine()
    context.stop(self)
    Thread.sleep(1000) // hack -- Akka is not too good at stopping?
    context.system.terminate()
  }
}

object TagCheckActor {
  /** Starting point for the actor solution. */
  def actorRun(fileRegexp: String, dirs: List[String]) {
    val system = ActorSystem("Main")
    val ac = system.actorOf(Props[TagCheckActor])
    ac ! ScanRequest(fileRegexp, dirs)
  }
}
