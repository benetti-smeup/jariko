/*
 * Copyright 2019 Sme.UP S.p.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smeup.rpgparser.execution

import com.smeup.rpgparser.AbstractTest
import com.smeup.rpgparser.utils.StringOutputStream
import org.apache.commons.io.input.ReaderInputStream
import org.junit.Test
import java.io.PrintStream
import java.io.StringReader
import java.nio.charset.Charset
import kotlin.test.assertTrue
import com.smeup.rpgparser.execution.main as runnerMain

class RunnerCliTest : AbstractTest() {

    @Test
    fun withNoArgsReplIsStarted() {
        System.setIn(ReaderInputStream(StringReader("signoff"), Charset.defaultCharset()))
        val out = StringOutputStream()
        System.setOut(PrintStream(out))

        runnerMain(arrayOf())

        assertTrue(out.written)
        assertTrue(out.toString().contains("Goodbye"))
    }
}