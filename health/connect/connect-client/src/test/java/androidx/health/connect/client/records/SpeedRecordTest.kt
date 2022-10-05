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

package androidx.health.connect.client.records

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpeedRecordTest {

    @Test
    fun validRecord_equals() {
        assertThat(
                SpeedRecord(
                    Instant.ofEpochMilli(1234L),
                    null,
                    Instant.ofEpochMilli(1236L),
                    null,
                    listOf<SpeedRecord.Sample>()
                )
            )
            .isEqualTo(
                SpeedRecord(
                    Instant.ofEpochMilli(1234L),
                    null,
                    Instant.ofEpochMilli(1236L),
                    null,
                    listOf<SpeedRecord.Sample>()
                )
            )
    }

    @Test
    fun sameStartEndTime_validRecord_equals() {
        assertThat(
            SpeedRecord(
                Instant.ofEpochMilli(1234L),
                null,
                Instant.ofEpochMilli(1234L),
                null,
                listOf<SpeedRecord.Sample>()
            )
        )
            .isEqualTo(
                SpeedRecord(
                    Instant.ofEpochMilli(1234L),
                    null,
                    Instant.ofEpochMilli(1234L),
                    null,
                    listOf<SpeedRecord.Sample>()
                )
            )
    }

    @Test
    fun invalidTimes_throws() {
        assertFailsWith<IllegalArgumentException> {
            SpeedRecord(
                Instant.ofEpochMilli(1235L),
                null,
                Instant.ofEpochMilli(1234L),
                null,
                listOf<SpeedRecord.Sample>()
            )
        }
    }
}