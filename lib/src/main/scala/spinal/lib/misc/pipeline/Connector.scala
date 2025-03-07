package spinal.lib.misc.pipeline

import spinal.core._
import scala.collection.Seq

object Connector{
  def connectDatas(up: Node, down: Node): Unit = {
    val matches = down.fromUp.payload.intersect(up.fromDown.payload)
    for (m <- matches) down(m) := up(m)
  }

  def connectDatasWithSwap(up: Node, down: Node, swap : scala.collection.Map[SignalKey[_ <: Data], SignalKey[_ <: Data]]): Unit = {
    val matches = down.fromUp.payload.intersect(up.fromDown.payload).filter(e => !swap.contains(e.tpe))
    for (m <- matches) down(m) := up(m)
    for((to, from) <- swap) down(to.asInstanceOf[SignalKey[Data]]) := up(from.asInstanceOf[SignalKey[Data]])
  }
}

trait Connector extends Area{
  def ups : Seq[Node]
  def downs : Seq[Node]

  def nodeSetup() : Unit = {}
  def propagateDown(): Unit
  def propagateUp(): Unit
  def build() : Unit

  def propagateDownAll(): Unit = {
    for(up <- ups; down <- downs) {
      down.fromUp.payload ++= up.fromUp.payload
      down.fromUp.payload ++= up.keyToData.keys
    }
  }
  def propagateUpAll(): Unit = {
    for (up <- ups; down <- downs) {
      up.fromDown.payload ++= down.fromDown.payload
      up.fromDown.payload ++= down.keyToData.keys
    }
  }


}