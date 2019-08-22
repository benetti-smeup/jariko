package com.smeup.rpgparser.interpreter

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.streams.toList

const val PAD_CHAR = '\u0000'
const val PAD_STRING = PAD_CHAR.toString()

abstract class Value {
    open fun asInt(): IntValue = throw UnsupportedOperationException("${this.javaClass.simpleName} cannot be seen as an Int")
    open fun asDecimal(): DecimalValue = throw UnsupportedOperationException("${this.javaClass.simpleName} cannot be seen as an Decimal")
    open fun asString(): StringValue = throw UnsupportedOperationException()
    open fun asBoolean(): BooleanValue = throw UnsupportedOperationException()
    open fun asTimeStamp(): TimeStampValue = throw UnsupportedOperationException()
    abstract fun assignableTo(expectedType: Type): Boolean
    open fun takeLast(n: Int): Value = TODO("takeLast not yet implemented for ${this.javaClass.simpleName}")
    open fun takeFirst(n: Int): Value = TODO("takeFirst not yet implemented for ${this.javaClass.simpleName}")
    open fun concatenate(other: Value): Value = TODO("concatenate not yet implemented for ${this.javaClass.simpleName}")
    open fun asArray(): ArrayValue = throw UnsupportedOperationException()
}

data class StringValue(var value: String) : Value() {
    override fun assignableTo(expectedType: Type): Boolean {
        return when (expectedType) {
            is StringType -> expectedType.length >= value.length.toLong()
            is DataStructureType -> expectedType.elementSize == value.length // Check for >= ???
            else -> false
        }
    }

    override fun takeLast(n: Int): Value {
        return StringValue(value.takeLast(n))
    }

    override fun takeFirst(n: Int): Value {
        return StringValue(value.take(n))
    }

    override fun concatenate(other: Value): Value {
        require(other is StringValue)
        return StringValue(value + other.value)
    }

    val valueWithoutPadding: String
        get() = value.removeNullChars()

    companion object {
        fun blank(length: Int) = StringValue(PAD_STRING.repeat(length))
        fun padded(value: String, size: Int) = StringValue(value.padEnd(size, PAD_CHAR))
    }

    override fun equals(other: Any?): Boolean {
        return if (other is StringValue) {
            this.valueWithoutPadding == other.valueWithoutPadding
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return valueWithoutPadding.hashCode()
    }

    fun setSubstring(startOffset: Int, endOffset: Int, substringValue: StringValue) {
        require(startOffset >= 0)
        require(startOffset <= value.length)
        require(endOffset >= startOffset)
        require(endOffset <= value.length) { "Asked startOffset=$startOffset, endOffset=$endOffset on string of length ${value.length}" }
        require(endOffset - startOffset == substringValue.value.length)
        val newValue = value.substring(0, startOffset) + substringValue.value + value.substring(endOffset)
        value = newValue.replace('\u0000', ' ')
    }

    fun getSubstring(startOffset: Int, endOffset: Int): StringValue {
        require(startOffset >= 0)
        require(startOffset <= value.length)
        require(endOffset >= startOffset)
        require(endOffset <= value.length) { "Asked startOffset=$startOffset, endOffset=$endOffset on string of length ${value.length}" }
        val s = value.substring(startOffset, endOffset)
        return StringValue(s)
    }

    override fun toString(): String {
        return "StringValue[${value.length}]($valueWithoutPadding)"
    }

    override fun asString() = this
    fun isBlank(): Boolean {
        return this.valueWithoutPadding.isBlank()
    }
}

fun String.removeNullChars(): String {
    val firstNullChar = this.chars().toList().indexOfFirst { it == 0 }
    return if (firstNullChar == -1) {
        this
    } else {
        this.substring(0, firstNullChar)
    }
}

data class IntValue(val value: Long) : Value() {
    override fun assignableTo(expectedType: Type): Boolean {
        // TODO check decimals
        return expectedType is NumberType
    }

    override fun asInt() = this
    // TODO Verify conversion
    override fun asDecimal(): DecimalValue = DecimalValue(BigDecimal(value))

    fun increment() = IntValue(value + 1)

    override fun takeLast(n: Int): Value {
        return IntValue(lastDigits(value, n))
    }

    private fun lastDigits(n: Long, digits: Int): Long {
        return (n % Math.pow(10.0, digits.toDouble())).toLong()
    }

    private fun firstDigits(n: Long, digits: Int): Long {
        var localNr = n
        if (n < 0) {
            localNr = n * -1
        }
        val div = Math.pow(10.0, digits.toDouble()).toInt()
        while (localNr / div > 0) {
            localNr /= 10
        }
        return localNr * java.lang.Long.signum(n)
    }

    override fun takeFirst(n: Int): Value {
        return IntValue(firstDigits(value, n))
    }

    override fun concatenate(other: Value): Value {
        require(other is IntValue)
        return IntValue((value.toString() + other.value.toString()).toLong())
    }

    companion object {
        val ZERO = IntValue(0)
    }
}
data class DecimalValue(val value: BigDecimal) : Value() {
    // TODO Verify conversion
    override fun asInt(): IntValue = IntValue(value.longValueExact())

    override fun asDecimal(): DecimalValue = this

    override fun assignableTo(expectedType: Type): Boolean {
        // TODO check decimals
        return expectedType is NumberType
    }

    companion object {
        val ZERO = DecimalValue(BigDecimal.ZERO)
    }
}
data class BooleanValue(val value: Boolean) : Value() {
    override fun assignableTo(expectedType: Type): Boolean {
        return expectedType is BooleanType
    }

    override fun asBoolean() = this

    companion object {
        val FALSE = BooleanValue(false)
        val TRUE = BooleanValue(true)
    }
}
data class TimeStampValue(val value: Date) : Value() {
    override fun assignableTo(expectedType: Type): Boolean {
        return expectedType is TimeStampType
    }

    override fun asTimeStamp() = this

    companion object {
        val LOVAL = TimeStampValue(GregorianCalendar(0, Calendar.JANUARY, 0).time)
    }
}
abstract class ArrayValue : Value() {
    abstract fun arrayLength(): Int
    abstract fun elementSize(): Int
    abstract fun setElement(index: Int, value: Value)
    abstract fun getElement(index: Int): Value
    fun elements(): List<Value> {
        val elements = LinkedList<Value>()
        for (i in 0 until (arrayLength())) {
            elements.add(getElement(i + 1))
        }
        return elements
    }

    override fun asString(): StringValue {
        return StringValue(elements().map { it.asString() }.joinToString(""))
    }

    override fun assignableTo(expectedType: Type): Boolean {
        if (expectedType is ArrayType) {
            return elements().all { it.assignableTo(expectedType.element) }
        }
        if (expectedType is StringType) {
            return expectedType.length >= arrayLength() * elementSize()
        }
        return false
    }

    override fun asArray() = this
}
data class ConcreteArrayValue(val elements: MutableList<Value>, val elementType: Type) : ArrayValue() {
    override fun elementSize() = elementType.size.toInt()

    override fun arrayLength() = elements.size

    override fun setElement(index: Int, value: Value) {
        require(index >= 1)
        require(index <= arrayLength())
        require(value.assignableTo(elementType))
        elements[index - 1] = value
    }

    override fun getElement(index: Int): Value {
        require(index >= 1)
        require(index <= arrayLength())
        return elements[index - 1]
    }
}

object BlanksValue : Value() {
    override fun toString(): String {
        return "BlanksValue"
    }

    override fun assignableTo(expectedType: Type): Boolean {
        // FIXME
        return true
    }
}

object HiValValue : Value() {
    override fun toString(): String {
        return "HiValValue"
    }

    override fun assignableTo(expectedType: Type): Boolean {
        // FIXME
        return true
    }
}

class StructValue(val elements: MutableMap<FieldDefinition, Value>) : Value() {
    override fun assignableTo(expectedType: Type): Boolean {
        // FIXME
        return true
    }
}

class ProjectedArrayValue(val container: ArrayValue, val field: FieldDefinition) : ArrayValue() {
    override fun elementSize(): Int {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun arrayLength() = container.arrayLength()

    override fun setElement(index: Int, value: Value) {
        require(index >= 1)
        require(index <= arrayLength())
        require(value.assignableTo((field.type as ArrayType).element)) { "Assigning to field $field incompatible value $value" }
        val containerElement = container.getElement(index)
        if (containerElement is StringValue) {
            if (value is StringValue) {
                containerElement.setSubstring(field.startOffset, field.endOffset, value)
            } else {
                TODO()
            }
        } else {
            TODO()
        }
    }

    override fun getElement(index: Int): Value {
        val containerElement = container.getElement(index)
        if (containerElement is StringValue) {
            return containerElement.getSubstring(field.startOffset, field.endOffset)
        } else {
            TODO()
        }
    }
}

fun createArrayValue(elementType: Type, n: Int, creator: (Int) -> Value) = ConcreteArrayValue(Array(n, creator).toMutableList(), elementType)

fun blankString(length: Int) = StringValue(PAD_STRING.repeat(length))

fun Long.asValue() = IntValue(this)

fun String.asValue() = StringValue(this)

private const val FORMAT_DATE_ISO = "yyyy-MM-dd-HH.mm.ss.SSS"

fun String.asIsoDate(): Date {
    return SimpleDateFormat(FORMAT_DATE_ISO).parse(this.take(FORMAT_DATE_ISO.length))
}
