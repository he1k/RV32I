import chisel3._
import cpu._
import memory._
class PipedRV(dir: String) extends Module{
  val io = IO(new Bundle{
    val regFile = Output(Vec(32, SInt(32.W)))
    val ecll = Output(Bool())
  })

  val cpu = Module(new CPU)
  val ram = Module(new RAM)
  val rom = Module(new ROM(dir))
  io.regFile <> cpu.io.regFile
  io.ecll := cpu.io.ecll
  ram.io.we := cpu.io.we
  ram.io.din := cpu.io.doutD
  ram.io.sign := cpu.io.sign
  ram.io.bytes := cpu.io.bytes
  ram.io.addr := cpu.io.addrD
  cpu.io.dinD := ram.io.dout
  rom.io.addr := cpu.io.addrI
  cpu.io.dinI := rom.io.dout
}
