package com.smeup.rpgparser.rpginterop

import com.smeup.dbnative.file.DBFile
import com.smeup.rpgparser.interpreter.RpgProgram
import com.smeup.rpgparser.interpreter.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

interface RpgProgramFinder {
    fun findRpgProgram(nameOrSource: String): RpgProgram?
}

class SourceProgramFinder : RpgProgramFinder {
    override fun findRpgProgram(nameOrSource: String): RpgProgram? {
        if (nameOrSource.contains("\n") || nameOrSource.contains("\r")) {
            return RpgProgram.fromInputStream(ByteArrayInputStream(nameOrSource.toByteArray(Charsets.UTF_8)), nameOrSource)
        }
        return null
    }

    override fun toString(): String {
        return "source:"
    }
}

class DirRpgProgramFinder(val directory: File? = null) : RpgProgramFinder {

    init {
        directory?.let { require(it.exists()) { "The specified directory should exist: ${directory.path} -> ${directory.absolutePath}" } }
    }

    override fun findRpgProgram(nameOrSource: String): RpgProgram? {
        val file = File(prefix() + nameAndSuffix(nameOrSource))
        return if (file.exists()) {
            RpgProgram.fromInputStream(FileInputStream(file), nameOrSource)
        } else {
            null
        }
    }

    private fun prefix(): String {
        if (directory != null) {
            return directory.absolutePath + File.separator
        }
        return ""
    }

    private fun nameAndSuffix(name: String): String {
        if (name.endsWith(".rpgle")) {
            return name
        }
        return "$name.rpgle"
    }
    override fun toString(): String {
        val path = if (directory == null) "" else directory.absolutePath.toString()
        return "directory: $path "
    }
}

object RpgSystem {

    private val programFinders = mutableSetOf<RpgProgramFinder>()

    @Synchronized
    fun addProgramFinders(programFindersList: List<RpgProgramFinder>) {
        programFinders.addAll(programFindersList)
    }

    @Synchronized
    fun addProgramFinder(programFinder: RpgProgramFinder) {
        programFinders.add(programFinder)
    }

    @Synchronized
    fun getProgram(programName: String): RpgProgram {
        programFinders.forEach {
            val program = it.findRpgProgram(programName)
            if (program != null) {
                return program
            }
        }
        throw RuntimeException("Program $programName not found")
    }

    @Synchronized
    fun log(logHandlers: List<InterpreterLogHandler>) {
        programFinders.forEach {
            logHandlers.log(RpgProgramFinderLogEntry(it.toString()))
        }
    }
}
