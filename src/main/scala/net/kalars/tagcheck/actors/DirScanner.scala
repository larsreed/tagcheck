package net.kalars.tagcheck.actors

import akka.actor._

import net.kalars.tagcheck.io.IoUtils._
import net.kalars.tagcheck.rules.Checkers
import net.kalars.tagcheck.{FileSearch, DoneDir, FileResult, DirSearch, ScanResponse, ScanResponseLine}

/**
 * Scans a directory, starts a file scanner for audio files and new dir scanners for subdirectories.
 * Sends results to the "boss" and "done" to the parent.
 */
class DirScanner(boss:ActorRef, fileRegexp:String) extends Actor with ActorLogging {
  var fileCount= 0
  var dirCount= 0
  var dir= ""
  var fileScanner: ActorRef= _
  var results= List.empty[FileResult]

  override def receive = {
    case DirSearch(dirName) => // Starting point for a directory //////////////////////////////////
      dir= dirName
      fileScanner= context.actorOf(Props[FileScanner]) // Scanner for all files in this dir
      log.debug(s"Scanning directory $dir [${self.path}]")
      scan(dirName)  // Real work
      possiblyDone() // Could be an empty dir

    case msg:FileResult => // A file name with tags -- check & return /////////////////////////////
      fileCount -= 1
      results ::= Checkers.checkFile(msg)
      possiblyDone()

    case DoneDir(dirName) => // Subdir done ///////////////////////////////////////////////////////
      dirCount -= 1
      possiblyDone()
  }


  /** Terminate when all files and subdirs report "done" (and report recursively). */
  private def possiblyDone() {
    if (fileCount==0) {
      val lines = for {fileRes <- results if fileRes.warningLevel > 0
                       warn <- fileRes.warnings} yield
        ScanResponseLine(dir, fileRes.name, warn.level, warn.text)
      boss ! ScanResponse(dir, lines.toList)
      fileCount= -1
    }
    if (fileCount <=0 && dirCount==0 ) {
      log.debug(s"Done scanning directory $dir")
      context.parent ! DoneDir(dir)
      context.stop(self)
    }
  }

  private def scan(dirName: String) = {
    val (dirs, allFiles) = scanDir(dirName)
    dirCount+= dirs.size
    for (dir <- dirs)
      context.actorOf(Props(new DirScanner(boss, fileRegexp))) ! DirSearch(dir)
    val files = allFiles filter { _.toUpperCase.matches(fileRegexp) } // TODO use regexp flag rather than toupper
    for (file <- files) {
      fileCount += 1
      fileScanner ! FileSearch(file)
    }
  }
}
