package com.smeup.rpgparser.interpreter

import java.nio.charset.Charset

/**
 * The charset should be sort of system setting
 * Cp037    EBCDIC US
 * Cp0280   EBCDIC ITALIAN
 * See: https://www.ibm.com/support/knowledgecenter/SSLTBW_2.1.0/com.ibm.zos.v2r1.idad400/ccsids.htm
 */
fun sortA(value: Value, arrayType: ArrayType, charset: Charset) {
    val ascend: Boolean = arrayType.ascend == null || arrayType.ascend == true

    when (value) {
        is ConcreteArrayValue -> {
            // TODO pass the correct charset to the default sorting algorithm
            if (ascend) {
                value.elements.sort()
            } else {
                value.elements.sortByDescending { it }
            }
        }

        is ProjectedArrayValue -> {
            require(value.field.type is ArrayType)

            val numOfElements = value.arrayLength
            val totalLengthOfAllElements = value.container.len
            val elementSize = totalLengthOfAllElements / numOfElements

            val elements: List<String> = value.container.value.chunked(elementSize)
            val elementsToCalculateSort: List<String> = elements.map {
                it.substring(
                    value.field.calculatedStartOffset!!,
                    value.field.calculatedEndOffset!!
                )
            }
            // crete a map <Index, ValueToSort>
            val indexesMap: Map<Int, String> = elementsToCalculateSort.mapIndexed { i, v -> i + 1 to v }.toMap()
            // sort the map
            val descend: Boolean = if (value.field.overlayingOn == null) {
                value.field.descend
            } else {
                (value.field.overlayingOn as FieldDefinition).descend
            }
            val sortedMap = if (descend) {
                indexesMap.toList().sortedByDescending { it.second }.toMap()
            } else {
                indexesMap.toList().sortedBy { it.second }.toMap()
            }
            var containerValue = ""
            sortedMap.keys.forEach { key ->
                containerValue += elements[key - 1]
            }

            // return value
            value.container.value = containerValue
        }
    }
}