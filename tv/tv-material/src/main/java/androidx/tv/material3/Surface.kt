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

package androidx.tv.material3

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.tokens.Elevation
import kotlinx.coroutines.launch

/**
 * The [Surface] is a building block component that will be used for any element on TV such as
 * buttons, cards, navigation, or a simple background etc. This non-interactive Surface is similar
 * to Compose Material's Surface composable
 *
 * @param modifier Modifier to be applied to the layout corresponding to the surface
 * @param tonalElevation When [color] is [ColorScheme.surface], a higher the elevation will result
 * in a darker color in light theme and lighter color in dark theme.
 * @param shape Defines the surface's shape.
 * @param colors Defines the background & content color to be used in this Surface.
 * See [NonInteractiveSurfaceDefaults.colors].
 * @param border Defines a border around the Surface.
 * @param glow Diffused shadow to be shown behind the Surface.
 * @param content defines the [Composable] content inside the surface
 */
@ExperimentalTvMaterial3Api
@NonRestartableComposable
@Composable
fun Surface(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 0.dp,
    shape: Shape = NonInteractiveSurfaceDefaults.shape,
    colors: NonInteractiveSurfaceColors = NonInteractiveSurfaceDefaults.colors(),
    border: Border = NonInteractiveSurfaceDefaults.border,
    glow: Glow = NonInteractiveSurfaceDefaults.glow,
    content: @Composable (BoxScope.() -> Unit)
) {
    SurfaceImpl(
        modifier = modifier,
        checked = false,
        enabled = true,
        tonalElevation = tonalElevation,
        shape = shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        scale = 1.0f,
        border = border,
        glow = glow,
        content = content
    )
}

/**
 * The [Surface] is a building block component that will be used for any focusable
 * element on TV such as buttons, cards, navigation, etc. This clickable Surface is similar to
 * Compose Material's Surface composable but will have more functionality that will make focus
 * management easier. [Surface] will automatically apply the relevant modifier(s) based on
 * the current interaction state.
 *
 * @param onClick callback to be called when the surface is clicked. Note: DPad Enter button won't
 * work if this value is null
 * @param modifier Modifier to be applied to the layout corresponding to the surface
 * @param onLongClick callback to be called when the surface is long clicked (long-pressed).
 * @param enabled Controls the enabled state of the surface. When `false`, this Surface will not be
 * clickable or focusable.
 * @param tonalElevation When [color] is [ColorScheme.surface], a higher the elevation will result
 * in a darker color in light theme and lighter color in dark theme.
 * @param shape Defines the surface's shape.
 * @param colors Defines the background & content colors to be used in this surface for different
 * interaction states. See [ClickableSurfaceDefaults.colors].
 * @param scale Defines size of the Surface relative to its original size.
 * @param border Defines a border around the Surface.
 * @param glow Diffused shadow to be shown behind the Surface.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this Surface. You can create and pass in your own remembered [MutableInteractionSource] if
 * you want to observe [Interaction]s and customize the appearance / behavior of this Surface in
 * different [Interaction]s.
 * @param content defines the [Composable] content inside the surface
 */
@ExperimentalTvMaterial3Api
@Composable
fun Surface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    tonalElevation: Dp = 0.dp,
    shape: ClickableSurfaceShape = ClickableSurfaceDefaults.shape(),
    colors: ClickableSurfaceColors = ClickableSurfaceDefaults.colors(),
    scale: ClickableSurfaceScale = ClickableSurfaceDefaults.scale(),
    border: ClickableSurfaceBorder = ClickableSurfaceDefaults.border(),
    glow: ClickableSurfaceGlow = ClickableSurfaceDefaults.glow(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (BoxScope.() -> Unit)
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    SurfaceImpl(
        modifier = modifier.tvClickable(
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interactionSource,
        ),
        checked = false,
        enabled = enabled,
        tonalElevation = tonalElevation,
        shape = ClickableSurfaceDefaults.shape(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            shape = shape
        ),
        color = ClickableSurfaceDefaults.containerColor(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            colors = colors
        ),
        contentColor = ClickableSurfaceDefaults.contentColor(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            colors = colors
        ),
        scale = ClickableSurfaceDefaults.scale(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            scale = scale
        ),
        border = ClickableSurfaceDefaults.border(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            border = border
        ),
        glow = ClickableSurfaceDefaults.glow(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            glow = glow
        ),
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * The Surface is a building block component that will be used for any focusable
 * element on TV such as buttons, cards, navigation, etc.
 *
 * This version of Surface is responsible for a toggling its checked state as well as everything
 * else that a regular Surface does:
 *
 * This version of surface will react to the check toggles, calling
 * [onCheckedChange] lambda, updating the [interactionSource] when [PressInteraction] occurs, and
 * showing ripple indication in response to press events. If you don't need check
 * handling, consider using a Surface function that doesn't require [onCheckedChange] param.
 *
 * To manually retrieve the content color inside a surface, use [LocalContentColor].
 *
 * @param checked whether or not this Surface is toggled on or off
 * @param onCheckedChange callback to be invoked when the toggleable Surface is clicked.
 * @param modifier [Modifier] to be applied to the layout corresponding to the surface
 * @param onLongClick callback to be called when the toggleable surface is long clicked
 * (long-pressed).
 * @param enabled Controls the enabled state of the surface. When `false`, this Surface will not be
 * clickable or focusable.
 * @param tonalElevation When [color] is [ColorScheme.surface], a higher the elevation will result
 * in a darker color in light theme and lighter color in dark theme.
 * @param shape Defines the surface's shape.
 * @param colors  Defines the background & content colors to be used in this surface for different
 * interaction states. See [ToggleableSurfaceDefaults.colors].
 * @param scale Defines size of the Surface relative to its original size.
 * @param border Defines a border around the Surface.
 * @param glow Diffused shadow to be shown behind the Surface.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this Surface. You can create and pass in your own remembered [MutableInteractionSource] if
 * you want to observe [Interaction]s and customize the appearance / behavior of this Surface in
 * different [Interaction]s.
 * @param content defines the [Composable] content inside the surface
 */
@ExperimentalTvMaterial3Api
@Composable
fun Surface(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    tonalElevation: Dp = Elevation.Level0,
    shape: ToggleableSurfaceShape = ToggleableSurfaceDefaults.shape(),
    colors: ToggleableSurfaceColors = ToggleableSurfaceDefaults.colors(),
    scale: ToggleableSurfaceScale = ToggleableSurfaceDefaults.scale(),
    border: ToggleableSurfaceBorder = ToggleableSurfaceDefaults.border(),
    glow: ToggleableSurfaceGlow = ToggleableSurfaceDefaults.glow(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (BoxScope.() -> Unit)
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    SurfaceImpl(
        modifier = modifier.tvToggleable(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onCheckedChange,
            interactionSource = interactionSource,
            onLongClick = onLongClick
        ),
        checked = checked,
        enabled = enabled,
        tonalElevation = tonalElevation,
        shape = ToggleableSurfaceDefaults.shape(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            shape = shape
        ),
        color = ToggleableSurfaceDefaults.containerColor(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            colors = colors
        ),
        contentColor = ToggleableSurfaceDefaults.contentColor(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            colors = colors
        ),
        scale = ToggleableSurfaceDefaults.scale(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            scale = scale
        ),
        border = ToggleableSurfaceDefaults.border(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            border = border
        ),
        glow = ToggleableSurfaceDefaults.glow(
            enabled = enabled,
            focused = focused,
            pressed = pressed,
            selected = checked,
            glow = glow
        ),
        interactionSource = interactionSource,
        content = content
    )
}

@ExperimentalTvMaterial3Api
@Composable
private fun SurfaceImpl(
    modifier: Modifier,
    checked: Boolean,
    enabled: Boolean,
    shape: Shape,
    color: Color,
    contentColor: Color,
    scale: Float,
    border: Border,
    glow: Glow,
    tonalElevation: Dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (BoxScope.() -> Unit)
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val surfaceAlpha = stateAlpha(
        enabled = enabled,
        focused = focused,
        pressed = pressed,
        selected = checked
    )

    val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation
    val contentColorAsAnim by animateColorAsState(
        targetValue = contentColor,
        label = "Surface.contentColor"
    )

    CompositionLocalProvider(
        LocalContentColor provides contentColorAsAnim,
        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        val zIndex by animateFloatAsState(
            targetValue = if (focused) FocusedZIndex else NonFocusedZIndex,
            label = "zIndex"
        )

        val backgroundColorByState = surfaceColorAtElevation(
            color = color,
            elevation = LocalAbsoluteTonalElevation.current
        )

        Box(
            modifier = modifier
                .indication(
                    interactionSource = interactionSource,
                    indication = remember(scale) { ScaleIndication(scale = scale) }
                )
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberGlowIndication(
                        color = surfaceColorAtElevation(
                            color = glow.elevationColor,
                            elevation = glow.elevation
                        ),
                        shape = shape,
                        glowBlurRadius = glow.elevation
                    )
                )
                // Increasing the zIndex of this Surface when it is in the focused state to
                // avoid the glowIndication from being overlapped by subsequent items if
                // this Surface is inside a list composable (like a Row/Column).
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0, zIndex = zIndex)
                    }
                }
                .then(
                    if (border != Border.None) {
                        Modifier.indication(
                            interactionSource = interactionSource,
                            indication = remember(border) { BorderIndication(border = border) }
                        )
                    } else Modifier
                )
                .drawWithCache {
                    onDrawBehind {
                        drawOutline(
                            outline = shape.createOutline(
                                size = size,
                                layoutDirection = layoutDirection,
                                density = Density(density, fontScale)
                            ),
                            color = backgroundColorByState
                        )
                    }
                }
                .graphicsLayer {
                    this.alpha = surfaceAlpha
                    this.shape = shape
                    this.clip = true
                },
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = if (!enabled) DisabledContentAlpha else EnabledContentAlpha
                },
                content = content
            )
        }
    }
}

/**
 * This modifier handles click, press, and focus events for a TV composable.
 * @param enabled decides whether [onClick] is executed
 * @param onClick executes the provided lambda on click.
 * @param onLongClick executes the provided lambda on long press.
 * @param interactionSource used to emit [PressInteraction] events
 */
private fun Modifier.tvClickable(
    enabled: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    interactionSource: MutableInteractionSource
) = handleDPadEnter(
    enabled = enabled,
    interactionSource = interactionSource,
    onClick = onClick,
    onLongClick = onLongClick
)
    // We are not using "clickable" modifier here because if we set "enabled" to false
    // then the Surface won't be focusable as well. But, in TV use case, a disabled surface
    // should be focusable
    .focusable(interactionSource = interactionSource)
    .semantics(mergeDescendants = true) {
        onClick {
            onClick?.let { nnOnClick ->
                nnOnClick()
                return@onClick true
            }
            false
        }
        onLongClick {
            onLongClick?.let { nnOnLongClick ->
                nnOnLongClick()
                return@onLongClick true
            }
            false
        }
        if (!enabled) {
            disabled()
        }
    }

/**
 * This modifier handles click, press, and focus events for a TV composable.
 * @param enabled decides whether [onCheckedChange] is executed
 * @param checked differentiates whether the current item is checked or unchecked
 * @param onCheckedChange executes the provided lambda on click, while returning the inverse state
 * of [checked].
 * @param onLongClick executes the provided lambda on long press.
 * @param interactionSource used to emit [PressInteraction] events
 */
private fun Modifier.tvToggleable(
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLongClick: (() -> Unit)?,
    interactionSource: MutableInteractionSource,
) = handleDPadEnter(
        enabled = enabled,
        interactionSource = interactionSource,
        checked = checked,
        onCheckedChanged = onCheckedChange,
        onLongClick = onLongClick
    )
    // We are not using "toggleable" modifier here because if we set "enabled" to false
    // then the Surface won't be focusable as well. But, in TV use case, a disabled surface
    // should be focusable
    .focusable(interactionSource = interactionSource)
    .semantics(mergeDescendants = true) {
        onClick {
            onCheckedChange(!checked)
            true
        }
        onLongClick {
            onLongClick?.let { nnOnLongClick ->
                nnOnLongClick()
                return@onLongClick true
            }
            false
        }
        if (!enabled) {
            disabled()
        }
    }

/**
 * This modifier is used to perform some actions when the user clicks the D-PAD enter button
 *
 * @param enabled if this is false, the D-PAD enter event is ignored
 * @param interactionSource used to emit [PressInteraction] events
 * @param onClick this lambda will be triggered on D-PAD enter event
 * @param onLongClick this lambda will be triggered when D-PAD enter is long pressed.
 * @param checked differentiates whether the current item is checked or unchecked
 * @param onCheckedChanged executes the provided lambda while returning the inverse state of
 * [checked]
 */
private fun Modifier.handleDPadEnter(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    checked: Boolean = false,
    onCheckedChanged: ((Boolean) -> Unit)? = null
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "handleDPadEnter"
        properties["enabled"] = enabled
        properties["interactionSource"] = interactionSource
        properties["onClick"] = onClick
        properties["onLongClick"] = onLongClick
        properties["checked"] = checked
        properties["onCheckedChanged"] = onCheckedChanged
    }
) {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val pressInteraction = remember { PressInteraction.Press(Offset.Zero) }
    var isLongClick by remember { mutableStateOf(false) }
    val isPressed by interactionSource.collectIsPressedAsState()

    this
        .onFocusChanged {
            if (!it.isFocused && isPressed) {
                coroutineScope.launch {
                    interactionSource.emit(PressInteraction.Release(pressInteraction))
                }
            }
        }
        .onKeyEvent { keyEvent ->
            if (AcceptableKeys.contains(keyEvent.nativeKeyEvent.keyCode)) {
                when (keyEvent.nativeKeyEvent.action) {
                    NativeKeyEvent.ACTION_DOWN -> {
                        when (keyEvent.nativeKeyEvent.repeatCount) {
                            0 ->
                                coroutineScope.launch {
                                    interactionSource.emit(pressInteraction)
                                }

                            1 ->
                                onLongClick?.let {
                                    isLongClick = true
                                    coroutineScope.launch {
                                        interactionSource.emit(
                                            PressInteraction.Release(
                                                pressInteraction
                                            )
                                        )
                                    }
                                    it.invoke()
                                }
                        }
                    }

                    NativeKeyEvent.ACTION_UP -> {
                        if (!isLongClick) {
                            coroutineScope.launch {
                                interactionSource.emit(PressInteraction.Release(pressInteraction))
                            }
                            onClick?.invoke()
                            onCheckedChanged?.invoke(!checked)
                        } else isLongClick = false
                    }
                }
                return@onKeyEvent KeyEventPropagation.StopPropagation
            }
            KeyEventPropagation.ContinuePropagation
        }
}

@Composable
@ExperimentalTvMaterial3Api
private fun surfaceColorAtElevation(color: Color, elevation: Dp): Color {
    return if (color == MaterialTheme.colorScheme.surface) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
    } else {
        color
    }
}

/**
 * Returns the alpha value for Surface's background based on its current indication state. The
 * value ranges between 0f and 1f.
 */
private fun stateAlpha(
    enabled: Boolean,
    focused: Boolean,
    pressed: Boolean,
    selected: Boolean
): Float {
    return when {
        !enabled && pressed -> DisabledPressedStateAlpha
        !enabled && focused -> DisabledFocusedStateAlpha
        !enabled && selected -> DisabledSelectedStateAlpha
        enabled -> EnabledContentAlpha
        else -> DisabledDefaultStateAlpha
    }
}

private const val DisabledPressedStateAlpha = 0.8f
private const val DisabledFocusedStateAlpha = 0.8f
private const val DisabledSelectedStateAlpha = 0.8f
private const val DisabledDefaultStateAlpha = 0.6f

private const val FocusedZIndex = 0.5f
private const val NonFocusedZIndex = 0f

private const val DisabledContentAlpha = 0.8f
internal const val EnabledContentAlpha = 1f

/**
 * CompositionLocal containing the current absolute elevation provided by Surface components. This
 * absolute elevation is a sum of all the previous elevations. Absolute elevation is only used for
 * calculating surface tonal colors, and is *not* used for drawing the shadow in a [SurfaceImpl].
 */
val LocalAbsoluteTonalElevation = compositionLocalOf { 0.dp }
private val AcceptableKeys = hashSetOf(
    NativeKeyEvent.KEYCODE_DPAD_CENTER,
    NativeKeyEvent.KEYCODE_ENTER,
    NativeKeyEvent.KEYCODE_NUMPAD_ENTER
)
