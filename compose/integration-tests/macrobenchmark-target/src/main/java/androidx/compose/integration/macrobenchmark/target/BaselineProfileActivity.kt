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

package androidx.compose.integration.macrobenchmark.target

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

class BaselineProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemCount = intent.getIntExtra(EXTRA_ITEM_COUNT, 3000)
        val entries = List(itemCount) { Entry("Item $it") }

        setContent {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "IamLazy" }
            ) {
                itemsIndexed(entries) { index, item ->
                    if (index % 2 == 0) {
                        M3ListRow(entry = item)
                    } else {
                        ListRow(item)
                    }
                }
            }
        }

        launchIdlenessTracking()
    }

    companion object {
        const val EXTRA_ITEM_COUNT = "ITEM_COUNT"
    }
}

@Composable
private fun ListRow(entry: Entry) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row {
            Text(
                text = entry.contents,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            Checkbox(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
internal fun M3ListRow(entry: Entry) {
    androidx.compose.material3.Card(modifier = Modifier.padding(8.dp)) {
        Row {
            androidx.compose.material3.Text(
                text = entry.contents,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            androidx.compose.material3.Checkbox(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}