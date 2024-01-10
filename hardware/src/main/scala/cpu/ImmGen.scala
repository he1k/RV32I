package cpu
import chisel3._
import chisel3.util._
class ImmGen extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val immed = Output(UInt(32.W))
  })
  val op = WireDefault(io.inst(6,0))
  val funct3 = WireDefault(io.inst(14, 12))
  val funct7 = WireDefault(io.inst(31, 25))
  val rs2 = WireDefault(io.inst(24,20))

  // I type instruction
  io.immed :=  Fill(20, io.inst(31)) ## io.inst(31, 20)
  switch(op){
    // B type instruction
    is("b1100011".U){
      io.immed := Fill(20, io.inst(31)) ##io.inst(31) ## io.inst(7) ## io.inst(30, 25) ## io.inst(11, 8) ## 0.U
    }
    // S type instruction
    is("b0100011".U){
      io.immed := Fill(20, io.inst(31)) ## io.inst(31, 25) ## io.inst(11, 7)
    }
    // J type instruction
    is("b1101111".U){
      io.immed := Fill(12, io.inst(31)) ## io.inst(31) ## io.inst(19, 12) ## io.inst(20) ## io.inst(30,21)  ## 0.U
    }
    // U type instruction
    is("b0110111".U){
      io.immed := (io.inst(31, 12) << 12)
    }
    // U type instruction
    is("b0010111".U){
      io.immed := (io.inst(31, 12)<<12)
    }
    // For 'shamt'
    is("b0010011".U){
      when(funct3 ## funct7 === "b0010000000".U || funct3 ## funct7 === "b1010000000".U || funct3 ## funct7 === "b1010100000".U){
        io.immed := Fill(27, rs2(4)) ## rs2
      }
    }
  }
}

