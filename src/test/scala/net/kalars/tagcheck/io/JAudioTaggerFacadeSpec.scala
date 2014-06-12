package net.kalars.tagcheck.io

import org.scalatest.FlatSpec
import JAudioTaggerFacade._

/** Spec of  JAudioTaggerFacade. */
class JAudioTaggerFacadeSpec extends FlatSpec {
  "A tag check" should "handle missing files as empty" in {
    val tags=extractTags("noSuchFile.mp3")
    assert(tags.size===1, tags)
  }

  it should "handle untagged files" in {
    val tags=extractTags("src/test/resources/fileTagTest/notags/heroes.mp3")
    assert(tags.size===0, tags)
  }

  it should "handle happy path for MP3" in {
    val tags=extractTags("src/test/resources/fileTagTest/anArtist/albumok/heroes.mp3")
    assert(tags.size>=IoUtils.TagCount, tags)
  }

  it should "handle happy path for WMA" in {
    val tags=extractTags("src/test/resources/fileTagTest/anArtist/albumOK/eclipse.wma")
    assert(tags.size>=IoUtils.TagCount, tags)
  }
}
