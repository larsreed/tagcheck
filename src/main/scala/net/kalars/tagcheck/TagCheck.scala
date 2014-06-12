package net.kalars.tagcheck

import net.kalars.tagcheck.naive.TagChecker
import net.kalars.tagcheck.actors.TagCheckActor
import net.kalars.tagcheck.threads.DispatcherWorker

/** Main program for the tag checker. */
object TagCheck{
  def main(args: Array[String]) {
    def help() = println("""Usage: tagcheck A|N|T dir [...]
                           |A or N or T: mode (Actor/Naive/Tread)
                           |dir(s): where to search""".stripMargin)

    val fileRegexp= ".+[.](WMA|MP3)$"
    var dirs: List[String]= List.empty
    var runMode: RunMode= ActorMode
    val argsList= args.toList

    if (argsList.size<2)  return help()
    argsList(0).toUpperCase match {
      case "A" => runMode=ActorMode
      case "N" => runMode= NaiveMode
      case "T" => runMode=ThreadMode
      case _ => return help()
    }
    for (arg <- argsList.tail)
      if (arg.matches("^[-]+h(elp)?$"))  return help()
      else dirs ::= arg

    if (runMode == ActorMode)       TagCheckActor.actorRun(fileRegexp, dirs)
    else if (runMode == ThreadMode) new DispatcherWorker().runThreads(fileRegexp, dirs)
    else if (runMode==NaiveMode)    TagChecker.naiveRun(fileRegexp, dirs)
  }
}

private sealed trait RunMode
private case object ActorMode extends RunMode
private case object NaiveMode extends RunMode
private case object ThreadMode extends RunMode
