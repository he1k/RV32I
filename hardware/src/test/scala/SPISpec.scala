/*
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import scala.math._

class SPITester(dut: SPI) extends PeekPokeTester(dut) {
  val r = new scala.util.Random
  var num = 0
  def BCDToInt(in : BigInt):BigInt = {
    var out : BigInt = 0
    for(i <- 0 to 9){
      out +=  ((in & (0xF << (i*4))) >> (i*4)) * pow(10, i).toInt // This is very ugly. Find a better solution
    }
    out
  }
  poke(dut.io.spi.MISO, false)
  step(20000)
}
class SPISpec extends FlatSpec with Matchers {
  "SPI wave" should "pass" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"),() => new SPI) { c => new SPITester(c)} should be (true)
  }
}
*/
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SPISpec extends AnyFlatSpec with ChiselScalatestTester {

  "SPI" should "pass" in {
    test(new SPI()).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      dut.clock.setTimeout(0)
      dut.io.spi.MISO.poke(false.B)
      //poke(dut.io.spi.MISO, false)
      dut.clock.step(20000)
      dut.io.ctrl.valid.poke(true.B)
      dut.io.ctrl.we.poke(true.B)
      dut.io.ctrl.din.poke(0x12345678.U)
      dut.io.ctrl.addr.poke(0x876543.U)
      var i = 0
      while((dut.io.ctrl.ready.peekInt().toInt == 0) && i < 10000){
        dut.clock.step(1)
        i = i + 1
      }
      dut.clock.step(400)
    }
  }
}