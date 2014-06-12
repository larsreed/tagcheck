package net.kalars.tagcheck.threads

import net.kalars.tagcheck._
import net.kalars.tagcheck.io.IoUtils
import net.kalars.tagcheck.rules.Checkers
import net.kalars.tagcheck.{FileSearch, FileResult, ScanResponseLine, DirSearch}
import java.util.concurrent.{ConcurrentHashMap, TimeUnit, LinkedBlockingQueue}

class MsgQueue[T](qName:String, taskSet: ConcurrentHashMap[Any, Int]) {

  private val q= new LinkedBlockingQueue[T]()

  /** Add a message to the queue. */
  def put(msg: T) {
    taskSet.get(msg) match {
      case i:Int => taskSet.put(msg, i+1)
      case _ =>  taskSet.put(msg, 1)
    }
    q.put(msg)
  }

  /** Retrieve a message from the queue. */
  def get: Option[T] = {
    while (true) {
      if (taskSet.isEmpty) return None
      val msg = q.poll(100, TimeUnit.MILLISECONDS)
      if (msg != null) return Some(msg)
    }
    None
  }
}


/** The main worker. */
class DispatcherWorker {

  // No. of respective threads
  private val DirWorkers= 2
  private val FileScanWorkers= 4
  private val FileCheckWorkers= 1

  // Need to control what's left to do, no natural stopping point
  private val taskSet= new ConcurrentHashMap[Any, Int] // Should have been a MultiSet...
  private val dirScanQueue= new MsgQueue[DirSearch]("DirScan", taskSet)
  private val fileScanQueue= new MsgQueue[FileSearch]("FileScan", taskSet)
  private val fileCheckQueue= new MsgQueue[FileResult]("FileCheck", taskSet)
  private val resultQueue= new MsgQueue[ScanResponseLine]("Results", taskSet)

  def runThreads(fileRegexp: String, dirs: List[String]) {
    for (dir<- dirs) dirScanQueue.put(DirSearch(dir)) // Initial tasks
    // Start worker threads
    for (i<- 1 to DirWorkers)
      new Thread(new DirScanWorker(taskSet, dirScanQueue, fileScanQueue, fileRegexp), "dirWorker"+i).start()
    for (i<- 1 to FileScanWorkers)
      new Thread(new FileTagWorker(taskSet, fileScanQueue, fileCheckQueue), "fileScanWorker"+i).start()
    for (i<- 1 to FileCheckWorkers)
      new Thread(new FileCheckWorker(taskSet, fileCheckQueue, resultQueue), "fileCheckWorker"+i).start()

    var results= List.empty[ScanResponseLine]
    while (true) resultQueue.get match {
      case Some(line) =>
        results ::= line
        taskSet.remove(line)
      case None =>
        for (line<- ScanUtils.sortResults(results)) line.printLine()
        return
    }
  }
}

/** General worker. */
abstract class Worker[T](taskSet: ConcurrentHashMap[Any, Int], listen: MsgQueue[T]) extends Runnable {

  /** Handle one message. */
  def handle(msg:T): Unit

  /** Message loop. Read from queue until all tasks are done. */
  override def run(): Unit =
    while (true) listen.get match {
        case None => return
        case Some(msg) =>
          handle(msg)
          taskSet.get(msg) match {
            case i:Int if i>1 => taskSet.put(msg, i-1)
            case i:Int => taskSet.remove(msg)
          }
      }
}

/** Traverses directories. */
class DirScanWorker(taskSet: ConcurrentHashMap[Any, Int],
                    dirQ: MsgQueue[DirSearch],
                    fileQ: MsgQueue[FileSearch],
                    fileRegexp: String)
      extends Worker[DirSearch](taskSet, dirQ) {

  override def handle(dirMsg: DirSearch) {
    val (dirs, files)= IoUtils.scanDir(dirMsg.name)
    for (dir <- dirs) dirQ.put(DirSearch(dir))
    for (f<-files if f.toUpperCase.matches(fileRegexp)) fileQ.put(FileSearch(f))
  }
}

/** Retrieves file tags. */
class FileTagWorker(taskSet: ConcurrentHashMap[Any, Int],
                    requests: MsgQueue[FileSearch],
                    results: MsgQueue[FileResult])
       extends Worker[FileSearch](taskSet, requests) {

   override def handle(fileMsg: FileSearch) {
     results.put(FileResult(fileMsg.name, IoUtils.extractTags(fileMsg.name), List.empty))
   }
 }

/** Checks file tags against file name. */
class FileCheckWorker(taskSet: ConcurrentHashMap[Any, Int],
                      requests: MsgQueue[FileResult],
                      results: MsgQueue[ScanResponseLine])
       extends Worker[FileResult](taskSet, requests) {

   override def handle(fileMsg: FileResult) {
     val fileResult = Checkers.checkFile(fileMsg)
     if (fileResult.warningLevel>0) for (w<- fileResult.warnings)
       results.put(ScanResponseLine("", fileResult.name, w.level, w.text))
   }
}
