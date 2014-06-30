package net.kalars.tagcheck.io

import scala.collection.JavaConversions._
import java.io.File

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

/** Interface to JAudioTagger. */
object JAudioTaggerFacade {

  protected[io] val TitleTag= FieldKey.TITLE
  protected[io] val TrackArtistTag= FieldKey.ARTIST
  protected[io] val AlbumArtistTag= FieldKey.ALBUM_ARTIST
  protected[io] val AlbumTag= FieldKey.ALBUM
  protected[io] val ErrorTag= "Error"
  protected[io] val AllTags= List(TitleTag, TrackArtistTag, AlbumArtistTag, AlbumTag)

  /** Retrieve selected tags from given file. */
  def extractTags(file:String): Map[String, String]= {
    // Canonicalize string format
    def fixValue(s:String): String= s.replaceAll("Text=.", "").replaceAll("\"; *$", "")

    try {
      val audioFile = AudioFileIO.read(new File(file))
      val tags= audioFile.getTag
      val mappings = for {key <- AllTags
                          tag <- tags.getFields(key)}
         yield (key.toString, fixValue(tag.toString))
      mappings.toMap
    }
    catch {
      case e:Exception => Map(ErrorTag -> e.toString)
    }
  }
}
