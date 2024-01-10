import chisel3._
import cpu._
import memory._
class PipedRV(dir: String) extends Module{
  val io = IO(new Bundle{
    val regFile = Output(Vec(32, SInt(32.W)))
    val ecll = Output(Bool())
    //val we = Output(Bool())
    //val dinD = Input(UInt(32.W))
    //val doutD = Output(UInt(32.W))
    //val addrD = Output(UInt(32.W))
  })

  val cpu = Module(new CPU)
  val ram = Module(new RAM)
  val rom = Module(new ROM(dir))
  io.regFile <> cpu.io.regFile
  io.ecll := cpu.io.ecll

  //io.doutD := cpu.io.doutD
  //io.we := cpu.io.we
  //io.addrD := cpu.io.addrD
  //cpu.io.dinD := io.dinD
  ram.io.we := cpu.io.we
  ram.io.din := cpu.io.doutD
  ram.io.sign := cpu.io.sign
  ram.io.bytes := cpu.io.bytes
  ram.io.addr := cpu.io.addrD
  cpu.io.dinD := ram.io.dout
  rom.io.addr := cpu.io.addrI
  cpu.io.dinI := rom.io.dout
}

object Main extends App{
  val dir = "C:\\Users\\farfar\\Desktop\\tests\\" + "addlarge"
  (new chisel3.stage.ChiselStage).emitVerilog(new PipedRV(dir),args)
}