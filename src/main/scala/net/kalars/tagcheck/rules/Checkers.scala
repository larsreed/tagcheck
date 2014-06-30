package net.kalars.tagcheck.rules

import net.kalars.tagcheck.io.IoUtils._
import net.kalars.tagcheck.{FileResult, Warning}

/** Public entry point. */
object Checkers {
  private val checkers=
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

sealed trait FileCheckerImpl {
  self: FileChecker =>

  def noTag(tag: String, meta: Map[String, String], warnings: List[Warning]) =
    Warning(Warning.ReallyBad, s"Missing $tag tag") :: warnings

  def tagOK(tag: String, file: FileResult, level: Int, tagText:String,
            meta: Map[String, String], warnings: List[Warning]) =
    if (file.codeName.contains(washName(tagText))) warnings
    else Warning(level, s"$tag '$tagText' does not match file name") :: warnings

  /** Standard implementation of verifying a single tag. */
  def tagChecker(file: FileResult, tag: String, level:Int) = {
    def checkTag(meta: Map[String, String], warnings: List[Warning]) = {
      meta.get(tag) match {
        case Some(tagText) =>
          tagOK(tag, file, level, tagText, meta, warnings)
        case None =>
          noTag(tag, meta, warnings)
      }
    }
    file.copy(warnings = checkTag(file.tags, file.warnings))
  }
}
/** Check for artist metadata. */
class ArtistChecker extends FileChecker with FileCheckerImpl {

  override def noTag(tag: String, meta: Map[String, String], warnings: List[Warning]) = {
    meta get AlbumArtist match {
      case Some(tag2) =>
        Warning(Warning.BadArtist, s"Missing $tag tag (but has album artist $tag2)") :: warnings
      case None =>
        super.noTag(tag, meta, warnings)
    }
  }

  override def tagOK(tag: String, file: FileResult, level: Int, tagText:String,
                     meta: Map[String, String], warnings: List[Warning]) =
      if (file.codeName.contains(washName(tagText)))
        meta get AlbumArtist match {
          case Some(tag2) if tag2 != tagText =>
            Warning(Warning.ArtistMismatch,
              s"Track artist $tag does not match album artist $tag2)") :: warnings
          case _ =>
            warnings
      }
      else super.tagOK(tag, file, level, tagText, meta, warnings)


  override def check(file: FileResult): FileResult = tagChecker(file, TrackArtist, Warning.MaybeBad)
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
