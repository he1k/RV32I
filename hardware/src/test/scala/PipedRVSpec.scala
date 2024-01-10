import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}

object TestVar{
  val dir = ".\\caelab-tests\\"
  val tests = Seq("addlarge", "addneg","addpos","bool","set","shift","shift2","branchcnt","branchmany","branchtrap","loop",
    "recursive","width","t1","t2","t3","t4","t5","t6","t7","t8","t9","t10","t11","t12","t13","t15")
  val DISPLAY_OUTPUT = false
}

class PipedRVSpec extends AnyFlatSpec with ChiselScalatestTester {
  TestVar.tests.foreach{ filename =>
    "PipedRV " + filename should "pass" in {
      test(new PipedRV(TestVar.dir + filename)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
        dut.clock.setTimeout(0)
        def extract(n : Int, b: Int): Byte ={
          ((n << 8*b) >>> 8*b).toByte
        }
        var a7 = 0
        while(a7 != 10){
          if(dut.io.ecll.peekInt().toInt == 1){
            a7 = dut.io.regFile(17).peekInt().toInt
          }
          dut.clock.step(1)
        }
        var x = 0
        val cmpr = Files.readAllBytes(Paths.get(TestVar.dir + filename + ".res")).grouped(4).map(b =>ByteBuffer.wrap(b.reverse).getInt).toArray
        val res = Array.ofDim[Byte](128)
        for(i<-0 to 31){
          dut.io.regFile(i).expect(cmpr(i)) // Compare output with expected values
          // Save output to res file
          x = dut.io.regFile(i).peekInt().toInt 
          res(i) = extract(x, 0)
          res(i+1) = extract(x, 1)
          res(i+2) = extract(x, 2)
          res(i+3) = extract(x, 3)
        }
        new FileOutputStream(TestVar.dir + "/out/" + filename + ".bin").write(res) // Write the result to a binary file
        if(TestVar.DISPLAY_OUTPUT){ // Spill all 32 registers
          println("Output from \"" + filename +"\":")
          for(i <-0 to 31){
            println("x"+i.toString + " = 0x" +  dut.io.regFile(i).peekInt().toInt.toHexString)
          }
        }
      }
    }
  }
}