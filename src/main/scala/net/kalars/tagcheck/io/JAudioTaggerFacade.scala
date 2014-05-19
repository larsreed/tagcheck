package net.kalars.tagcheck.io

import scala.collection.JavaConversions._
import java.io.File

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

/** Interface to JAudioTagger. */
object JAudioTaggerFacade {

  protected[io] val TitleTag= FieldKey.TITLE
  protected[io] val ArtistTag= FieldKey.ARTIST
  protected[io] val ArtistTag2= FieldKey.ALBUM_ARTIST
  protected[io] val AlbumTag= FieldKey.ALBUM
  protected[io] val ErrorTag= "Error"
  protected[io] val AllTags= List(TitleTag, ArtistTag, AlbumTag)

  def extractTags(file:String): Map[String, String]= {
    def fixValue(s:String): String= s.replaceAll("Text=.", "").replaceAll("\"; *$", "")
    try {
      val audioFile = AudioFileIO.read(new File(file))
      val tags= audioFile.getTag
      val mappings = for {key <- AllTags
                          tag <- tags.getFields(key)}
         yield (key.toString, fixValue(tag.toString))
      val res= mappings.toMap
      res get ArtistTag.toString match {
        case Some(_) => res
        case None =>
          val alt= tags.getFields(ArtistTag2)
          if (!alt.isEmpty)  res + (ArtistTag.toString -> fixValue(alt.toString))
          else res
      }
    }
    catch {
      case e:Exception => Map(ErrorTag -> e.toString)
    }
  }
}
