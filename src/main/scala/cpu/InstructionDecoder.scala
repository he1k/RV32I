package cpu

import chisel3._
import chisel3.util._


class InstructionDecoder extends Module{
  val io = IO(new Bundle{
    val inst = Input(UInt(32.W))
    val ctrlop = Output(UInt(6.W))
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val im = Output(Bool())
  })
  val funct3 = WireDefault(io.inst(14, 12))
  val funct7 = WireDefault(io.inst(31, 25))
  val op = WireDefault(io.inst(6,0))

  io.im := false.B
  io.ctrlop := 0.U
  io.rd := io.inst(11, 7)
  io.rs1 := io.inst(19, 15)
  io.rs2 := io.inst(24,20)

  switch(op){
    //R type instruction
    is("b0110011".U){
      switch(funct3 ## funct7){
        is("b0000000000".U){ io.ctrlop := ctrl_add }
        is("b0000100000".U){ io.ctrlop := ctrl_sub }
        is("b0010000000".U){ io.ctrlop := ctrl_sll }
        is("b1000000000".U){ io.ctrlop := ctrl_xor }
        is("b1010000000".U){ io.ctrlop := ctrl_srl }
        is("b1010100000".U){ io.ctrlop := ctrl_sra }
        is("b1100000000".U){ io.ctrlop := ctrl_or }
        is("b1110000000".U){ io.ctrlop := ctrl_and }
        is("b0100000000".U){ io.ctrlop := ctrl_slt }
        is("b0110000000".U){ io.ctrlop := ctrl_sltu }
      }
    }
    // B type instruction
    is("b1100011".U){
      switch(funct3){
        is("b000".U){ io.ctrlop := ctrl_beq }
        is("b001".U){ io.ctrlop := ctrl_bne }
        is("b100".U){ io.ctrlop := ctrl_blt }
        is("b101".U){ io.ctrlop := ctrl_bge }
        is("b110".U){ io.ctrlop := ctrl_bltu }
        is("b111".U){ io.ctrlop := ctrl_bgeu }
      }
    }
    // S type instruction
    is("b0100011".U){
      io.im := true.B
      switch(funct3){
        is("b000".U){ io.ctrlop := ctrl_sb }
        is("b001".U){ io.ctrlop := ctrl_sh }
        is("b010".U){ io.ctrlop := ctrl_sw }
      }
    }
    // J type instruction
    is("b1101111".U){
      io.ctrlop := ctrl_jal
    }
    // U type instruction
    is("b0110111".U){
      io.im := true.B
      io.ctrlop := ctrl_lui
    }
    // U type instruction
    is("b0010111".U){
      io.im := true.B
      io.ctrlop := ctrl_auipc
    }
    // I type instruction (data transfer)
    is("b0000011".U){
      io.im := true.B
      switch(funct3){
        is("b000".U){ io.ctrlop := ctrl_lb }
        is("b001".U){ io.ctrlop := ctrl_lh }
        is("b010".U){ io.ctrlop := ctrl_lw }
        is("b100".U){ io.ctrlop := ctrl_lbu }
        is("b101".U){ io.ctrlop := ctrl_lhu }
      }
    }
    // I type instruction (arithmetic / logic)
    is("b0010011".U){
      io.im := true.B
      switch(funct3){
        is("b000".U){ io.ctrlop := ctrl_addi }
        is("b010".U){ io.ctrlop := ctrl_slti }
        is("b011".U){ io.ctrlop := ctrl_sltiu }
        is("b100".U){ io.ctrlop := ctrl_xori }
        is("b001".U){
          when(funct7 === "b0000000".U){
            io.ctrlop := ctrl_slli
          }
        }
        is("b101".U){
          when(funct7 === "b0000000".U){
            io.ctrlop := ctrl_srli
          } . elsewhen(funct7 === "b0100000".U){
            io.ctrlop := ctrl_srai
          }
        }
        is("b110".U){ io.ctrlop := ctrl_ori }
        is("b111".U){ io.ctrlop := ctrl_andi }
      }
    }
    // I type instruction (jump)
    is("b1100111".U){
      when(funct3 === "b000".U){ io.ctrlop := ctrl_jalr}
    }
    // System call
    is("b1110011".U){
      when(io.inst(31,7) === 0.U){ io.ctrlop := ctrl_ecall }
    }
  }
}
