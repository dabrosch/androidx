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

package androidx.privacysandbox.ads.adservices.customaudience

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class TrustedBiddingDataTest {
    private val uri = Uri.parse("abc.com")
    private val keys = listOf("key1", "key2")
    @Test
    fun testToString() {
        val result = "TrustedBiddingData: trustedBiddingUri=abc.com trustedBiddingKeys=[key1, key2]"
        val trustedBiddingData = TrustedBiddingData(uri, keys)
        Truth.assertThat(trustedBiddingData.toString()).isEqualTo(result)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Test
    fun testBuilderSetters() {
        val constructed = TrustedBiddingData(uri, keys)
        val builder = TrustedBiddingData.Builder()
            .setTrustedBiddingUri(uri)
            .setTrustedBiddingKeys(keys)
        Truth.assertThat(builder.build()).isEqualTo(constructed)
    }
}