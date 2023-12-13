package cpu

import chisel3._
class RegisterFile extends Module{
  val io = IO(new Bundle{
    val we = Input(Bool())
    val rd = Input(UInt(5.W))
    val din = Input(SInt(32.W))
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val reg1 = Output(SInt(32.W))
    val reg2 = Output(SInt(32.W))
    val regFile = Output(Vec(32, SInt(32.W)))
  })

  val regFile = RegInit(VecInit(Seq.fill(32)(0.S(32.W))))
  io.reg1 := regFile(io.rs1)
  io.reg2 := regFile(io.rs2)
  when(io.we && io.rd =/= 0.U){
    regFile(io.rd) := io.din
  }
  io.regFile <> regFile
}
