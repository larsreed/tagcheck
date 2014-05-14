package net.kalars.tagcheck

import akka.actor.{Actor, Props, ActorSystem}
import net.kalars.tagcheck.actors.{ScanResponseLine, ScanResponse, ScanRequest, TagCheckBoss}

/** Main program for the tag checker. */
class TagCheck  extends Actor {
  def printLine(line: ScanResponseLine) { println(s"${line.level}\t${line.file}: ${line.warning}") }

  override def receive = {
    case line: ScanResponseLine =>
      printLine(line)
    case res: ScanResponse =>
      for (line<-res.dirs) printLine(line)
      context.stop(self)
      Thread.sleep(1000)
      context.system.shutdown
  }
}

object TagCheck{
  def main(args: Array[String]) {
    var dirs: List[String]= List.empty
    var immediateResponse= false
    var maxDepth=1000
    for (arg <- args) arg match {
      case "^[0-9]+$" => maxDepth= arg.toInt
      case "^!!$" => immediateResponse= true
      case "^[-]+h(elp)?$" => println("""Usage: tagcheck [!!] [nn] dir [...]
                                      |!!: immediate feedback
                                      |nn: max directory depth
                                      |dir(s): where to search"""".stripMargin)
    }
    val fileRegexp= ".+[.](WMA|MP3)$"
    val system = ActorSystem("Main")
    val reportTo= system.actorOf(Props[TagCheck])
    dirs ::= "N:\\mp3\\David Bowie"
    val ac = system.actorOf(Props(new TagCheckBoss(reportTo, immediateResponse, fileRegexp)))
    ac ! ScanRequest(maxDepth, dirs)
  }
}
