package utility

import utility.Constants._
import chisel3._
import chisel3.util._

/*
object RAM {
    class RAMIO extends Bundle{
        val en = Input(Bool())
        val we = Input(Bool())
        val addr = Input(UInt(32.W))
        val din = Input(UInt(32.W))
        val dout = Output(UInt(32.W))
    }
    class RAMClientIO extends Bundle{
        val valid = Output(Bool())
        val we = Output(Bool())
        val dout = Output(UInt(32.W))
        val addr = Output(UInt(32.W))
        val ready = Input(Bool())
        val din = Input(UInt(32.W))
        val burst = Output(Bool())
    }   
}

object ICACHE{
    class ICacheIO extends Bundle{
        val ram = new RAM.RAMClientIO
        val cpu = new Bundle{
            val valid = Output(Bool())
            val addr = Input(UInt(32.W))
            val dout = Output(UInt(32.W))
        }
    }
}




object IFID{
    // Signals for the pipeline register
    class REG extends Bundle{
        val inst = UInt(32.W)
        val pc = UInt(32.W)
        def clr(): Unit ={
            inst := 0.U
            pc := 0.U
         }
    }
    val io = (new Bundle{
        val instr = (new Bundle{
            val in = Input(UInt(32.W))
            val out = Output(UInt(32.W))
        })
        val pc = Output(UInt(32.W))
    })
}

class IDEX extends Bundle{
  val rd = UInt(5.W)
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
  val aluop = UInt(3.W)
  val a = SInt(32.W)
  val b = SInt(32.W)
  val pc = UInt(32.W)
  val inst = UInt(32.W)
  val sely = UInt(2.W)
  val sela = Bool()
  val selb = UInt(2.W)
  val branch = UInt(3.W)
  val jump = UInt(2.W)
  val regWE = Bool()
  val memWE = Bool()
  val memRE = Bool()
  val bytes = UInt(2.W)
  val sign = Bool()
  val ecll = Bool()
  def clr(): Unit ={
    rd := 0.U
    rs1 := 0.U
    rs2 := 0.U
    aluop := 0.U
    a := 0.S
    b := 0.S
    pc := 0.U
    inst := 0.U
    sely := 0.U
    sela := false.B
    selb := 0.U
    branch := 0.U
    jump := 0.U
    regWE := false.B
    memWE := false.B
    memRE := false.B
    bytes := 0.U
    sign := false.B
    ecll := false.B
  }
}

class EXMEM extends Bundle{
  val rd = UInt(5.W)
  val regWE = Bool()
  val memWE = Bool()
  val memRE = Bool()
  val y = SInt(32.W)
  val b = SInt(32.W)
  val bytes = UInt(2.W)
  val sign = Bool()
  val ecll = Bool()
}

class MEMWB extends Bundle{
  val rd = UInt(5.W)
  val regWE = Bool()
  val data = SInt(32.W)
  val ecll = Bool()
}
*/