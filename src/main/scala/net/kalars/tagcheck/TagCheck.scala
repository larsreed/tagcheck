package net.kalars.tagcheck

import net.kalars.tagcheck.actors._

/** Main program for the tag checker. */

object TagCheck{
  def main(args: Array[String]) {
    var dirs: List[String]= List.empty
    var immediateResponse= false
    var maxDepth=1000
    for (arg <- args)  {
      if (arg.matches("^-[0-9]+$")) maxDepth= arg.toInt
      else if (arg.matches("^!!$"))immediateResponse= true
      else if (arg.matches("^[-]+h(elp)?$")) {
        println("""Usage: tagcheck [!!] [nn] dir [...]
                  |!!: immediate feedback
                  |nn: max directory depth
                  |dir(s): where to search""".stripMargin)
        return
      }
      else dirs ::= arg
    }
    val fileRegexp= ".+[.](WMA|MP3)$"
    TagCheckActor.actorRun(immediateResponse, fileRegexp, maxDepth, dirs)
  }
}
