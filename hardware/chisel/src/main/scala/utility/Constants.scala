package utility

import chisel3._
import chisel3.util._


object Constants{
    object Global{
        val ADDRW = 16
        val DATAW = 32
    }
    object CTRL{
        val ADD = 0.U
        val SUB = 1.U
        val SLL = 2.U
        val XOR = 3.U
        val SRL = 4.U
        val SRA = 5.U
        val OR = 6.U
        val AND = 7.U
        val SLT = 8.U
        val SLTU = 9.U
        val LB = 10.U
        val LH = 11.U
        val LW = 12.U
        val LBU = 13.U
        val LHU = 14.U
        val ADDI = 15.U
        val SLLI = 16.U
        val XORI = 17.U
        val SRLI = 18.U
        val SRAI = 19.U
        val ORI = 20.U
        val ANDI = 21.U
        val SLTI = 22.U
        val SLTIU = 23.U
        val ECALL = 24.U
        val SB = 25.U
        val SH = 26.U
        val SW  = 27.U
        val BEQ = 29.U
        val BNE = 30.U
        val BLT = 31.U
        val BGE = 32.U
        val BLTU = 33.U
        val BGEU = 34.U
        val JAL = 35.U
        val JALR = 36.U
        val LUI = 37.U
        val AUIPC = 38.U
    }
    object ALU{
        val ADD = 0.U
        val SUB = 1.U
        val SLL = 2.U
        val XOR = 3.U
        val SRL = 4.U
        val SRA = 5.U
        val OR = 6.U
        val AND = 7.U
    }
}
