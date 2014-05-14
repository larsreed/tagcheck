package net.kalars.tagcheck.actors

import net.kalars.tagcheck.io.IoUtils

/*
Message passing:
1. -> TagCheckBoss: ScanRequest (start dirs)
2.  TagCheckBoss -> DirScanner: DirSearch (dir)
3a. DirScanner -> DirScanner: DirSearch (dir)
3b. DirScanner -> FileScanner: FileSearch (dir)
3c. DirScanner -> TagCheckBoss: FoundDir (dir)
4.  FileScanner -> DirScanner: FileResult (file, meta data)
5.  DirScanner -> DirChecker: DirResult (dir, FileResults)
6.  DirChecker -> TagCheckBoss: DirResult (dir, FileResults)
7.  TagCheckBoss -> : ScanResponse (n*ScanResponseLine = dir, file, level, warning)
 */

/** What we want to scan. */
case class ScanRequest(maxLevels: Int, dirList:List[String])
/** A result line. */
case class ScanResponseLine(dir: String, file:String, level: Int, warning: String)
/** The total result. */
case class ScanResponse(dirs:List[ScanResponseLine])

/** Notification of searching in another (aub)dir. */
case class FoundDir(dir:String)
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
/** Result of DirSearch. */
case class DirResult(name:String, files:List[FileResult]) {
  lazy val warningLevel= files.foldLeft(0) {
      (max: Int, res: FileResult) => math.max(max, res.warningLevel)
  }
  lazy val codeName= IoUtils.washName(name)
  def warnings: List[ScanResponseLine]= files.flatMap { file =>
    file.warnings.map { w=> ScanResponseLine(name, file.name, w.level, w.text) }
  }
}
