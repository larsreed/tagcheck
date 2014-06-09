package net.kalars.tagcheck.tags

import net.kalars.tagcheck.io.IoUtils._
import net.kalars.tagcheck.{FileResult, Warning}

/** The checker classes. */
object Checkers {
  val checkers: List[FileChecker]=
      List(new ArtistChecker,
           new AlbumChecker,
           new TitleChecker,
           new ErrorChecker)
  def checkFile(file:FileResult): FileResult =
    checkers.foldLeft(file){ (res, checker) => checker.check(res)}
}

/** Interface for checking of files. */
trait FileChecker {
  /** Do the check, return result augmented by new warnings. */
  def check(file: FileResult): FileResult
}

trait FileCheckerImpl {
  self: FileChecker =>

  def tagChecker(file: FileResult, tag: String, level:Int) = {
    def checkTag(meta: Map[String, String], w: List[Warning]) = {
      // println(s"Checking $tag in ${file.name}")
      meta get tag match {
        case Some(tagText) =>
          if (file.codeName.contains(washName(tagText))) w
          else Warning(level, s"$tag '$tagText' does not match file name") :: w
        case None =>
          Warning(Warning.ReallyBad, s"Missing $tag tag") :: w
      }
    }
    val warnings = checkTag(file.tags, file.warnings)
    file.copy(warnings = warnings)
  }
}
/** Check for artist metadata. */
class ArtistChecker extends FileChecker with FileCheckerImpl {
  override def check(file: FileResult): FileResult = tagChecker(file, Artist, Warning.MaybeBad)
}

/** Check for title metadata. */
class TitleChecker extends FileChecker  with FileCheckerImpl {
  override def check(file: FileResult): FileResult = tagChecker(file, Title, Warning.Bad)
}

/** Check for album metadata. */
class AlbumChecker extends FileChecker  with FileCheckerImpl {
  override def check(file: FileResult): FileResult = tagChecker(file, Album, Warning.Bad)
}

/** Checks for Error tags. */
class ErrorChecker extends FileChecker {
  override def check(file: FileResult): FileResult = {
    file.tags.get(ErrorTag) match {
      case Some(s) => file.copy(warnings=Warning(Warning.Error, s) :: file.warnings)
      case None => file
    }
  }
}
