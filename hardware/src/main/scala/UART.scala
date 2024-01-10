import chisel3._
import chisel3.util._
import utility.Constants.Global._
import utility.Constants.UART._
class FIFO(depth: Int = 32) extends Module{
  val io = IO(new Bundle{
    val din = Input(UInt(8.W))
    val dout = Output(UInt(8.W))
    val empty = Output(Bool())
    val full = Output(Bool())
    val we, re = Input(Bool())
  })
  val head = RegInit(0.U(log2Up(depth).W))
  val tail = RegInit(0.U(log2Up(depth).W))
  val bfr = Reg(Vec(depth,UInt(8.W)))
  io.empty := head === tail
  io.full := ((tail - head) === 1.U) || ((head - tail) === (depth-1).U)
  io.dout := bfr(tail)
  when(io.we && !io.full){
    bfr(head) := io.din
    head := Mux(head === (depth-1).U, 0.U, head + 1.U)
  }.elsewhen(io.re && !io.empty){
    tail := Mux(tail === (depth-1).U, 0.U, tail + 1.U)
  }
}
class UARTRX extends Module{
  val io = IO(new Bundle{
    val rx = Input(Bool())
    val data = Output(UInt(8.W))
    val empty = Output(Bool())
    val re = Input(Bool())
  })
  val fifo = Module(new FIFO)
  val CPB = (CLK_FREQ/BAUD - 1).toInt 
  val idle :: start :: process :: stop :: Nil = Enum(4)
  val state = RegInit(idle)
  val cntCPB = RegInit(0.U(log2Up(CPB).W))
  val cntBits = RegInit(0.U(3.W))
  val rxSync = RegNext(RegNext(io.rx, true.B), true.B)
  val shiftReg = RegInit(0.U(8.W))
  val dataRdy = RegInit(false.B)

  switch(state){
    is(idle){
      when(!rxSync){
        cntCPB := cntCPB + 1.U
        when(cntCPB === (CPB / 2).U){
          state := start
          cntCPB := 0.U
        }
      }
    }
    is(start){
      cntCPB := cntCPB + 1.U
      when(cntCPB === CPB.U){
        cntCPB := 0.U
        shiftReg := rxSync ## shiftReg(7,1)
        cntBits := cntBits + 1.U
        when(cntBits === 7.U){
          state := stop
        }
      }
    }
    is(stop){
      cntCPB := cntCPB + 1.U
      when(cntCPB === CPB.U){
        state := idle
        dataRdy := true.B
      }
    }
  }
  fifo.io.din := shiftReg
  fifo.io.we := false.B
  fifo.io.re := false.B
  io.data := fifo.io.dout
  io.empty := fifo.io.empty
  when(io.re && !fifo.io.empty){
    fifo.io.re := true.B
  }.elsewhen(dataRdy && !fifo.io.full){
    fifo.io.we := true.B
    dataRdy := false.B
  }
}
class UARTTX extends Module{
  val io = IO(new Bundle{
    val tx = Output(Bool())
    val data = Input(UInt(8.W))
    val full = Output(Bool())
    val we = Input(Bool())
  })
  val fifo = Module(new FIFO)
  val CPB = (CLK_FREQ/BAUD - 1).toInt
  val idle :: start :: process :: stop :: Nil = Enum(4)
  val state = RegInit(idle)
  val cntCPB = RegInit(0.U(log2Up(CPB).W))
  val cntBits = RegInit(0.U(3.W))
  val shiftReg = RegInit(0.U(8.W))
  val beginTx = WireDefault(false.B)

  io.tx := true.B
  switch(state){
    is(idle){
      cntCPB := 0.U
      when(beginTx){
        state := start
        shiftReg := fifo.io.dout
      }
    }
    is(start){
      io.tx := false.B
      cntCPB := cntCPB + 1.U
      when(cntCPB === CPB.U){
        state := process
        cntCPB := 0.U
      }
    }
    is(process){
      cntCPB := cntCPB + 1.U
      io.tx := shiftReg(0)
      when(cntCPB === CPB.U){
        cntCPB := 0.U
        cntBits := cntBits + 1.U
        shiftReg := 0.U(1.W) ## shiftReg(7,1)
        when(cntBits === 7.U){
          state := stop
        }
      }
    }
    is(stop){
      cntCPB := cntCPB + 1.U
      when(cntCPB === CPB.U){
        state := idle
      }
    }
  }
  fifo.io.din := io.data
  fifo.io.we := false.B
  fifo.io.re := false.B
  io.full := fifo.io.full
  when(io.we && !fifo.io.full){
    fifo.io.we := true.B
  }.elsewhen(state === idle && !fifo.io.empty){
    fifo.io.re := true.B
    beginTx := true.B
  }
}
class UART extends Module{
  val io = IO(new Bundle{
    val rx = Input(Bool())
    val tx = Output(Bool())
    val din = Input(UInt(8.W))
    val dout = Output(UInt(8.W))
    val empty = Output(Bool())
    val full = Output(Bool())
    val we, re = Input(Bool())
  })
  val rx = Module(new UARTRX)
  val tx = Module(new UARTTX)

  rx.io.rx := io.rx
  rx.io.re := io.re

  tx.io.we := io.we
  tx.io.data := io.din

  io.tx := tx.io.tx
  io.empty := rx.io.empty
  io.full := tx.io.full
  io.dout := rx.io.data

}