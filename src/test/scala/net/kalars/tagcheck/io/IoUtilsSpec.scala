package net.kalars.tagcheck.io

import org.scalatest.FlatSpec
import IoUtils._

/** Spec of IoUtils. */
class IoUtilsSpec extends FlatSpec {
  val dir= "src/test/resources/scanDirTest"

  "scanDir" should "return empty lists on non-existing directory" in {
    val (dirs,files)= scanDir("fooFight")
    assert(dirs.size===0, "dirs")
    assert(files.size===0, "files")
  }

  it should "find files and subdirectories" in {
    val (dirs,files)= scanDir(dir)
    assert(dirs.size===1, "dirs")
    assert(files.size===2, "files")
  }

  it should "return full path names" in {
    val (dirs,files)= scanDir(dir)
    val all= dirs ++ files
    all.foreach(p=> assert(p.matches(".*" + dir.replaceAll("/", ".") + ".*"), p + " contains"))
    all.foreach(p=> assert(!p.startsWith("src"), p + " startswith"))
  }

  "washNames" should "wash empty names" in {
    assert(washName("")==="")
  }

  it should "remove A from all positions when a single word" in {
    assert(washName("A long way home")==="LONGWAYHOME")
    assert(washName("Like a banana")==="LIKEBANANA")
  }

  it should "remove THE from all positions when a single word" in {
    assert(washName("the long way home")==="LONGWAYHOME")
    assert(washName("The The")==="")
    assert(washName("Like the banana")==="LIKEBANANA")
  }

  it should "remove all non-A-Z-characters" in {
    assert(washName("Well,\tAll's Well That    End's Well?\n123 Monkeys wouldn't write that!")
      ==="WELLALLSWELLTHATENDSWELLMONKEYSWOULDNTWRITETHAT")
    assert(washName("æøåNORSKÆØÅ$")==="NORSK")
  }
}
