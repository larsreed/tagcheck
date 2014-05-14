package net.kalars.tagcheck.actors

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import net.kalars.tagcheck.io.IoUtils._

/** Scans a directory, starts file scanners for auidio files and new dir scanners for subdirectories. */
class DirScanner(boss:ActorRef, fileRegexp:String) extends Actor with ActorLogging {
  var children= Set.empty[ActorRef]
  var results= List.empty[FileResult]
  var dir= ""

  override def receive = {
    case DirSearch(dirName, depth) =>
      dir= dirName
      log.debug(s"Scanning directory $dir ($depth)")
      val (dirs, allFiles)= scanDir(dirName)
      if (depth>0) for (dir <- dirs) {
        val child = context.actorOf(Props(new DirScanner(boss, fileRegexp)))
        boss ! FoundDir(dir)
        child ! DirSearch(dir, depth-1)
      }
      val files= allFiles filter { _.toUpperCase.matches(fileRegexp) }
      for (file <- files ) {
        val child= context.actorOf(Props[FileScanner])
        children += child
        child ! FileSearch(file)
      }
      if (files.isEmpty) boss ! DirResult(dirName, List.empty)
    case msg:FileResult =>
      children -= sender
      results ::= msg
      if (children.isEmpty) {
        log.debug(s"Done scanning directory $dir")
        context.actorOf(Props(new DirChecker(boss))) ! DirResult(dir, results)
      }
  }
}
