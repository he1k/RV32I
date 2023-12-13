import chisel3._
package object cpu {
  // Instructions mnemonics
  val ctrl_add = 0.U
  val ctrl_sub = 1.U
  val ctrl_sll = 2.U
  val ctrl_xor = 3.U
  val ctrl_srl = 4.U
  val ctrl_sra = 5.U
  val ctrl_or = 6.U
  val ctrl_and = 7.U
  val ctrl_slt = 8.U
  val ctrl_sltu = 9.U
  val ctrl_lb = 10.U
  val ctrl_lh = 11.U
  val ctrl_lw = 12.U
  val ctrl_lbu = 13.U
  val ctrl_lhu = 14.U
  val ctrl_addi = 15.U
  val ctrl_slli = 16.U
  val ctrl_xori = 17.U
  val ctrl_srli = 18.U
  val ctrl_srai = 19.U
  val ctrl_ori = 20.U
  val ctrl_andi = 21.U
  val ctrl_slti = 22.U
  val ctrl_sltiu = 23.U
  val ctrl_ecall = 24.U
  val ctrl_sb = 25.U
  val ctrl_sh = 26.U
  val ctrl_sw  = 27.U
  val ctrl_beq = 29.U
  val ctrl_bne = 30.U
  val ctrl_blt = 31.U
  val ctrl_bge = 32.U
  val ctrl_bltu = 33.U
  val ctrl_bgeu = 34.U
  val ctrl_jal = 35.U
  val ctrl_jalr = 36.U
  val ctrl_lui = 37.U
  val ctrl_auipc = 38.U

  // alu opcodes
  val alu_add = 0.U
  val alu_sub = 1.U
  val alu_sll = 2.U
  val alu_xor = 3.U
  val alu_srl = 4.U
  val alu_sra = 5.U
  val alu_or = 6.U
  val alu_and = 7.U

}