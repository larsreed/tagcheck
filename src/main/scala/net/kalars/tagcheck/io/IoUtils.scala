package net.kalars.tagcheck.io

import java.io.File
import JAudioTaggerFacade.{TitleTag, ArtistTag, AlbumTag, AllTags}
import net.kalars.tagcheck.io

/** General IO routines. */
object IoUtils {
  val Title= TitleTag.toString
  val Artist= ArtistTag.toString
  val Album= AlbumTag.toString
  val ErrorTag= JAudioTaggerFacade.ErrorTag
  val TagCount= AllTags.size

  /** Scan a directory, return list of dirs & list of files. */
  def scanDir(dir:String): (Seq[String], Seq[String]) = {
    val allEntries= new File(dir).listFiles()
    val all= if (allEntries==null) Array.empty[File] else allEntries
    (
      for (file <- all if file.isDirectory) yield file.getCanonicalPath,
      for (file <- all if file.isFile) yield file.getCanonicalPath
    )
  }

  /** What we compare in tags. ASCII uppercase, not A or THE as words*/
  def washName(name:String): String=
    name.toUpperCase
        .replaceAll("\\bA\\b", "")
        .replaceAll("\\bTHE\\b", "")
        .replaceAll("[^A-Z]", "")

  /** Extract tags from file. */
  def extractTags(file:String): Map[String, String] = io.JAudioTaggerFacade.extractTags(file)
}
