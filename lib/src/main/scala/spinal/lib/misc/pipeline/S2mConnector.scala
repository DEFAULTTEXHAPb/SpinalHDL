package spinal.lib.misc.pipeline

import spinal.core._


object S2mConnector{
  def apply(up : Node, down : Node) = new S2mConnector(up, down)
}

class S2mConnector(val up : Node, val down : Node) extends Connector {
  down.up = this
  up.down = this

  override def ups: Seq[Node] = List(up)
  override def downs: Seq[Node] = List(down)

  override def propagateDown(): Unit = {
    propagateDownAll()
    down.ctrl.forgetOneSupported = true
  }
  override def propagateUp(): Unit = {
    propagateUpAll()
    if (down.ctrl.forgetOne.nonEmpty) up.ctrl.forgetOneCreate()
    if (down.ctrl.cancel.nonEmpty) up.ctrl.cancelCreate()
  }

  override def build(): Unit = {
    val matches = down.fromUp.payload.intersect(up.fromDown.payload)

    val rValid = RegInit(False) setWhen (up.valid) clearWhen (down.ready) setCompositeName(this, "rValid")
    val rData = matches.map(e => RegNextWhen(up(e), up.ready).setCompositeName(this, "s2mBuffer"))

    up.ready := !rValid

    down.valid := up.valid || rValid
    when(rValid) {
      (matches, rData).zipped.foreach(down(_) := _)
    } otherwise {
      matches.foreach(e => down(e) := up(e))
    }

    down.ctrl.forgetOne.foreach { cond =>
      rValid clearWhen(cond)
      up.ctrl.forgetOne.get := cond && !rValid
    }
    down.ctrl.cancel.foreach { cond =>
      up.ctrl.cancel.get := cond && !rValid
    }
  }
}