package net.kalars.tagcheck.io

import java.io.File
import JAudioTaggerFacade.{TitleTag, TrackArtistTag, AlbumArtistTag, AlbumTag, AllTags}

/** General IO routines. */
object IoUtils {
  val Title= TitleTag.toString
  val TrackArtist= TrackArtistTag.toString
  val AlbumArtist= AlbumArtistTag.toString
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
        .replaceAll("[-_]", " ")
        .replaceAll("\\bA\\b", "")
        .replaceAll("\\bVOL[.]", "")
        .replaceAll("\\bVOLUME\\b", "")
        .replaceAll("\\bCD[ ]*[0-9]+[ ]+OF[ ]*[0-9]+", "")
        .replaceAll("\\bCD[ ]*[0-9]+", "")
        .replaceAll("\\bDISK[ ]*[0-9]+[ ]+OF[ ]*[0-9]+", "")
        .replaceAll("\\bDISK[ ]*[0-9]+", "")
        .replaceAll("\\bTHE\\b", "")
        .replaceAll("\\bREMASTERED\\b", "")
        .replaceAll("\\bREMIX\\b", "")
        .replaceAll("[^A-Z]", "")

  /** Extract tags from file. */
  def extractTags(file:String): Map[String, String] = JAudioTaggerFacade.extractTags(file)
}
