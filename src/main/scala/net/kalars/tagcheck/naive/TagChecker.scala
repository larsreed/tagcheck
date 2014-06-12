package net.kalars.tagcheck.naive

import net.kalars.tagcheck.{FileResult, ScanResponseLine}
import scala.collection.immutable.Stack
import net.kalars.tagcheck.io.IoUtils
import net.kalars.tagcheck.rules.Checkers
import net.kalars.tagcheck.ScanUtils._
import net.kalars.tagcheck.FileResult
import net.kalars.tagcheck.ScanResponseLine

/** A non-threaded, straight forward implementation... */
object TagChecker {

  /** Entry point */
  def naiveRun(fileRegexp: String, dirs: List[String]) {

    var remaining: Stack[String]= Stack(dirs:_*)
    var res: List[ScanResponseLine]= List.empty

    while (!remaining.isEmpty) {
      val (dir, rest)= remaining.pop2
      val (dirs, files)= IoUtils.scanDir(dir)
      remaining= rest.pushAll(dirs)
      val results= for (f<-files if f.toUpperCase.matches(fileRegexp)) yield
        Checkers.checkFile(FileResult(f, IoUtils.extractTags(f), List.empty))
      for (f <- results if f.warningLevel>0;
           w <- f.warnings)
        res ::= ScanResponseLine(dir, f.name, w.level, w.text)
    }

    for (line <- sortResults(res)) line.printLine()
  }
}
