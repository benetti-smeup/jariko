package com.smeup.rpgparser.evaluation

import com.smeup.rpgparser.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpreterTest {

    @Test
    fun executeJD_001_plist() {
        val cu = assertASTCanBeProduced("JD_001", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf(
                "U\$FUNZ" to StringValue("Foo"),
                "U\$METO" to StringValue("Bar"),
                "U\$SVARSK" to createArrayValue(1050, 200) { blankString(1050) },
                "U\$IN35" to blankString(1)))
        assertEquals(listOf("IMP0", "FIN0"), interpreter.getExecutedSubroutineNames())
        assertEquals(StringValue("Foo"), interpreter["U\$FUNZ"])
        assertEquals(StringValue("Bar"), interpreter["U\$METO"])
        assertEquals(createArrayValue(1050, 200) { blankString(1050) }, interpreter["U\$SVARSK"])
        assertEquals(StringValue(" "), interpreter["U\$IN35"])
    }

    @Test
    fun executeJD_001_settingVars() {
        val cu = assertASTCanBeProduced("JD_001", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf(
                "U\$FUNZ" to StringValue("Foo"),
                "U\$METO" to StringValue("Bar"),
                "U\$SVARSK" to createArrayValue(1050, 200) { blankString(1050) },
                "U\$IN35" to blankString(1)))
        assertEquals(listOf("IMP0", "FIN0"), interpreter.getExecutedSubroutineNames())
        // Initialized inside IMP0
        assertEquals(createArrayValue(1050, 200) { blankString(1050) }, interpreter["\$\$SVAR"])
    }

    @Test
    fun executeJD_001_inzFunz() {
        val cu = assertASTCanBeProduced("JD_001", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf(
                "U\$FUNZ" to StringValue("INZ"),
                "U\$METO" to StringValue("Bar"),
                "U\$SVARSK" to createArrayValue(1050,200) { blankString(1050) },
                "U\$IN35" to StringValue("X")))
        assertEquals(10, interpreter.getEvaluatedExpressionsConcise().size)
        assertEquals(listOf("IMP0", "FINZ", "FIN0"), interpreter.getExecutedSubroutineNames())
        // Initialized inside IMP0
        assertEquals(createArrayValue(1050, 200) { blankString(1050) }, interpreter["\$\$SVAR"])
        // Assigned inside FINZ
        assertEquals(createArrayValue(1050, 200) { blankString(1050) }, interpreter["U\$SVARSK_INI"])
        assertEquals(StringValue(" "), interpreter["U\$IN35"])
    }

    @Test
    fun executeJD_000_datainit() {
        val cu = assertASTCanBeProduced("JD_000_datainit", true)
        cu.resolve()

        assertEquals("U\$SVARSK", cu.allDataDefinitions[0].name)
        assertEquals("\$\$SVARCD", cu.allDataDefinitions[1].name)
        assertEquals("\$\$SVARVA", cu.allDataDefinitions[2].name)
        assertEquals(0, (cu.allDataDefinitions[1] as FieldDefinition).startOffset)
        assertEquals(50, (cu.allDataDefinitions[1] as FieldDefinition).endOffset)
        assertEquals(50, (cu.allDataDefinitions[2] as FieldDefinition).startOffset)
        assertEquals(1050, (cu.allDataDefinitions[2] as FieldDefinition).endOffset)

        val interpreter = execute(cu, mapOf())

        val svarsk = interpreter["U\$SVARSK"]
        assertTrue(svarsk is ArrayValue)
        assertEquals(200, (svarsk as ArrayValue).arrayLength())
        val svarskElement = (svarsk as ArrayValue).getElement(0)
        assertEquals(blankString(1050), svarskElement)

        val svarcd = interpreter["\$\$SVARCD"]
        assertTrue(svarcd is ArrayValue)
        assertEquals(200, (svarcd as ArrayValue).arrayLength())
        val svarcdElement = (svarcd as ArrayValue).getElement(0)
        assertEquals(blankString(50), svarcdElement)

        val svarva = interpreter["\$\$SVARVA"]
        assertTrue(svarva is ArrayValue)
        assertEquals(200, (svarva as ArrayValue).arrayLength())
        val svarvaElement = (svarva as ArrayValue).getElement(0)
        assertEquals(blankString(1000), svarvaElement)
    }

    @Test
    fun executeJD_000_base() {
        val cu = assertASTCanBeProduced("JD_000_base", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf())
    }

//    @Test
//    fun executeJD_000() {
//        val cu = assertASTCanBeProduced("JD_000", true)
//        cu.resolve()
//        val interpreter = execute(cu, mapOf())
//    }

    @Test
    fun executeCALCFIB_initialDeclarations_dec() {
        val cu = assertASTCanBeProduced("CALCFIB_1", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("3")))
        assertIsIntValue(interpreter["NBR"], 3)
    }

    @Test
    fun executeCALCFIB_initialDeclarations_inz() {
        val cu = assertASTCanBeProduced("CALCFIB_1", true)
        cu.resolve()

        assertTrue(cu.getDataDefinition("ppdat").initializationValue == null)
        assertTrue(cu.getDataDefinition("NBR").initializationValue == null)
        assertTrue(cu.getDataDefinition("RESULT").initializationValue != null)
        assertTrue(cu.getDataDefinition("COUNT").initializationValue == null)
        assertTrue(cu.getDataDefinition("A").initializationValue != null)
        assertTrue(cu.getDataDefinition("B").initializationValue != null)

        val interpreter = execute(cu, mapOf("ppdat" to StringValue("3")))
        assertIsIntValue(interpreter["RESULT"], 0)
        assertIsIntValue(interpreter["A"], 0)
        assertIsIntValue(interpreter["B"], 1)
    }

    @Test
    fun executeCALCFIB_otherClauseOfSelect() {
        val cu = assertASTCanBeProduced("CALCFIB_2", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("10")), si)
        val assignments = interpreter.getAssignments()
        assertIsIntValue(interpreter["NBR"], 10)
        assertEquals(listOf("10"), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_0() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("0")), si)
        assertEquals(listOf("FIBONACCI OF: 0 IS: 0                             "), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_1() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("1")), si)
        assertEquals(listOf("FIBONACCI OF: 1 IS: 1                             "), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_2() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("2")), si)
        assertEquals(listOf("FIBONACCI OF: 2 IS: 1                             "), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_3() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("3")), si)
        assertEquals(listOf("FIBONACCI OF: 3 IS: 2                             "), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_4() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("4")), si)
        assertEquals(listOf("FIBONACCI OF: 4 IS: 3                             "), si.displayed)
    }

    @Test
    fun executeCALCFIB_for_value_10() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf("ppdat" to StringValue("10")), si)
        assertEquals(listOf("FIBONACCI OF: 10 IS: 55                           "), si.displayed)
    }

    @Test
    fun executeHELLO() {
        val cu = assertASTCanBeProduced("HELLO", true)
        cu.resolve()
        val si = CollectorSystemInterface()
        val interpreter = execute(cu, mapOf(), si)
        assertEquals(listOf("Hello World!"), si.displayed)
    }
}