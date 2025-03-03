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

package androidx.window.layout

import android.app.Activity
import android.content.ContextWrapper
import android.inputmethodservice.InputMethodService
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.window.layout.util.ContextUtils
import androidx.window.layout.util.ContextUtils.unwrapUiContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

/**
 * Instrumentation tests for [ContextUtils].
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class ContextUtilsTest {

    @Test
    fun testUnwrapUiContext_noContextWrapper_activity() {
        val context = mock(Activity::class.java)
        assertEquals(context, unwrapUiContext(context))
    }

    @Test
    fun testUnwrapUiContext_noContextWrapper_inputMethodService() {
        val context = mock(InputMethodService::class.java)
        assertEquals(context, unwrapUiContext(context))
    }

    @Test
    fun testUnwrapUiContext_contextWrapper_null() {
        val contextWrapper = ContextWrapper(null)
        assertEquals(contextWrapper, unwrapUiContext(contextWrapper))
    }
}