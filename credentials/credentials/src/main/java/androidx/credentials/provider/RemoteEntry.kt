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
package androidx.credentials.provider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.slice.Slice
import android.app.slice.SliceSpec
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import java.util.Collections

/**
 * An entry on the selector, denoting that the credential request will be completed on a remote
 * device.
 *
 * Once this entry is selected, the corresponding [pendingIntent] will be invoked. The provider
 * can then show any activity they wish to while establishing a connection with a different
 * device and retrieving a credential. Before finishing the activity, provider must
 * set the final [androidx.credentials.GetCredentialResponse] through the
 * [PendingIntentHandler.setGetCredentialResponse] helper API, or a
 * [androidx.credentials.CreateCredentialResponse] through the
 * [PendingIntentHandler.setCreateCredentialResponse] helper API depending on whether it is a get
 * or create flow.
 *
 * See [android.service.credentials.BeginGetCredentialResponse] for usage details.
 *
 * @constructor constructs an instance of [RemoteEntry]
 *
 * @param pendingIntent the [PendingIntent] that will get invoked when the user selects this
 * authentication entry on the UI, must be created with flag [PendingIntent.FLAG_MUTABLE] so
 * that the system can add the complete request to the extras of the associated intent
 *
 * @throws NullPointerException If [pendingIntent] is null
 */
class RemoteEntry constructor(
    val pendingIntent: PendingIntent
) {
    /**
     * A builder for [RemoteEntry]
     *
     * @param pendingIntent the [PendingIntent] that will get invoked when the user selects this
     * entry, must be created with flag [PendingIntent.FLAG_MUTABLE] to allow the Android
     * system to attach the final request
     */
    class Builder constructor(
        private val pendingIntent: PendingIntent
    ) {
        /**
         * Builds an instance of [RemoteEntry]
         */
        fun build(): RemoteEntry {
            return RemoteEntry(pendingIntent)
        }
    }

    internal companion object {
        private const val TAG = "RemoteEntry"

        private const val SLICE_HINT_PENDING_INTENT =
            "androidx.credentials.provider.remoteEntry.SLICE_HINT_PENDING_INTENT"

        private const val SLICE_SPEC_TYPE = "RemoteEntry"

        private const val REVISION_ID = 1

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @RequiresApi(28)
        @JvmStatic
        fun toSlice(
            remoteEntry: RemoteEntry
        ): Slice {
            val pendingIntent = remoteEntry.pendingIntent
            val sliceBuilder = Slice.Builder(Uri.EMPTY, SliceSpec(SLICE_SPEC_TYPE, REVISION_ID))
            sliceBuilder.addAction(
                pendingIntent,
                Slice.Builder(sliceBuilder)
                    .addHints(Collections.singletonList(SLICE_HINT_PENDING_INTENT))
                    .build(), /*subType=*/null
            )
            return sliceBuilder.build()
        }

        /**
         * Returns an instance of [RemoteEntry] derived from a [Slice] object.
         *
         * @param slice the [Slice] object constructed through [toSlice]
         *
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @RequiresApi(28)
        @SuppressLint("WrongConstant") // custom conversion between jetpack and framework
        @JvmStatic
        fun fromSlice(slice: Slice): RemoteEntry? {
            var pendingIntent: PendingIntent? = null
            slice.items.forEach {
                if (it.hasHint(SLICE_HINT_PENDING_INTENT)) {
                    pendingIntent = it.action
                }
            }
            return try {
                RemoteEntry(pendingIntent!!)
            } catch (e: Exception) {
                Log.i(TAG, "fromSlice failed with: " + e.message)
                null
            }
        }
    }
}