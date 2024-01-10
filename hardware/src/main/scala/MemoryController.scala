import chisel3._
import chisel3.util._
import chisel3.experimental._
import utility.Constants.UART._

class MemoryController extends Module{
    val io = IO(new Bundle{
        val spi = new SPIPins
        val led = Output(UInt(16.W))
        val upr = Input(Bool())
        val rx = Input(Bool())
        val tx = Output(Bool())
    })
    val spi = Module(new SPI)
    val uart = Module(new UART)
    val idle :: readcmd :: parsecmd :: readmem :: displaymem :: endline :: readdata :: writemem :: error :: sendaddr :: senddata :: Nil = Enum(11)
    //val idle :: readvalid :: readwe :: readaddr :: readdata :: mem :: writeback :: endline :: Nil = Enum(8)
    val state = RegInit(idle)
    val nextstate = RegInit(idle)
    val cmd = RegInit(0.U(32.W))
    val cntbytes = RegInit(0.U(2.W))

    val empt = RegInit(false.B)
    val full = RegInit(false.B)
    val valid = RegInit(0.U(8.W))
    val we = RegInit(0.U(8.W))
    val addr = RegInit(0.U(24.W))
    val data = RegInit(0.U(32.W))



    io.led := valid ## we
    io.spi.CE := spi.io.spi.CE
    io.spi.MOSI := spi.io.spi.MOSI
    io.spi.SCLK := spi.io.spi.SCLK
    spi.io.spi.MISO := io.spi.MISO
    spi.io.ctrl.valid := false.B
    spi.io.ctrl.we := false.B
    spi.io.ctrl.din := data // 0x12345678.U(32.W)
    spi.io.ctrl.addr := addr // 0x000000.U(32.W)

    uart.io.rx := io.rx
    io.tx := uart.io.tx
    uart.io.din := 0.U
    uart.io.we := false.B
    uart.io.re := false.B

    switch(state){
        /*
        is(idle){
            when(!uart.io.empty){
                state := readvalid
            }
        }
        is(readvalid){
            when(!uart.io.empty){
                uart.io.re := true.B
                valid := uart.io.dout
                state := readwe
            }
        }
        is(readwe){
            when(!uart.io.empty){
                uart.io.re := true.B
                we := uart.io.dout
                state := readaddr
            }
        }
        is(readaddr){
            when(!uart.io.empty){
                uart.io.re := true.B
                addr := uart.io.dout ## addr(23,8)
                cntbytes := cntbytes + 1.U
                when(cntbytes === 2.U){
                    cntbytes := 0.U
                    state := readdata
                }
            }
        }
        is(readdata){
            when(!uart.io.empty){
                uart.io.re := true.B
                data := uart.io.dout ## data(31,8)
                cntbytes := cntbytes + 1.U
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := mem
                }
            }
        }
        is(mem){
            spi.io.ctrl.valid := (valid === 1.U)
            spi.io.ctrl.we := (we === 1.U)
            when(valid === 1.U){
                when(spi.io.ctrl.ready){
                    spi.io.ctrl.valid := false.B
                    data := spi.io.ctrl.dout
                    state := Mux(we === 1.U, endline, writeback)
                }
            }.otherwise{
                state := endline
            }
        }
        is(writeback){
            uart.io.din := data(7,0)
            when(!uart.io.full){
                uart.io.we := true.B
                data := 0.U(8.W) ## data(31,8)
                cntbytes := cntbytes + 1.U
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := endline
                }
            }
        }
        is(endline){
            uart.io.din := 0x0A.U
            when(!uart.io.full){
                uart.io.we := true.B
                state := idle
            }
        }
        */
        /*
        is(writemem){
            spi.io.ctrl.valid := true.B
            spi.io.ctrl.we := true.B
            when(spi.io.ctrl.ready){
                spi.io.ctrl.valid := false.B
                state := readmem
            }
        }
        is(readmem){
            spi.io.ctrl.valid := true.B
            when(spi.io.ctrl.ready){
                data := spi.io.ctrl.dout
                state := displaymem
            }
        }
        is(displaymem){
            uart.io.din := data(7,0)
            when(!uart.io.full){
                uart.io.we := true.B
                cntbytes := cntbytes + 1.U
                data := 0.U(8.W) ## data(31,8)
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := endline
                }
            }
        }
        is(endline){
            uart.io.din := 0x0A.U
            when(!uart.io.full){
                uart.io.we := true.B
                state := idle
                //addr := addr + 4.U
                //data := data + 4.U
            }
        }
        */
        
        
        is(idle){
            when(!uart.io.empty){
                state := readcmd
            }
        }
        is(readcmd){
            when(!uart.io.empty){
                uart.io.re := true.B
                cntbytes := cntbytes + 1.U
                cmd := uart.io.dout ## cmd(31,8)
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := parsecmd
                }
            }
        }
        is(parsecmd){
            addr := cmd(31,8)
            when(cmd(7,0) === READ_CMD.U){
                nextstate := readmem
                state := sendaddr
            }.elsewhen(cmd(7,0) === WRITE_CMD.U){
                nextstate := readdata
                state := sendaddr
            }.otherwise{
                state := error
            }
        }
        is(sendaddr){
            uart.io.din := addr(7,0)
            when(!uart.io.full){
                uart.io.we := true.B
                cntbytes := cntbytes + 1.U
                addr := addr(7,0) ## addr(23,8)
                when(cntbytes === 2.U){
                    cntbytes := 0.U
                    state := nextstate
                }
            }
        }
        is(readmem){
            spi.io.ctrl.valid := true.B
            when(spi.io.ctrl.ready){
                data :=  spi.io.ctrl.dout
                state := displaymem
            }
        }
        is(displaymem){
            uart.io.din := data(7,0)
            when(!uart.io.full){
                uart.io.we := true.B
                cntbytes := cntbytes + 1.U
                data := 0.U(8.W) ## data(31,8)
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := endline
                }
            }
        }
        is(endline){
            uart.io.din := 0x0A.U
            when(!uart.io.full){
                uart.io.we := true.B
                state := idle
            }
        }
        is(readdata){
            when(!uart.io.empty){
                uart.io.re := true.B
                cntbytes := cntbytes + 1.U
                data := uart.io.dout ## data(31,8)
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := senddata
                }
            }
        }
        is(senddata){
            uart.io.din := data(7,0)
            when(!uart.io.full){
                uart.io.we := true.B
                cntbytes := cntbytes + 1.U
                data := data(7,0) ## data(31,8)
                when(cntbytes === 3.U){
                    cntbytes := 0.U
                    state := writemem
                }
            }
        }
        is(writemem){
            spi.io.ctrl.valid := true.B
            spi.io.ctrl.we := true.B
            when(spi.io.ctrl.ready){
                state := endline
            }
        }
        is(error){
            uart.io.din := 0x45.U
            when(!uart.io.full){
                uart.io.we := true.B
                state := endline
            }
        }
    }
}
object MemGen extends App{
  (new chisel3.stage.ChiselStage).emitVerilog(new MemoryController,args)
}