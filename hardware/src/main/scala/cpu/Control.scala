package cpu
import utility.Constants._
import chisel3._
import chisel3.util._

class Control extends Module{
  val io = IO(new Bundle{
    val op = Input(UInt(6.W))
    val im = Input(Bool())
    val aluop = Output(UInt(3.W))
    val branch = Output(UInt(3.W))
    val jump = Output(UInt(2.W))
    val sela = Output(Bool())
    val selb = Output(UInt(2.W))
    val sely = Output(UInt(2.W))
    val regWE = Output(Bool())
    val memWE = Output(Bool())
    val memRE = Output(Bool())
    val bytes = Output(UInt(2.W))
    val sign = Output(Bool())
    val ecll = Output(Bool())
  })

  // ALU opcode determination.
  io.aluop := ALU.ADD // StANDard assignment
  when(io.op === CTRL.SUB) {
    io.aluop := ALU.SUB
  }.elsewhen(io.op === CTRL.SLL || io.op === CTRL.SLLI) {
    io.aluop := ALU.SLL
  }.elsewhen(io.op === CTRL.SRL || io.op === CTRL.SRLI) {
    io.aluop := ALU.SRL
  }.elsewhen(io.op === CTRL.SRA || io.op === CTRL.SRAI) {
    io.aluop := ALU.SRA
  }.elsewhen(io.op === CTRL.XOR || io.op === CTRL.XORI) {
    io.aluop := ALU.XOR
  }.elsewhen(io.op === CTRL.OR || io.op === CTRL.ORI) {
    io.aluop := ALU.OR
  }.elsewhen(io.op === CTRL.AND || io.op === CTRL.ANDI) {
    io.aluop := ALU.AND
  }

  // Signalling branch instruction
  io.branch := 0.U
  switch(io.op){
    is(CTRL.BEQ){io.branch := 1.U}
    is(CTRL.BNE){io.branch := 2.U}
    is(CTRL.BLT){io.branch := 3.U}
    is(CTRL.BGE){io.branch := 4.U}
    is(CTRL.BLTU){io.branch := 5.U}
    is(CTRL.BGEU){io.branch := 6.U}
  }
  // Signalling jump instruction
  io.jump := 0.U
  when(io.op === CTRL.JAL){io.jump := 1.U}
    .elsewhen(io.op === CTRL.JALR){io.jump := 2.U}

  // Selecting the ALU output
  io.sely := 0.U
  when(io.op === CTRL.SLT || io.op === CTRL.SLTI){
    io.sely := 1.U
  }
  when(io.op === CTRL.SLTU || io.op === CTRL.SLTIU){
    io.sely := 2.U
  }

  // Choosing between rs1 or pc for the a input of the ALU
  io.sela := (io.op === CTRL.AUIPC) || (io.jump =/= 0.U)

  // Choosing between rs2, 4.U or immediate value for the b input of the ALU
  io.selb := 0.U
  when(io.jump =/= 0.U){
    io.selb := 1.U
  } . elsewhen(io.im){
    io.selb := 2.U
  }

  io.regWE := !(io.op <= CTRL.BGEU && io.op >= CTRL.ECALL) // Not writing to register's on store instructions or ECALL's

  io.memWE := io.op >= CTRL.SB && io.op <= CTRL.SW // Only writing to memory on store instructions

  io.bytes := 3.U // load/store 4 bytes
  when(io.op === CTRL.SB || io.op === CTRL.LB || io.op === CTRL.LBU){
    io.bytes := 1.U // load/store 1 byte
  } . elsewhen(io.op === CTRL.SH || io.op === CTRL.LH || io.op === CTRL.LHU){
    io.bytes := 2.U // load/store 2 bytes
  }

  io.sign := !(io.op <= CTRL.LHU && io.op >= CTRL.LBU) // No sign on load unsigned instructions

  io.memRE := io.op >= CTRL.LB && io.op <= CTRL.LHU // Only reading from memory on load instructions

  io.ecll := io.op === CTRL.ECALL
}

