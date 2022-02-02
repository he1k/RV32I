package memory

import chisel3._
import chisel3.util._
class RAM extends Module{
  val io = IO(new Bundle{
    val we = Input(Bool())
    val addr = Input(UInt(20.W))
    val din = Input(UInt(32.W))
    val dout = Output(UInt(32.W))
    val bytes = Input(UInt(2.W))
    val sign = Input(Bool())
  })
  val bankA = Mem(262144, UInt(8.W))
  val bankB = Mem(262144, UInt(8.W))
  val bankC = Mem(262144, UInt(8.W))
  val bankD = Mem(262144, UInt(8.W))
  val rowUpper = io.addr(19,2) + 1.U
  val rowLower = io.addr(19,2)
  val dout = WireDefault(VecInit(Seq.fill(4)(0.U(8.W))))
  val rowA = WireDefault(rowLower)
  val rowB = WireDefault(rowLower)
  val rowC = WireDefault(rowLower)
  val rowD = WireDefault(rowLower)


  when(io.addr(1) || io.addr(0)){ rowA := rowUpper }
  when(io.addr(1)){ rowB := rowUpper }
  when(io.addr(1) && io.addr(0)){ rowC := rowUpper }

  switch(io.addr(1,0)){
    is(0.U){
      when(io.we){
        bankA.write(rowA, io.din(7, 0))
        when(io.bytes(1)){
          bankB.write(rowB, io.din(15, 8))
          when(io.bytes(0)){
            bankC.write(rowC, io.din(23, 16))
            bankD.write(rowD, io.din(31, 24))
          }
        }
      } . otherwise {
        dout(0) := bankA.read(rowA)
        when(io.bytes(1)){
          dout(1) := bankB.read(rowB)
          when(io.bytes(0)){
            dout(2) := bankC.read(rowC)
            dout(3) := bankD.read(rowD)
          } . elsewhen(io.sign){
            dout(2) := Fill(8, dout(1)(7))
            dout(3) := Fill(8, dout(1)(7))
          }
        } . elsewhen(io.sign){
          dout(1) := Fill(8, dout(0)(7))
          dout(2) := Fill(8, dout(0)(7))
          dout(3) := Fill(8, dout(0)(7))
        }
      }
    }
    is(1.U){
      when(io.we){
        bankB.write(rowB, io.din(7, 0))
        when(io.bytes(1)){
          bankC.write(rowC, io.din(15, 8))
          when(io.bytes(0)){
            bankD.write(rowD, io.din(23, 16))
            bankA.write(rowA, io.din(31, 24))
          }
        }
      } . otherwise {
        dout(0) := bankB.read(rowB)
        when(io.bytes(1)) {
          dout(1) := bankC.read(rowC)
          when(io.bytes(0)){
            dout(2) := bankD.read(rowD)
            dout(3) := bankA.read(rowA)
          } . elsewhen(io.sign){
            dout(2) := Fill(8, dout(1)(7))
            dout(3) := Fill(8, dout(1)(7))
          }
        }.elsewhen(io.sign) {
          dout(1) := Fill(8, dout(0)(7))
          dout(2) := Fill(8, dout(0)(7))
          dout(3) := Fill(8, dout(0)(7))
        }
      }
    }
    is(2.U){
      when(io.we){
        bankC.write(rowC, io.din(7, 0))
        when(io.bytes(1)){
          bankD.write(rowD, io.din(15, 8))
          when(io.bytes(0)){
            bankA.write(rowA, io.din(23, 16))
            bankB.write(rowB, io.din(31, 24))
          }
        }
      }. otherwise {
        dout(0) := bankC.read(rowC)
        when(io.bytes(1)) {
          dout(1) := bankD.read(rowD)
          when(io.bytes(0)){
            dout(2) := bankA.read(rowA)
            dout(3) := bankB.read(rowB)
          }. elsewhen(io.sign){
            dout(2) := Fill(8, dout(1)(7))
            dout(3) := Fill(8, dout(1)(7))
          }
        }.elsewhen(io.sign) {
          dout(1) := Fill(8, dout(0)(7))
          dout(2) := Fill(8, dout(0)(7))
          dout(3) := Fill(8, dout(0)(7))
        }
      }
    }
    is(3.U){
      when(io.we){
        bankD.write(rowD, io.din(7, 0))
        when(io.bytes(1)){
          bankA.write(rowA, io.din(15, 8))
          when(io.bytes(0)){
            bankB.write(rowB, io.din(23, 16))
            bankC.write(rowC, io.din(31, 24))
          }
        }
      }. otherwise {
        dout(0) := bankD.read(rowD)
        when(io.bytes(1)){
          dout(1) := bankA.read(rowA)
          when(io.bytes(0)){
            dout(2) := bankB.read(rowB)
            dout(3) := bankC.read(rowC)
          } . elsewhen(io.sign){
            dout(2) := Fill(8, dout(1)(7))
            dout(3) := Fill(8, dout(1)(7))
          }
        } . elsewhen(io.sign){
          dout(1) := Fill(8, dout(0)(7))
          dout(2) := Fill(8, dout(0)(7))
          dout(3) := Fill(8, dout(0)(7))
        }
      }
    }
  }
  io.dout := dout(3) ## dout(2) ## dout(1) ## dout(0)
}
