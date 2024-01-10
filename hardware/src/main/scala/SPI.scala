import chisel3._
import chisel3.util._
import chisel3.experimental._
import utility.Constants.Global._
import utility.Constants.SPI._



class SPIPins extends Bundle(){
  val CE = Output(Bool())
  val MOSI = Output(Bool())
  val MISO = Input(Bool())
  val SCLK = Output(Bool())
}
class SPIBridge extends Bundle(){
  val we, valid = Input(Bool())
  val ready = Output(Bool())
  val din = Input(UInt(32.W))
  val dout = Output(UInt(32.W))
  val addr = Input(UInt(24.W))
}
class SPI extends Module(){
    val io = IO(new Bundle{
        val spi = new SPIPins
        val ctrl = new SPIBridge
    })
    val init :: rst :: cedelay :: idle :: ctrltx :: write :: read :: ack ::  Nil = Enum(8)
    val state = RegInit(init)
    val statenext = RegInit(init)
    val CNT_MAX_TPU : Int = ((CLK_FREQ*TPU).toInt-1)
    val CNT_MAX_SCLK : Int = ((CLK_FREQ/SCLK_FREQ)/4).toInt
    val cnt = RegInit(0.U(log2Up(CNT_MAX_TPU).W))
    val cntbits = RegInit(0.U(3.W))
    val cntbytes = RegInit(0.U(4.W))
    val sclk = RegInit(false.B)
    val cntsclk = RegInit(0.U(log2Up(CNT_MAX_SCLK).W))
    val ensclk = WireDefault(false.B)
    val rising = WireDefault(false.B)
    val falling = WireDefault(false.B) 
    val clrsclk = WireDefault(false.B)
    val data = RegInit((RST_EN_CMD.U(8.W) ## RST_CMD.U(8.W) ## 0.U(16.W) ## 0.U(32.W)))
    io.spi.CE := true.B
    io.spi.SCLK := Mux(ensclk, sclk, false.B)
    io.spi.MOSI := false.B
    io.ctrl.ready := false.B
    io.ctrl.dout := data(7,0) ## data(15,8) ## data(23,16) ## data(31,24)
    when(clrsclk){
      cntsclk := 0.U
      sclk := false.B
    }.elsewhen(cntsclk === CNT_MAX_SCLK.U){
      cntsclk := 0.U
      sclk := ~sclk
      rising := !sclk
      falling := sclk
    }.otherwise{
      cntsclk := cntsclk + 1.U
    }
    cnt := cnt + 1.U
    switch(state){
      is(init){
        when(cnt === CNT_MAX_TPU.U){
          state := rst
          clrsclk := true.B
        }
      }
      is(rst){
        io.spi.MOSI := data(63)
        io.spi.CE := false.B
        ensclk := true.B
        when(falling){
          data := data(62,0) ## 0.U(1.W) 
          cntbits := cntbits + 1.U
          when(cntbits === 7.U){
            cntbits := 0.U
            state := cedelay
            statenext := Mux(data(62,0) === 0.U, idle, rst)
          } 
        }
      }
      is(cedelay){
        when(falling){
          cntbits := cntbits + 1.U
          when(cntbits === 3.U){
            cntbits := 0.U
            state := statenext
          }
        }
      }
      is(idle){
        when(io.ctrl.valid){
          when(io.ctrl.we){
            data := WRITE_CMD.U(8.W) ## io.ctrl.addr ## io.ctrl.din(7,0) ## io.ctrl.din(15,8) ## io.ctrl.din(23,16) ## io.ctrl.din(31,24) 
            statenext := write
          }.otherwise{
            data := READ_CMD.U(8.W) ## io.ctrl.addr ## io.ctrl.din(7,0) ## io.ctrl.din(15,8) ## io.ctrl.din(23,16) ## io.ctrl.din(31,24)
            statenext := read
          }
          clrsclk := true.B
          state := ctrltx
        }
      }
      is(ctrltx){
        io.spi.MOSI := data(63)
        io.spi.CE := false.B
        ensclk := true.B
        when(falling){
          data := data(62,0) ## 0.U(1.W) 
          cntbits := cntbits + 1.U
          when(cntbits === 7.U){
            cntbytes := cntbytes + 1.U
            when(cntbytes === 3.U){
              state := statenext

            }
          } 
        }
      }
      is(write){
        io.spi.MOSI := data(63)
        io.spi.CE := false.B
        ensclk := true.B
        when(falling){
          data := data(62,0) ## 0.U(1.W) 
          cntbits := cntbits + 1.U
          when(cntbits === 7.U){
            cntbytes := cntbytes + 1.U
            when(cntbytes === 7.U){
              state := ack
            }
          } 
        }
      }
      is(read){
        io.spi.CE := false.B
        ensclk := true.B
        when(rising){
          data := data(62,0) ## io.spi.MISO
          cntbits := cntbits + 1.U
          when(cntbits === 7.U){
            cntbytes := cntbytes + 1.U
            when(cntbytes === 7.U){
              state := ack
            }
          } 
        }
      }
      is(ack){
        io.ctrl.ready := true.B
        cntbytes := 0.U
        statenext := idle
        when(!io.ctrl.valid){
          state := cedelay
        }
      }
    }
}
object SPIGen extends App{
  (new chisel3.stage.ChiselStage).emitVerilog(new SPI,args)
}