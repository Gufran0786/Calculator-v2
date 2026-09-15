package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.CalculatorEvaluator
import com.example.calculator.engine.EvalResult
import kotlin.math.roundToInt

@Composable
fun FloatingMiniCalculator(
    isHapticEnabled: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(160f) }

    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var isEval by remember { mutableStateOf(false) }

    fun vibrate() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun onKey(key: String) {
        vibrate()
        when (key) {
            "C" -> {
                expr = ""
                result = null
                isEval = false
            }
            "⌫" -> {
                if (expr.isNotEmpty()) {
                    expr = expr.dropLast(1)
                    isEval = false
                    result = null
                }
            }
            "=" -> {
                if (expr.isNotEmpty()) {
                    when (val res = CalculatorEvaluator.evaluate(expr)) {
                        is EvalResult.Success -> {
                            result = res.value
                            isEval = true
                        }
                        is EvalResult.Error -> {
                            result = "Error"
                        }
                    }
                }
            }
            "+", "−", "×", "÷" -> {
                if (isEval && result != null && result != "Error") {
                    expr = result!!.replace(",", "") + key
                    isEval = false
                    result = null
                } else if (expr.isNotEmpty() && expr.last() in listOf('+', '−', '×', '÷', '-')) {
                    expr = expr.dropLast(1) + key
                } else {
                    expr += key
                }
            }
            else -> { // Digits & decimal
                if (isEval) {
                    expr = key
                    isEval = false
                    result = null
                } else {
                    expr += key
                }
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(280.dp)
            .shadow(24.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF0111827))
            .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .testTag("floating_mini_calculator")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Drag Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = "Drag Window",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Floating Mini Calc",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        vibrate()
                        onClose()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Floating",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (expr.isEmpty()) "0" else expr,
                        color = Color.White.copy(alpha = if (isEval) 0.6f else 1f),
                        fontSize = if (expr.length > 12) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                    if (result != null) {
                        Text(
                            text = "= $result",
                            color = Color(0xFF38BDF8),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini 4x5 Keypad
            val buttons = listOf(
                listOf("C", "÷", "×", "⌫"),
                listOf("7", "8", "9", "−"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "="),
                listOf("0", ".", "%", "")
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        for (btn in row) {
                            if (btn.isEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isOp = btn in listOf("÷", "×", "−", "+", "=")
                                val isAction = btn in listOf("C", "⌫")

                                val bg = when {
                                    btn == "=" -> Color(0xFF2563EB)
                                    isOp -> Color(0xFF0284C7).copy(alpha = 0.6f)
                                    isAction -> Color(0xFFDC2626).copy(alpha = 0.4f)
                                    else -> Color.White.copy(alpha = 0.10f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { onKey(btn) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (btn == "⌫") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    } else {
                                        Text(
                                            text = btn,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
