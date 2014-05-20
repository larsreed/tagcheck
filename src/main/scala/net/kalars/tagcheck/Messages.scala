package net.kalars.tagcheck

import net.kalars.tagcheck.io.IoUtils

// ALL MESSAGE TYPES

/*
Message passing:
1.  -> TagCheckBoss: ScanRequest (start dirs)
2.  TagCheckBoss -> DirScanner: DirSearch (dir)
3a. DirScanner -> DirScanner: DirSearch (dir)
3b. DirScanner -> FileScanner: FileSearch (dir)
4.  FileScanner -> DirScanner: FileResult (file, meta data)
5.  DirScanner -> TagCheckBoss: ScanResponse (n*ScanResponseLine = dir, file, level, warning)
6.  DirScanner -> TagCheckBoss / DirScanner: DoneDir(dir)
 */

/** What we want to scan. */
case class ScanRequest(maxLevels: Int, immediateResponse:Boolean, fileRegexp: String,
                       dirList:List[String])

/** A result line. */
case class ScanResponseLine(dir: String, file:String, level: Int, warning: String) {
  def printLine() { println(s"$level\t$file: $warning") }
}
/** The total result. */
case class ScanResponse(dirName:String, dirs:List[ScanResponseLine])

/** Notification of directory done. */
case class DoneDir(dir:String)
/** Search a directory. */
case class DirSearch (name: String, restLevel: Int)
/** Search a file. */
case class FileSearch(name: String)

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
case class FileResult(name:String, tags:Map[String, String], warnings:List[Warning]) {
  lazy val warningLevel= warnings.foldLeft(0) { (max:Int, w:Warning) => math.max(max, w.level) }
  lazy val codeName= IoUtils.washName(name)
}
