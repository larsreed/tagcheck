package net.kalars.tagcheck.actors

import akka.actor._
import net.kalars.tagcheck.io.IoUtils._
import net.kalars.tagcheck._
import net.kalars.tagcheck.tags.Checkers
import net.kalars.tagcheck.FileSearch
import net.kalars.tagcheck.DoneDir
import net.kalars.tagcheck.FileResult
import net.kalars.tagcheck.DirSearch

/**
 * Scans a directory, starts file scanners for audio files and new dir scanners for subdirectories.
 * When all files are scanned, sends a message to a new `DirChecker` to evaluate the result.
  */
class DirScanner(boss:ActorRef, fileRegexp:String) extends Actor with ActorLogging {
  var fileCount= 0
  var dirCount= 0
  var dir= ""
  var fileScanner: ActorRef= _
  var results= List.empty[FileResult]

  override def receive = {
    case DirSearch(dirName, depth) => /////////////////////////////////////////////////////////////
      dir= dirName
      fileScanner= context.actorOf(Props[FileScanner])
      log.debug(s"Scanning directory $dir ($depth)")
      scan(dirName, depth)
      possiblyDone()

    case msg:FileResult => ////////////////////////////////////////////////////////////////////////
      fileCount -= 1
      results ::= checkDir(msg)
      possiblyDone()

    case DoneDir(dirName) => //////////////////////////////////////////////////////////////////////
      dirCount -= 1
      possiblyDone()
  }


  def possiblyDone() {
    if (fileCount==0) {
      log.debug(s"Done scanning directory $dir")
      val lines = for {fileRes <- results if fileRes.warningLevel > 0
                       warn <- fileRes.warnings} yield {
        ScanResponseLine(dir, fileRes.name, warn.level, warn.text)
      }
      boss ! ScanResponse(lines.toList)
      fileCount= -1
    }
    if (fileCount <=0 && dirCount==0 ) {
      context.parent ! DoneDir(dir)
      fileScanner ! PoisonPill
      self ! PoisonPill
    }
  }

  def scan(dirName: String, depth:Int) = {
    val (dirs, allFiles) = scanDir(dirName)
    if (depth > 0) for (dir <- dirs) {
      dirCount += 1
      val child = context.actorOf(Props(new DirScanner(boss, fileRegexp)))
      child ! DirSearch(dir, depth - 1)
    }
    val files = allFiles filter { _.toUpperCase.matches(fileRegexp) } // TODO use regexp flag rather than toupper
    for (file <- files) {
      fileCount += 1
      fileScanner ! FileSearch(file)
    }
  }

  def checkDir(file:FileResult): FileResult =
    Checkers.checkers.foldLeft(file){ (res, checker) => checker.check(res)}
}
