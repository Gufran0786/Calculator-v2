package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.AppThemeMode

enum class GlassButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS,
    SCIENTIFIC
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: GlassButtonType = GlassButtonType.NUMBER,
    theme: AppThemeMode = AppThemeMode.FROSTED_DARK,
    isHapticEnabled: Boolean = true,
    testTag: String = "btn_$text",
    fontSize: TextUnit? = null,
    aspectRatio: Float? = 1f,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(22.dp),
    isSelected: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "btn_scale"
    )

    val backgroundBrush = when (theme) {
        AppThemeMode.AMOLED -> when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFF0369A1).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.SCIENTIFIC -> Brush.verticalGradient(
                colors = listOf(
                    Color(if (isPressed) 0xFF27272A else 0xFF18181B),
                    Color(if (isPressed) 0xFF1F1F23 else 0xFF0F0F12)
                )
            )
            type == GlassButtonType.NUMBER -> Brush.verticalGradient(
                colors = listOf(
                    Color(if (isPressed) 0xFF27272A else 0xFF141416),
                    Color(if (isPressed) 0xFF1F1F23 else 0xFF0D0D0F)
                )
            )
            type == GlassButtonType.OPERATOR -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF9F0A).copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFFEA580C).copy(alpha = if (isPressed) 0.90f else 0.78f)
                )
            )
            type == GlassButtonType.ACTION -> Brush.verticalGradient(
                colors = listOf(
                    Color(if (isPressed) 0xFF3F3F46 else 0xFF27272A),
                    Color(if (isPressed) 0xFF27272A else 0xFF18181B)
                )
            )
            type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = if (isPressed) 0.98f else 0.90f),
                    Color(0xFF2563EB).copy(alpha = if (isPressed) 0.95f else 0.85f)
                )
            )
            else -> Brush.verticalGradient(listOf(Color(0xFF18181B), Color(0xFF0F0F12)))
        }
        AppThemeMode.LIGHT -> when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2563EB).copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFF1D4ED8).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.SCIENTIFIC -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE2E8F0).copy(alpha = if (isPressed) 0.95f else 0.80f),
                    Color(0xFFF1F5F9).copy(alpha = if (isPressed) 0.90f else 0.70f)
                )
            )
            type == GlassButtonType.NUMBER -> Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFFF1F5F9).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.OPERATOR -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF97316).copy(alpha = if (isPressed) 0.98f else 0.90f),
                    Color(0xFFEA580C).copy(alpha = if (isPressed) 0.95f else 0.82f)
                )
            )
            type == GlassButtonType.ACTION -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE2E8F0).copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFFCBD5E1).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2563EB).copy(alpha = if (isPressed) 0.98f else 0.90f),
                    Color(0xFF1D4ED8).copy(alpha = if (isPressed) 0.95f else 0.82f)
                )
            )
            else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F5F9)))
        }
        AppThemeMode.NEON -> when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06B6D4).copy(alpha = if (isPressed) 0.95f else 0.85f),
                    Color(0xFF3B82F6).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.SCIENTIFIC -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF312E81).copy(alpha = if (isPressed) 0.60f else 0.35f),
                    Color(0xFF1E1B4B).copy(alpha = if (isPressed) 0.50f else 0.25f)
                )
            )
            type == GlassButtonType.NUMBER -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E1B4B).copy(alpha = if (isPressed) 0.60f else 0.35f),
                    Color(0xFF0F172A).copy(alpha = if (isPressed) 0.50f else 0.25f)
                )
            )
            type == GlassButtonType.OPERATOR -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFEC4899).copy(alpha = if (isPressed) 0.95f else 0.82f),
                    Color(0xFFD946EF).copy(alpha = if (isPressed) 0.90f else 0.75f)
                )
            )
            type == GlassButtonType.ACTION -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4C1D95).copy(alpha = if (isPressed) 0.60f else 0.40f),
                    Color(0xFF312E81).copy(alpha = if (isPressed) 0.50f else 0.30f)
                )
            )
            type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06B6D4).copy(alpha = if (isPressed) 0.98f else 0.88f),
                    Color(0xFF3B82F6).copy(alpha = if (isPressed) 0.95f else 0.80f)
                )
            )
            else -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)))
        }
        AppThemeMode.FROSTED_DARK -> when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = if (isPressed) 0.95f else 0.80f),
                    Color(0xFF0369A1).copy(alpha = if (isPressed) 0.85f else 0.65f)
                )
            )
            type == GlassButtonType.SCIENTIFIC -> Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isPressed) 0.25f else 0.14f),
                    Color.White.copy(alpha = if (isPressed) 0.15f else 0.06f)
                )
            )
            type == GlassButtonType.NUMBER -> Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isPressed) 0.28f else 0.16f),
                    Color.White.copy(alpha = if (isPressed) 0.18f else 0.08f)
                )
            )
            type == GlassButtonType.OPERATOR -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF9F0A).copy(alpha = if (isPressed) 0.95f else 0.82f),
                    Color(0xFFFF7A00).copy(alpha = if (isPressed) 0.90f else 0.72f)
                )
            )
            type == GlassButtonType.ACTION -> Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isPressed) 0.38f else 0.24f),
                    Color.White.copy(alpha = if (isPressed) 0.28f else 0.14f)
                )
            )
            type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = if (isPressed) 0.98f else 0.88f),
                    Color(0xFF2563EB).copy(alpha = if (isPressed) 0.95f else 0.80f)
                )
            )
            else -> Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.06f)))
        }
    }

    val borderBrush = when (theme) {
        AppThemeMode.AMOLED -> if (isSelected) Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
        else Brush.verticalGradient(listOf(Color(0xFF3F3F46), Color(0xFF18181B)))
        AppThemeMode.LIGHT -> if (isSelected) Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))
        else Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.04f)))
        AppThemeMode.NEON -> when {
            isSelected -> Brush.verticalGradient(listOf(Color(0xFF22D3EE), Color(0xFF38BDF8)))
            type == GlassButtonType.OPERATOR || type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                listOf(Color(0xFF22D3EE), Color(0xFFE879F9))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFF818CF8).copy(0.5f), Color(0xFF38BDF8).copy(0.2f))
            )
        }
        AppThemeMode.FROSTED_DARK -> when {
            isSelected -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
            type == GlassButtonType.SCIENTIFIC -> Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.10f))
            )
            type == GlassButtonType.NUMBER -> Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.10f))
            )
            type == GlassButtonType.OPERATOR -> Brush.verticalGradient(
                listOf(Color(0xFFFFD180).copy(alpha = 0.6f), Color(0xFFFF9F0A).copy(alpha = 0.2f))
            )
            type == GlassButtonType.ACTION -> Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.50f), Color.White.copy(alpha = 0.15f))
            )
            type == GlassButtonType.EQUALS -> Brush.verticalGradient(
                listOf(Color(0xFFBAE6FD).copy(alpha = 0.7f), Color(0xFF38BDF8).copy(alpha = 0.3f))
            )
            else -> Brush.verticalGradient(listOf(Color.White.copy(0.3f), Color.White.copy(0.1f)))
        }
    }

    val textColor = when {
        isSelected -> Color.White
        theme == AppThemeMode.LIGHT && (type == GlassButtonType.NUMBER || type == GlassButtonType.ACTION || type == GlassButtonType.SCIENTIFIC) -> Color(0xFF0F172A)
        else -> Color.White
    }

    val textSize = fontSize ?: when (type) {
        GlassButtonType.SCIENTIFIC -> 15.sp
        GlassButtonType.ACTION -> 22.sp
        GlassButtonType.OPERATOR, GlassButtonType.EQUALS -> 28.sp
        GlassButtonType.NUMBER -> 26.sp
    }

    val boxModifier = modifier
        .scale(scale)
        .let { if (aspectRatio != null) it.aspectRatio(aspectRatio) else it }
        .clip(shape)
        .background(backgroundBrush, shape)
        .border(1.dp, borderBrush, shape)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (isHapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }
        )
        .testTag(testTag)

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = textSize,
                fontWeight = if (type == GlassButtonType.EQUALS || type == GlassButtonType.OPERATOR || isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                }
            )
        }
    }
}
