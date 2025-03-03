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

package androidx.appactions.interaction.capabilities.serializers.types

import androidx.appactions.builtintypes.types.DayOfWeek
import androidx.appactions.interaction.capabilities.core.impl.converters.TypeSpec
import androidx.appactions.interaction.capabilities.core.impl.exceptions.StructConversionException

private val supportedDayOfWeekValues = listOf(
  DayOfWeek.FRIDAY, DayOfWeek.MONDAY,
  DayOfWeek.PUBLIC_HOLIDAYS,
  DayOfWeek.SATURDAY,
  DayOfWeek.SUNDAY,
  DayOfWeek.THURSDAY,
  DayOfWeek.TUESDAY,
  DayOfWeek.WEDNESDAY
)

val DAY_OF_WEEK_TYPE_SPEC = TypeSpec.createStringBasedTypeSpec<DayOfWeek>(
  toString = DayOfWeek::canonicalUrl,
  fromString = {
    str ->
    supportedDayOfWeekValues.find {
      it.canonicalUrl == str
    } ?: throw StructConversionException(
      "failed to deserialize DayOfWeek with canonicalUrl=$str"
    )
  }
)