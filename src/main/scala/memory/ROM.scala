package memory
import chisel3._
import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}

class ROM(dir: String) extends Module{
  val io = IO(new Bundle{
    val addr = Input(UInt(12.W))
    val dout = Output(UInt(32.W))
  })

  val byte : Array[Byte] = Files.readAllBytes(Paths.get(dir +".bin"))
  val rom = VecInit(byte.grouped(4).map(b =>ByteBuffer.wrap(b.reverse).getInt).toArray.map(_.S(32.W)))
  io.dout := rom(io.addr).asUInt()
}