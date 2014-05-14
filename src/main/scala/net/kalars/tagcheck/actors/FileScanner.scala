package net.kalars.tagcheck.actors

import akka.actor.{ActorLogging, Actor}
import net.kalars.tagcheck.io.IoUtils._

/** Scans a file, sends a result message. */
class FileScanner extends Actor with ActorLogging {
  override def receive = {
    case FileSearch(file:String) =>
      log.debug(s"Extracting tags from $file")
      val tags= extractTags(file)
      sender ! FileResult(file, tags, List.empty)
      context.stop(self)
  }
}
