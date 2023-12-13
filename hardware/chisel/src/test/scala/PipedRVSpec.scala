import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}

// Post a directory for the folder containing both test files and result files in the dir val.
// Must be in the format of "/Users/MyUser/.../TestFolder/"
// Note: The tester expects there to be a folder named out inside your TestFolder.
object TestVar{
  val dir = "C:\\Users\\farfar\\Desktop\\tests\\"
  val tests = Seq("addlarge", "addneg","addpos","bool","set","shift","shift2","branchcnt","branchmany","branchtrap","loop",
    "recursive","width","t1","t2","t3","t4","t5","t6","t7","t8","t9","t10","t11","t12","t13","t15")
}
class PipedRVTester(dut: PipedRV, dir : String, filename : String) extends PeekPokeTester(dut) {
  def extract(n : Int, b: Int): Byte ={
    ((n << 8*b) >>> 8*b).toByte
  }
  var a7 = 0
  while(a7 != 10){
    if(peek(dut.io.ecll) == 1){
      a7 = peek(dut.io.regFile(17)).toInt
    }
    step(1)
  }
  var x = 0
  val cmpr = Files.readAllBytes(Paths.get(dir + filename + ".res")).grouped(4).map(b =>ByteBuffer.wrap(b.reverse).getInt).toArray
  val res = Array.ofDim[Byte](128)
  for(i<-0 to 31){
    expect(dut.io.regFile(i), cmpr(i))
    x = peek(dut.io.regFile(i)).toInt
    res(i) = extract(x, 0)
    res(i+1) = extract(x, 1)
    res(i+2) = extract(x, 2)
    res(i+3) = extract(x, 3)
  }
  // Write the result to a binary file
  new FileOutputStream(dir + "/out/" + filename + ".bin").write(res)
  // Spill all 32 registers
  println("Output from \"" + filename +"\":")
  for(i <-0 to 31){
    println("x"+i.toString + " = 0x" +  peek(dut.io.regFile(i)).toInt.toHexString)
  }
}
class PipedRVSpec extends FlatSpec with Matchers {
  TestVar.tests.foreach{ filename =>
    "PipedRV " + filename should "pass" in {
      chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "off"),() => new PipedRV(TestVar.dir + filename)) { c => new PipedRVTester(c,TestVar.dir,filename)} should be (true)
    }
  }
}
