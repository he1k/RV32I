package cpu
import chisel3._
import chisel3.util._


class IFID extends Bundle{
  val inst = UInt(32.W)
  val pc = UInt(32.W)
  def clr(): Unit ={
    inst := 0.U
    pc := 0.U
  }
}
class IDEX extends Bundle{
  val rd = UInt(5.W)
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
  val aluop = UInt(3.W)
  val a = SInt(32.W)
  val b = SInt(32.W)
  val pc = UInt(32.W)
  val inst = UInt(32.W)
  val sely = UInt(2.W)
  val sela = Bool()
  val selb = UInt(2.W)
  val branch = UInt(3.W)
  val jump = UInt(2.W)
  val regWE = Bool()
  val memWE = Bool()
  val memRE = Bool()
  val bytes = UInt(2.W)
  val sign = Bool()
  val ecll = Bool()
  def clr(): Unit ={
    rd := 0.U
    rs1 := 0.U
    rs2 := 0.U
    aluop := 0.U
    a := 0.S
    b := 0.S
    pc := 0.U
    inst := 0.U
    sely := 0.U
    sela := false.B
    selb := 0.U
    branch := 0.U
    jump := 0.U
    regWE := false.B
    memWE := false.B
    memRE := false.B
    bytes := 0.U
    sign := false.B
    ecll := false.B
  }
}
class EXMEM extends Bundle{
  val rd = UInt(5.W)
  val regWE = Bool()
  val memWE = Bool()
  val memRE = Bool()
  val y = SInt(32.W)
  val b = SInt(32.W)
  val bytes = UInt(2.W)
  val sign = Bool()
  val ecll = Bool()
}
class MEMWB extends Bundle{
  val rd = UInt(5.W)
  val regWE = Bool()
  val data = SInt(32.W)
  val ecll = Bool()
}
class CPU extends Module{
  val io = IO(new Bundle{
    val regFile = Output(Vec(32, SInt(32.W)))
    val ecll = Output(Bool())
    val dinI = Input(UInt(32.W))
    val dinD = Input(UInt(32.W))
    val doutD = Output(UInt(32.W))
    val addrI = Output(UInt(32.W))
    val addrD = Output(UInt(32.W))
    val we = Output(UInt(32.W))
    val bytes = Output(UInt(2.W))
    val sign = Output(Bool())
  })

  val regFile = Module(new RegisterFile)
  io.regFile <> regFile.io.regFile
  val alu = Module(new ALU)
  val instDec = Module(new InstructionDecoder)
  val control = Module(new Control)
  val forwardingUnit = Module(new ForwardingUnit)
  val hazardUnit = Module(new HazardUnit)
  val immGen = Module(new ImmGen)
  val pc = RegInit(0.U(32.W))

  val IFID = RegInit(0.U.asTypeOf(new IFID))
  val IDEX = RegInit(0.U.asTypeOf(new IDEX))
  val EXMEM = RegInit(0.U.asTypeOf(new EXMEM))
  val MEMWB = RegInit(0.U.asTypeOf(new MEMWB))
  val flush = WireDefault(false.B)
  val a = WireDefault(IDEX.a) //Wire that has the value of what rs1 refers to, after any potential forwarding has been done
  val b = WireDefault(IDEX.b) //Wire that has the value of what rs2 refers to, after any potential forwarding has been done

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting hazard unit and registers.
  hazardUnit.io.idex.rd := IDEX.rd
  hazardUnit.io.idex.memRE := IDEX.memRE
  hazardUnit.io.ifid.rs1 := instDec.io.rs1
  hazardUnit.io.ifid.rs2 := instDec.io.rs2

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting forwarding unit.
  forwardingUnit.io.idex.rs1 := IDEX.rs1
  forwardingUnit.io.idex.rs2 := IDEX.rs2
  forwardingUnit.io.exmem.rd := EXMEM.rd
  forwardingUnit.io.exmem.regWE := EXMEM.regWE
  forwardingUnit.io.memwb.rd := MEMWB.rd
  forwardingUnit.io.memwb.regWE := MEMWB.regWE
  forwardingUnit.io.ifid.rs1 := instDec.io.rs1
  forwardingUnit.io.ifid.rs2 := instDec.io.rs2

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting IF stage.
  io.addrI := pc(31, 2)
  when(!hazardUnit.io.stall){
    pc := pc + 4.U
    // Branching and direct jumping
    when((IDEX.branch === 1.U && alu.io.eq) || (IDEX.branch === 2.U && !alu.io.eq) ||
         (IDEX.branch === 3.U && alu.io.lt) || (IDEX.branch === 4.U && (alu.io.eq || !alu.io.lt)) ||
         (IDEX.branch === 5.U && alu.io.ltu) || (IDEX.branch === 6.U && (alu.io.eq || !alu.io.ltu)) || IDEX.jump(0)){
      pc := IDEX.pc + immGen.io.immed
      flush := true.B
    }
    // Inderect jumping
    when(IDEX.jump(1)){
      pc := a.asUInt() + immGen.io.immed // pc = rs1 + immed
      flush := true.B
    }
  }

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting ID stage.
  instDec.io.inst := IFID.inst
  control.io.op := instDec.io.ctrlop
  control.io.im := instDec.io.im
  regFile.io.rs1 := instDec.io.rs1
  regFile.io.rs2 := instDec.io.rs2
  when(!hazardUnit.io.stall){
    IFID.inst := io.dinI
    IFID.pc := pc
  }
  when(flush){
    IFID.clr()
  }
  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting EX stage.
  IDEX.rd := instDec.io.rd
  IDEX.rs1 := instDec.io.rs1
  IDEX.rs2 := instDec.io.rs2
  IDEX.aluop := control.io.aluop
  IDEX.a := regFile.io.reg1
  when(forwardingUnit.io.forwardIDA){
    IDEX.a := MEMWB.data
  }
  IDEX.b := regFile.io.reg2
  when(forwardingUnit.io.forwardIDB){
    IDEX.b := MEMWB.data
  }
  IDEX.pc := IFID.pc
  IDEX.inst := IFID.inst
  IDEX.sely := control.io.sely
  IDEX.sela := control.io.sela
  IDEX.selb := control.io.selb
  IDEX.branch := control.io.branch
  IDEX.jump := control.io.jump
  IDEX.regWE := control.io.regWE
  IDEX.memWE := control.io.memWE
  IDEX.memRE := control.io.memRE
  IDEX.bytes := control.io.bytes
  IDEX.sign := control.io.sign
  IDEX.ecll := control.io.ecll
  immGen.io.inst := IDEX.inst
  when(hazardUnit.io.stall || flush){
    IDEX.clr()

  }
  // Connecting ALU
  alu.io.op := IDEX.aluop
  alu.io.a := a
  when(IDEX.sela){
    alu.io.a := IDEX.pc.asSInt()
  }
  switch(forwardingUnit.io.forwardEXA){
    is(1.U){a := MEMWB.data }
    is(2.U){a := EXMEM.y }
  }

  alu.io.b := b
  when(IDEX.selb === 1.U){
    alu.io.b := 4.S
  } . elsewhen(IDEX.selb === 2.U){
    alu.io.b := immGen.io.immed.asSInt()
  }
  when(forwardingUnit.io.forwardEXB === 1.U){
    b := MEMWB.data
  } . elsewhen(forwardingUnit.io.forwardEXB === 2.U){
    b := EXMEM.y
  }

  // ALU output selecting
  EXMEM.y := alu.io.y
  switch(IDEX.sely){
    is(1.U){ EXMEM.y := (0.U ## alu.io.lt).asSInt() }
    is(2.U){ EXMEM.y := (0.U ## alu.io.ltu).asSInt() }
  }

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting MEM stage.
  EXMEM.rd := IDEX.rd
  EXMEM.regWE := IDEX.regWE
  EXMEM.memWE := IDEX.memWE
  EXMEM.memRE := IDEX.memRE
  EXMEM.b := b
  EXMEM.bytes := IDEX.bytes
  EXMEM.sign := IDEX.sign
  EXMEM.ecll := IDEX.ecll
  io.doutD := EXMEM.b.asUInt()
  io.we := EXMEM.memWE
  io.addrD := EXMEM.y.asUInt()
  io.sign := EXMEM.sign
  io.bytes := EXMEM.bytes

  //------------------------------------------------------------------------------------------------------------------//
  //                Connecting the WB stage
  MEMWB.rd := EXMEM.rd
  MEMWB.regWE := EXMEM.regWE
  MEMWB.data := EXMEM.y
  MEMWB.ecll := EXMEM.ecll
  when(EXMEM.memRE){
    MEMWB.data := io.dinD.asSInt()
  }
  regFile.io.we := MEMWB.regWE
  regFile.io.rd := MEMWB.rd
  regFile.io.din := MEMWB.data

  // Only for simulation purposes
  io.ecll := MEMWB.ecll
}
