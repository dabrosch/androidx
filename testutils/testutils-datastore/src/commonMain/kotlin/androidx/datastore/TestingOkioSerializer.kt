/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import okio.BufferedSink
import okio.BufferedSource
import okio.EOFException
import okio.IOException
import okio.use

class TestingOkioSerializer(
    private val config: TestingSerializerConfig
) : OkioSerializer<Byte> {

    override suspend fun readFrom(source: BufferedSource): Byte {
        if (config.failReadWithCorruptionException) {
            throw CorruptionException(
                "CorruptionException",
                IOException("I was asked to fail with corruption on reads")
            )
        }

        if (config.failingRead) {
            throw IOException("I was asked to fail on reads")
        }

        val read = try {
            source.use {
                it.readInt()
            }
        } catch (eof: EOFException) {
            return 0
        }
        return read.toByte()
    }

    override suspend fun writeTo(t: Byte, sink: BufferedSink) {
        if (config.failingWrite) {
            throw IOException("I was asked to fail on writes")
        }
        sink.use {
            it.writeInt(t.toInt())
        }
    }

    override val defaultValue: Byte
        get() = config.defaultValue
}