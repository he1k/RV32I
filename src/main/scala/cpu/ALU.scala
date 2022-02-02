package cpu

import chisel3._
import chisel3.util._
class ALU extends Module{
  val io = IO(new Bundle{
    val op = Input(UInt(3.W))
    val a = Input(SInt(32.W))
    val b = Input(SInt(32.W))
    val y = Output(SInt(32.W))
    val eq = Output(Bool())
    val lt = Output(Bool())
    val ltu = Output(Bool())
  })

  val a = WireDefault(io.a)
  val b = WireDefault(io.b)
  val y = WireDefault(0.S(32.W))
  switch(io.op){
    is(alu_add){ y := a + b }
    is(alu_sub){ y := a - b }
    is(alu_sll){ y := a << b(4, 0) }
    is(alu_xor){ y := a ^ b }
    is(alu_srl){ y := (a.asUInt() >> b(4,0)).asSInt() }
    is(alu_sra){ y := a >> b(4,0) }
    is(alu_or){ y := a | b }
    is(alu_and){ y := a & b }
  }
  io.eq := a === b
  io.lt := a < b
  io.ltu := a.asUInt() < b.asUInt()
  io.y := y


}
