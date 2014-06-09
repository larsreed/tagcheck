package net.kalars.tagcheck.naive

import net.kalars.tagcheck.{FileResult, ScanResponseLine}
import scala.collection.immutable.Stack
import net.kalars.tagcheck.io.IoUtils
import net.kalars.tagcheck.tags.Checkers

/** A Non-threaded straight-forward implementation. */
object TagChecker {

  def naiveRun(fileRegexp: String, maxDepth: Int, dirs: List[String]
                )(report: ScanResponseLine => Unit) {
    var remaining: Stack[String]= Stack(dirs:_*)
    while (!remaining.isEmpty) {
      val (dir, rest)= remaining.pop2
      // println(s"Checking $dir...")
      remaining= rest
      val (dirs, files)= IoUtils.scanDir(dir)
      remaining= remaining.pushAll(dirs)
      val results= for (f<-files if f.toUpperCase.matches(fileRegexp)) yield
        Checkers.checkFile(FileResult(f, IoUtils.extractTags(f), List.empty))
      for (f<-results if f.warningLevel>0;
           w<-f.warnings) report(ScanResponseLine(dir, f.name, w.level, w.text))
    }
  }
}
