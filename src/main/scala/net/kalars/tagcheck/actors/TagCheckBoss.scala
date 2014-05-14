package net.kalars.tagcheck.actors

import akka.actor.{ActorRef, Props, ActorLogging, Actor}

/** The initial recipient. */
class TagCheckBoss(reportTo: ActorRef, immediateResponse:Boolean,
                   fileRegexp: String) extends Actor with ActorLogging {

  var children= Set.empty[String]
  var results= List.empty[DirResult]

  override def receive= {
    case ScanRequest(maxLevels, dirList) =>
      for (dir <- dirList) {
        log.debug(s"Requesting scan of $dir")
        var child= context.actorOf(Props(new DirScanner(self, fileRegexp)))
        children += dir
        child ! DirSearch(dir, maxLevels)
      }
    case FoundDir(name) =>
      children += name
    case res:DirResult =>
      children -= res.name
      if (res.warningLevel>0) {
        log.debug(s"Warnings on ${res.name}: ${res.warnings.size}")
        if (immediateResponse)  res.warnings.map { line => reportTo ! line }
        else results ::= res
      }
      else log.debug(s"No warnings in ${res.name}")
      if (children.isEmpty) {
        log.debug("Scan done")
        if (!immediateResponse) {
          val all= results.flatMap(_.warnings).sortWith{(a,b)=>
            if (a.level==b.level) a.file < b.file
            else a.level > b.level
          }
          reportTo ! ScanResponse(all)
        }
        else reportTo ! ScanResponse(List.empty)
        context.stop(self)
      }
  }
}
