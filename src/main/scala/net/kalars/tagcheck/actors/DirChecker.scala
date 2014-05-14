package net.kalars.tagcheck.actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import net.kalars.tagcheck.tags._

/** Checks the validity of metadata in a directory. */
class DirChecker(boss:ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case dir: DirResult=>
      log.debug(s"Validating ${dir.name}")
      val files= for (file<-dir.files) yield {
        Checkers.checkers.foldLeft(file) { (res, checker) => checker.check(res) }
      }
      boss ! DirResult(dir.name, files)
      context.stop(self)
  }
}
