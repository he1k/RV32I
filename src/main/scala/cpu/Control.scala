package cpu
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
  io.aluop := alu_add // Standard assignment
  when(io.op === ctrl_sub) {
    io.aluop := alu_sub
  }.elsewhen(io.op === ctrl_sll || io.op === ctrl_slli) {
    io.aluop := alu_sll
  }.elsewhen(io.op === ctrl_srl || io.op === ctrl_srli) {
    io.aluop := alu_srl
  }.elsewhen(io.op === ctrl_sra || io.op === ctrl_srai) {
    io.aluop := alu_sra
  }.elsewhen(io.op === ctrl_xor || io.op === ctrl_xori) {
    io.aluop := alu_xor
  }.elsewhen(io.op === ctrl_or || io.op === ctrl_ori) {
    io.aluop := alu_or
  }.elsewhen(io.op === ctrl_and || io.op === ctrl_andi) {
    io.aluop := alu_and
  }

  // Signalling branch instruction
  io.branch := 0.U
  switch(io.op){
    is(ctrl_beq){io.branch := 1.U}
    is(ctrl_bne){io.branch := 2.U}
    is(ctrl_blt){io.branch := 3.U}
    is(ctrl_bge){io.branch := 4.U}
    is(ctrl_bltu){io.branch := 5.U}
    is(ctrl_bgeu){io.branch := 6.U}
  }
  // Signalling jump instruction
  io.jump := 0.U
  when(io.op === ctrl_jal){io.jump := 1.U}
    .elsewhen(io.op === ctrl_jalr){io.jump := 2.U}

  // Selecting the ALU output
  io.sely := 0.U
  when(io.op === ctrl_slt || io.op === ctrl_slti){
    io.sely := 1.U
  }
  when(io.op === ctrl_sltu || io.op === ctrl_sltiu){
    io.sely := 2.U
  }

  // Choosing between rs1 or pc for the a input of the ALU
  io.sela := (io.op === ctrl_auipc) || (io.jump =/= 0.U)

  // Choosing between rs2, 4.U or immediate value for the b input of the ALU
  io.selb := 0.U
  when(io.jump =/= 0.U){
    io.selb := 1.U
  } . elsewhen(io.im){
    io.selb := 2.U
  }

  io.regWE := !(io.op <= ctrl_bgeu && io.op >= ctrl_ecall) // Not writing to register's on store instructions or ecall's

  io.memWE := io.op >= ctrl_sb && io.op <= ctrl_sw // Only writing to memory on store instructions

  io.bytes := 3.U // load/store 4 bytes
  when(io.op === ctrl_sb || io.op === ctrl_lb || io.op === ctrl_lbu){
    io.bytes := 1.U // load/store 1 byte
  } . elsewhen(io.op === ctrl_sh || io.op === ctrl_lh || io.op === ctrl_lhu){
    io.bytes := 2.U // load/store 2 bytes
  }

  io.sign := !(io.op <= ctrl_lhu && io.op >= ctrl_lbu) // No sign on load unsigned instructions

  io.memRE := io.op >= ctrl_lb && io.op <= ctrl_lhu // Only reading from memory on load instructions

  io.ecll := io.op === ctrl_ecall
}

