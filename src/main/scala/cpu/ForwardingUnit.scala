package cpu
import chisel3._

class FU_EXMEM extends Bundle{
  val regWE = Bool()
  val rd = UInt(5.W)
}
class FU_MEMWB extends Bundle{
  val regWE = Bool()
  val rd = UInt(5.W)
}
class FU_IDEX extends Bundle{
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
}
class FU_IFID extends Bundle{
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
}
class ForwardingUnit extends Module{
  val io = IO(new Bundle{
    val forwardEXA = Output(UInt(2.W))
    val forwardEXB = Output(UInt(2.W))
    val forwardIDA = Output(Bool())
    val forwardIDB = Output(Bool())
    val exmem = Input(new FU_EXMEM)
    val memwb = Input(new FU_MEMWB)
    val idex = Input(new FU_IDEX)
    val ifid = Input(new FU_IFID)
  })
  val exHazardA = io.exmem.regWE && io.exmem.rd =/= 0.U && io.exmem.rd === io.idex.rs1
  val exHazardB = io.exmem.regWE && io.exmem.rd =/= 0.U && io.exmem.rd === io.idex.rs2
  val memHazardA = !exHazardA && io.memwb.regWE && io.memwb.rd =/= 0.U && io.memwb.rd === io.idex.rs1
  val memHazardB = !exHazardB && io.memwb.regWE && io.memwb.rd =/= 0.U && io.memwb.rd === io.idex.rs2
  val idhazardA = io.memwb.regWE && io.memwb.rd =/= 0.U && io.memwb.rd === io.ifid.rs1
  val idhazardB = io.memwb.regWE && io.memwb.rd =/= 0.U && io.memwb.rd === io.ifid.rs2

  io.forwardEXA := 0.U
  when(memHazardA){ io.forwardEXA := 1.U }
  when(exHazardA){ io.forwardEXA := 2.U }
  io.forwardEXB := 0.U
  when(memHazardB){ io.forwardEXB := 1.U }
  when(exHazardB){ io.forwardEXB := 2.U }
  io.forwardIDA := idhazardA
  io.forwardIDB := idhazardB
}
