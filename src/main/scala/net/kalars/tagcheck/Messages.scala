package net.kalars.tagcheck

import net.kalars.tagcheck.io.IoUtils

// ALL MESSAGE TYPES

/** Things that may be queued. */
sealed trait Message

/** What we want to scan. */
case class ScanRequest(fileRegexp: String, dirList:List[String]) extends Message

/** A result line. */
case class ScanResponseLine(dir: String,
                            file:String,
                            level: Int,
                            warning: String) extends Message {
  def printLine() { println(s"$level\t$file: $warning") }
}

/** Helper routines. */
object ScanUtils {
  def sortResults(list: List[ScanResponseLine]): List[ScanResponseLine] = {
    list.sortWith { (a, b) =>
      if (a.level == b.level) a.file < b.file
      else a.level > b.level
    }
  }
}
/** The total result. */
case class ScanResponse(dirName: String, dirs: List[ScanResponseLine]) extends Message

/** Notification of directory done. */
case class DoneDir(dir: String) extends Message
/** Search a directory. */
case class DirSearch (name: String) extends Message
/** Search a file. */
case class FileSearch(name: String) extends Message

/** One warning on a file .*/
case class Warning(level: Int, text: String)
/** Warning levels. */
case object Warning {
  val Error= 99
  val ReallyBad= 90
  val Bad= 50
  val MaybeBad= 20
}
/** Result of FileSearch. */
case class FileResult(name:String,
                      tags:Map[String, String],
                      warnings:List[Warning]) extends Message {
  lazy val warningLevel= warnings.foldLeft(0) { (max:Int, w:Warning) => math.max(max, w.level) }
  lazy val codeName= IoUtils.washName(name)
}

