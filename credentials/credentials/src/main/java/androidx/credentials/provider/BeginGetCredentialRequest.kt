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

package androidx.credentials.provider

import android.os.Build
import android.os.Bundle
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.credentials.provider.utils.BeginGetCredentialUtil

/**
 * Query stage request for getting user's credentials from a given credential provider.
 *
 * This request contains a list of [BeginGetCredentialOption] that have parameters
 * to be used to query credentials, and return a [BeginGetCredentialResponse], containing
 * a list [CredentialEntry] that are presented to the user on an selector.
 *
 * Note : Credential providers are not expected to utilize the constructor in this class for any
 * production flow. This constructor must only be used for testing purposes.
 *
 * @constructor constructs an instance of [BeginGetCredentialRequest]
 *
 * @param beginGetCredentialOptions the list of type specific credential options to to be processed
 * in order to produce a [BeginGetCredentialResponse]
 * @param callingAppInfo info pertaining to the app requesting credentials
 *
 * @throws NullPointerException If [beginGetCredentialOptions] is null
 */
class BeginGetCredentialRequest @JvmOverloads constructor(
    val beginGetCredentialOptions: List<BeginGetCredentialOption>,
    val callingAppInfo: CallingAppInfo? = null,
) {
    @RequiresApi(34)
    private object Api34Impl {
        private const val REQUEST_KEY = "androidx.credentials.provider.BeginGetCredentialRequest"

        @JvmStatic
        @DoNotInline
        fun asBundle(bundle: Bundle, request: BeginGetCredentialRequest) {
            bundle.putParcelable(
                REQUEST_KEY,
                BeginGetCredentialUtil.convertToFrameworkRequest(request)
            )
        }

        @JvmStatic
        @DoNotInline
        fun fromBundle(bundle: Bundle): BeginGetCredentialRequest? {
            val frameworkRequest = bundle.getParcelable(
                REQUEST_KEY,
                android.service.credentials.BeginGetCredentialRequest::class.java
            )
            if (frameworkRequest != null) {
                return BeginGetCredentialUtil.convertToJetpackRequest(frameworkRequest)
            }
            return null
        }
    }

    companion object {
        /**
         * Helper method to convert the class to a parcelable [Bundle], in case the class
         * instance needs to be sent across a process. Consumers of this method should use
         * [fromBundle] to reconstruct the class instance back from the bundle returned here.
         */
        @JvmStatic
        fun asBundle(request: BeginGetCredentialRequest): Bundle {
            val bundle = Bundle()
            if (Build.VERSION.SDK_INT >= 34) { // Android U
                Api34Impl.asBundle(bundle, request)
            }
            return bundle
        }

        /**
         * Helper method to convert a [Bundle] retrieved through [asBundle], back
         * to an instance of [BeginGetCredentialRequest].
         */
        @JvmStatic
        fun fromBundle(bundle: Bundle): BeginGetCredentialRequest? {
            return if (Build.VERSION.SDK_INT >= 34) { // Android U
                Api34Impl.fromBundle(bundle)
            } else {
                null
            }
        }
    }
}