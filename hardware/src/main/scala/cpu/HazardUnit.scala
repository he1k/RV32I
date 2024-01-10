package cpu
import chisel3._
class HU_IDEX_IO extends Bundle{
  val memRE = Input(Bool())
  val rd = Input(UInt(5.W))
}

class HU_IFID_IO extends Bundle{
  val rs1 = Input(UInt(5.W))
  val rs2 = Input(UInt(5.W))
}
class HazardUnit extends Module{
  val io = IO(new Bundle{
    val idex = new HU_IDEX_IO
    val ifid = new HU_IFID_IO
    val stall = Output(Bool())
  })
  io.stall := false.B
  val hazard = io.idex.memRE && io.idex.rd =/= 0.U && (io.idex.rd === io.ifid.rs1 || io.idex.rd === io.ifid.rs2)
  when(hazard){
    io.stall := true.B
  }

}
