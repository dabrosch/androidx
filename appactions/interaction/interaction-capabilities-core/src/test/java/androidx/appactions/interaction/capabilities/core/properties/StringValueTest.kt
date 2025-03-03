/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.appactions.interaction.capabilities.core.properties

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StringValueTest {

    @Test
    fun constructorWithOnlyName() {
        val stringValue = StringValue("abcd")

        assertThat(stringValue.name).isEqualTo("abcd")
        assertThat(stringValue.alternateNames).isEmpty()
    }

    @Test
    fun fullConstructor_returnsValues() {
        val stringValue = StringValue("abcd", listOf("alt1", "alt2"))

        assertThat(stringValue.name).isEqualTo("abcd")
        assertThat(stringValue.alternateNames).containsExactly("alt1", "alt2")
    }

    @Test
    fun objectMethods_overridden() {
        val stringValue = StringValue("abcd", listOf("alt1"))

        assertThat(stringValue).isEqualTo(StringValue("abcd", listOf("alt1")))
        assertThat(stringValue.hashCode()).isNotEqualTo(0)
        assertThat(stringValue.toString()).isEqualTo(
            "StringValue(name='abcd', alternateNames=[alt1])"
        )
    }
}