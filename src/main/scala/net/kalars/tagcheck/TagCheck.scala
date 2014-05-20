package net.kalars.tagcheck

import net.kalars.tagcheck.actors._

/** Main program for the tag checker. */

object TagCheck{
  def main(args: Array[String]) {
    def help() {
      println("""Usage: tagcheck A|N [!!] [nn] dir [...]
                |A or N: mode (Actor/Naive)
                |!!: immediate feedback
                |nn: max directory depth
                |dir(s): where to search""".stripMargin)
    }
    var dirs: List[String]= List.empty
    var immediateResponse= false
    var maxDepth=1000
    var runMode: RunMode= ActorMode
    val argsList= args.toList
    if (argsList.size<2) {
      help()
      return
    }
    argsList(0).toUpperCase match {
      case "A" => runMode=ActorMode
      case "N" => runMode= NaiveMode
      case _ =>
        help()
        return
    }
    for (arg <- argsList.tail)  {
      if (arg.matches("^-[0-9]+$")) maxDepth= arg.toInt
      else if (arg.matches("^!!$"))immediateResponse= true
      else if (arg.matches("^[-]+h(elp)?$")) {
        help()
        return
      }
      else dirs ::= arg
    }
    val fileRegexp= ".+[.](WMA|MP3)$"
    if (runMode == ActorMode) TagCheckActor.actorRun(immediateResponse, fileRegexp, maxDepth, dirs)
  }
}

private sealed trait RunMode
private case object ActorMode extends RunMode
private case object NaiveMode extends RunMode