package com.example.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.converters.model.ConverterGroup
import com.example.converters.model.ConverterType
import com.example.ui.components.BackgroundSettingsSheet
import com.example.ui.components.FloatingMiniCalculator
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonType
import com.example.ui.components.GlassSurface
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.converters.ConverterCatalogSheet
import com.example.ui.converters.FinanceScreen
import com.example.ui.converters.SpecialConverterScreen
import com.example.ui.converters.UnitConverterScreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    isInPipMode: Boolean = false,
    onEnterPip: (() -> Boolean)? = null,
    viewModel: CalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val appMode by viewModel.appMode.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val isFloatingMiniOpen by viewModel.isFloatingMiniOpen.collectAsState()

    val selectedConverter by viewModel.selectedConverter.collectAsState()
    val isCatalogOpen by viewModel.isCatalogOpen.collectAsState()

    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val result by viewModel.result.collectAsState()
    val isEvaluated by viewModel.isEvaluated.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val customBgPath by viewModel.customBackgroundPath.collectAsState()
    val blurRadius by viewModel.blurRadius.collectAsState()
    val overlayDarkness by viewModel.overlayDarkness.collectAsState()
    val isHistoryOpen by viewModel.isHistoryOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isScientificMode by viewModel.isScientificMode.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()
    val isInvMode by viewModel.isInvMode.collectAsState()

    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val catalogSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scrollState = rememberScrollState()

    LaunchedEffect(expression, result) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val triggerHaptic = {
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // In Picture-in-Picture mode, render compact PiP view
    if (isInPipMode) {
        FloatingPipMiniView(
            expression = expression,
            result = result,
            isEvaluated = isEvaluated,
            onDigit = { viewModel.onDigit(it) },
            onOperator = { viewModel.onOperator(it) },
            onEquals = { viewModel.onCalculate() },
            onClear = { viewModel.onClear() },
            onBackspace = { viewModel.onBackspace() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (appTheme) {
                    AppThemeMode.AMOLED -> Color.Black
                    AppThemeMode.LIGHT -> Color(0xFFF1F5F9)
                    AppThemeMode.NEON -> Color(0xFF030712)
                    AppThemeMode.FROSTED_DARK -> Color.Black
                }
            )
    ) {
        // Background Image Layer with Blur (Only for themes that support wallpaper)
        if (appTheme != AppThemeMode.AMOLED) {
            val imageModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                Modifier
                    .fillMaxSize()
                    .blur(blurRadius.dp)
            } else {
                Modifier.fillMaxSize()
            }

            if (customBgPath != null && File(customBgPath!!).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(customBgPath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.default_wallpaper)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }

            // Tint / Darkness scrim overlay for frosted glass readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (appTheme == AppThemeMode.LIGHT) {
                                listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.95f)
                                )
                            } else {
                                listOf(
                                    Color.Black.copy(alpha = overlayDarkness * 0.85f),
                                    Color.Black.copy(alpha = overlayDarkness),
                                    Color.Black.copy(alpha = overlayDarkness * 1.15f).copy(alpha = 0.85f.coerceAtMost(overlayDarkness * 1.25f))
                                )
                            }
                        )
                    )
            )
        }

        // Main Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Toggle / Converter Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Calculator tab
                    GlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = if (appMode == AppMode.CALCULATOR) Color(0xFF2563EB).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.12f),
                        borderColor = if (appMode == AppMode.CALCULATOR) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setAppMode(AppMode.CALCULATOR) }
                            .testTag("btn_mode_calculator")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calc",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Converters tab / Active Converter Pill
                    GlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = if (appMode == AppMode.CONVERTER) Color(0xFF0284C7).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.12f),
                        borderColor = if (appMode == AppMode.CONVERTER) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (appMode == AppMode.CONVERTER) {
                                    viewModel.setCatalogOpen(true)
                                } else {
                                    viewModel.setAppMode(AppMode.CONVERTER)
                                }
                            }
                            .testTag("btn_mode_converter")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appMode == AppMode.CONVERTER) "${selectedConverter.title} ▾" else "Tools (${ConverterType.values().size})",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Icons (PiP/Floating + History + Theme)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Floating Window / Picture-in-Picture Button
                    GlassSurface(
                        shape = CircleShape,
                        backgroundColor = if (isFloatingMiniOpen) Color(0xFF2563EB).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f),
                        borderColor = if (isFloatingMiniOpen) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f)
                    ) {
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                val entered = onEnterPip?.invoke() ?: false
                                if (!entered) {
                                    viewModel.setFloatingMiniOpen(!isFloatingMiniOpen)
                                    Toast.makeText(
                                        context,
                                        if (!isFloatingMiniOpen) "Floating Mini Window Opened" else "Floating Mini Window Closed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_pip_floating")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Floating Window / PiP",
                                tint = if (isFloatingMiniOpen) Color(0xFF38BDF8) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (appMode == AppMode.CALCULATOR) {
                        // History Icon with Badge
                        GlassSurface(
                            shape = CircleShape,
                            backgroundColor = Color.White.copy(alpha = 0.12f),
                            borderColor = Color.White.copy(alpha = 0.22f)
                        ) {
                            IconButton(
                                onClick = { viewModel.setHistoryOpen(true) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("btn_history")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (historyList.isNotEmpty()) {
                                            Badge(
                                                containerColor = Color(0xFF38BDF8),
                                                contentColor = Color(0xFF0F172A)
                                            ) {
                                                Text(
                                                    text = historyList.size.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = stringResource(R.string.history_title),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Open Tools Catalog sheet icon
                        GlassSurface(
                            shape = CircleShape,
                            backgroundColor = Color.White.copy(alpha = 0.12f),
                            borderColor = Color.White.copy(alpha = 0.22f)
                        ) {
                            IconButton(
                                onClick = { viewModel.setCatalogOpen(true) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("btn_open_catalog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "All Tools",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Background Settings Icon
                    GlassSurface(
                        shape = CircleShape,
                        backgroundColor = Color.White.copy(alpha = 0.12f),
                        borderColor = Color.White.copy(alpha = 0.22f)
                    ) {
                        IconButton(
                            onClick = { viewModel.setSettingsOpen(true) },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_theme_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = stringResource(R.string.change_background),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Content based on AppMode
            if (appMode == AppMode.CONVERTER) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedConverter.group) {
                        ConverterGroup.UNIT -> UnitConverterScreen(selectedConverter)
                        ConverterGroup.SPECIAL -> SpecialConverterScreen(selectedConverter)
                        ConverterGroup.FINANCE -> FinanceScreen(selectedConverter)
                    }
                }
            } else {
                // Calculator Display + Keypad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Display Screen Area (Frosted Glass Container)
                    val displayBg = when (appTheme) {
                        AppThemeMode.AMOLED -> Color(0xFF09090B)
                        AppThemeMode.LIGHT -> Color.White.copy(alpha = 0.85f)
                        AppThemeMode.NEON -> Color(0xFF0F172A).copy(alpha = 0.70f)
                        AppThemeMode.FROSTED_DARK -> Color.White.copy(alpha = 0.10f)
                    }
                    val displayBorder = when (appTheme) {
                        AppThemeMode.AMOLED -> Color(0xFF27272A)
                        AppThemeMode.LIGHT -> Color.Black.copy(alpha = 0.10f)
                        AppThemeMode.NEON -> Color(0xFF38BDF8).copy(alpha = 0.50f)
                        AppThemeMode.FROSTED_DARK -> Color.White.copy(alpha = 0.20f)
                    }
                    val primaryTextColor = if (appTheme == AppThemeMode.LIGHT) Color(0xFF0F172A) else Color.White

                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("display_screen"),
                        shape = RoundedCornerShape(26.dp),
                        backgroundColor = displayBg,
                        borderColor = displayBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.End
                        ) {
                            // Expression line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = if (isEvaluated && result != null) {
                                        result!!
                                    } else if (expression.isEmpty()) {
                                        "0"
                                    } else {
                                        expression
                                    },
                                    color = primaryTextColor,
                                    fontSize = if ((expression.length) > 10) 34.sp else 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .testTag("text_expression")
                                        .clickable {
                                            triggerHaptic()
                                            val textToCopy = if (isEvaluated && result != null) result!! else if (expression.isNotEmpty()) expression else "0"
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            Toast.makeText(context, "Copied: $textToCopy", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Preview result or Error message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (previewResult != null && !isEvaluated) {
                                    Text(
                                        text = "= ${previewResult ?: ""}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.testTag("text_preview")
                                    )
                                } else if (errorMessage != null) {
                                    Text(
                                        text = errorMessage ?: "",
                                        color = Color(0xFFF87171),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.testTag("text_error")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Copy, Paste, Theme label & Backspace Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Copy Button
                                    IconButton(
                                        onClick = {
                                            triggerHaptic()
                                            val textToCopy = if (isEvaluated && result != null) result!! else if (expression.isNotEmpty()) expression else "0"
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            Toast.makeText(context, "Copied to clipboard: $textToCopy", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (appTheme == AppThemeMode.LIGHT) Color.Black.copy(0.06f) else Color.White.copy(0.12f))
                                            .testTag("btn_copy")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Result",
                                            tint = primaryTextColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Paste Button
                                    IconButton(
                                        onClick = {
                                            triggerHaptic()
                                            val clipText = clipboardManager.getText()?.text
                                            if (!clipText.isNullOrBlank()) {
                                                viewModel.onPasteExpression(clipText)
                                                Toast.makeText(context, "Pasted: $clipText", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (appTheme == AppThemeMode.LIGHT) Color.Black.copy(0.06f) else Color.White.copy(0.12f))
                                            .testTag("btn_paste")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste text to calculator",
                                            tint = primaryTextColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = when (appTheme) {
                                            AppThemeMode.AMOLED -> "AMOLED Dark"
                                            AppThemeMode.LIGHT -> "Clean Light"
                                            AppThemeMode.NEON -> "Neon Mode"
                                            AppThemeMode.FROSTED_DARK -> if (customBgPath != null) "Custom Wallpaper" else "Frosted Glass"
                                        },
                                        color = primaryTextColor.copy(alpha = 0.45f),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.onBackspace()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("btn_backspace")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = primaryTextColor.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mode Control Strip (Scientific Mode Toggle, DEG/RAD, 2nd)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle Scientific Mode Button
                        GlassSurface(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = if (isScientificMode) Color(0xFF0284C7).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f),
                            borderColor = if (isScientificMode) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    triggerHaptic()
                                    viewModel.toggleScientificMode()
                                }
                                .testTag("btn_toggle_scientific")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = "Toggle Scientific Mode",
                                    tint = if (isScientificMode) Color(0xFF38BDF8) else primaryTextColor.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isScientificMode) "Scientific" else "Scientific",
                                    color = if (isScientificMode) Color.White else primaryTextColor.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isScientificMode) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // Angle & Inverse toggles when Scientific Mode is active
                        if (isScientificMode) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // DEG / RAD Toggle Pill
                                GlassSurface(
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = if (isDegreeMode) Color(0xFF0284C7).copy(alpha = 0.40f) else Color(0xFF7C3AED).copy(alpha = 0.40f),
                                    borderColor = if (isDegreeMode) Color(0xFF38BDF8).copy(alpha = 0.7f) else Color(0xFFA78BFA).copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            triggerHaptic()
                                            viewModel.toggleDegreeMode()
                                        }
                                        .testTag("btn_toggle_deg_rad")
                                ) {
                                    Text(
                                        text = if (isDegreeMode) "DEG" else "RAD",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                // 2nd (Inverse) Toggle Pill
                                GlassSurface(
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = if (isInvMode) Color(0xFFF59E0B).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f),
                                    borderColor = if (isInvMode) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            triggerHaptic()
                                            viewModel.toggleInvMode()
                                        }
                                        .testTag("btn_toggle_inv")
                                ) {
                                    Text(
                                        text = "2nd",
                                        color = if (isInvMode) Color(0xFFFEF3C7) else Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    @Composable
                    fun SciButton(
                        text: String,
                        onClick: () -> Unit,
                        isSelected: Boolean = false,
                        modifier: Modifier = Modifier,
                        testTag: String = "btn_sci_$text"
                    ) {
                        GlassButton(
                            text = text,
                            onClick = onClick,
                            type = GlassButtonType.SCIENTIFIC,
                            theme = appTheme,
                            isHapticEnabled = hapticEnabled,
                            fontSize = 13.sp,
                            aspectRatio = null,
                            shape = RoundedCornerShape(12.dp),
                            isSelected = isSelected,
                            modifier = modifier.height(38.dp),
                            testTag = testTag
                        )
                    }

                    // Expandable Scientific Keypad (Trigonometric, Logarithmic, Power functions)
                    AnimatedVisibility(
                        visible = isScientificMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("scientific_keypad"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Row 1: sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, ln/eˣ, log/10ˣ
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SciButton(
                                    text = if (isInvMode) "sin⁻¹" else "sin",
                                    onClick = { viewModel.onScientificFunction(if (isInvMode) "sin⁻¹" else "sin") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_sin"
                                )
                                SciButton(
                                    text = if (isInvMode) "cos⁻¹" else "cos",
                                    onClick = { viewModel.onScientificFunction(if (isInvMode) "cos⁻¹" else "cos") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_cos"
                                )
                                SciButton(
                                    text = if (isInvMode) "tan⁻¹" else "tan",
                                    onClick = { viewModel.onScientificFunction(if (isInvMode) "tan⁻¹" else "tan") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_tan"
                                )
                                SciButton(
                                    text = if (isInvMode) "eˣ" else "ln",
                                    onClick = {
                                        if (isInvMode) viewModel.onScientificFunction("eˣ")
                                        else viewModel.onScientificFunction("ln")
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_ln"
                                )
                                SciButton(
                                    text = if (isInvMode) "10ˣ" else "log",
                                    onClick = {
                                        if (isInvMode) {
                                            viewModel.onDigit("10")
                                            viewModel.onPower()
                                        } else {
                                            viewModel.onScientificFunction("log")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_log"
                                )
                            }

                            // Row 2: xʸ, √/x², ∛/x³, 1/x, |x|
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SciButton(
                                    text = "xʸ",
                                    onClick = { viewModel.onPower() },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_power"
                                )
                                SciButton(
                                    text = if (isInvMode) "x²" else "√",
                                    onClick = {
                                        if (isInvMode) viewModel.onSquare()
                                        else viewModel.onScientificFunction("√")
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_sqrt"
                                )
                                SciButton(
                                    text = if (isInvMode) "x³" else "∛",
                                    onClick = {
                                        if (isInvMode) viewModel.onCube()
                                        else viewModel.onScientificFunction("∛")
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_cbrt"
                                )
                                SciButton(
                                    text = "1/x",
                                    onClick = { viewModel.onReciprocal() },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_reciprocal"
                                )
                                SciButton(
                                    text = "|x|",
                                    onClick = { viewModel.onScientificFunction("abs") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_abs"
                                )
                            }

                            // Row 3: π, e, x!, (, )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SciButton(
                                    text = "π",
                                    onClick = { viewModel.onConstant("π") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_pi"
                                )
                                SciButton(
                                    text = "e",
                                    onClick = { viewModel.onConstant("e") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_e"
                                )
                                SciButton(
                                    text = "x!",
                                    onClick = { viewModel.onFactorial() },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_factorial"
                                )
                                SciButton(
                                    text = "(",
                                    onClick = { viewModel.onDigit("(") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_open_paren"
                                )
                                SciButton(
                                    text = ")",
                                    onClick = { viewModel.onDigit(")") },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_close_paren"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    @Composable
                    fun KeypadButton(
                        text: String,
                        onClick: () -> Unit,
                        type: GlassButtonType = GlassButtonType.NUMBER,
                        modifier: Modifier = Modifier,
                        testTag: String = "btn_$text"
                    ) {
                        GlassButton(
                            text = text,
                            onClick = onClick,
                            type = type,
                            theme = appTheme,
                            isHapticEnabled = hapticEnabled,
                            aspectRatio = if (isScientificMode) 1.25f else 1f,
                            shape = if (isScientificMode) RoundedCornerShape(18.dp) else RoundedCornerShape(22.dp),
                            fontSize = if (isScientificMode) {
                                when (type) {
                                    GlassButtonType.ACTION -> 19.sp
                                    GlassButtonType.OPERATOR, GlassButtonType.EQUALS -> 24.sp
                                    GlassButtonType.NUMBER -> 22.sp
                                    GlassButtonType.SCIENTIFIC -> 14.sp
                                }
                            } else null,
                            modifier = modifier,
                            testTag = testTag
                        )
                    }

                    // Keypad Grid (5 rows x 4 columns)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculator_keypad"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: AC, ( ), %, ÷
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KeypadButton(
                                text = "AC",
                                onClick = { viewModel.onClear() },
                                type = GlassButtonType.ACTION,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_ac"
                            )
                            KeypadButton(
                                text = "( )",
                                onClick = { viewModel.onParenthesis() },
                                type = GlassButtonType.ACTION,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_parenthesis"
                            )
                            KeypadButton(
                                text = "%",
                                onClick = { viewModel.onPercentage() },
                                type = GlassButtonType.ACTION,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_percent"
                            )
                            KeypadButton(
                                text = "÷",
                                onClick = { viewModel.onOperator("/") },
                                type = GlassButtonType.OPERATOR,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_divide"
                            )
                        }

                        // Row 2: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KeypadButton(
                                text = "7",
                                onClick = { viewModel.onDigit("7") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_7"
                            )
                            KeypadButton(
                                text = "8",
                                onClick = { viewModel.onDigit("8") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_8"
                            )
                            KeypadButton(
                                text = "9",
                                onClick = { viewModel.onDigit("9") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_9"
                            )
                            KeypadButton(
                                text = "×",
                                onClick = { viewModel.onOperator("*") },
                                type = GlassButtonType.OPERATOR,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_multiply"
                            )
                        }

                        // Row 3: 4, 5, 6, −
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KeypadButton(
                                text = "4",
                                onClick = { viewModel.onDigit("4") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_4"
                            )
                            KeypadButton(
                                text = "5",
                                onClick = { viewModel.onDigit("5") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_5"
                            )
                            KeypadButton(
                                text = "6",
                                onClick = { viewModel.onDigit("6") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_6"
                            )
                            KeypadButton(
                                text = "−",
                                onClick = { viewModel.onOperator("-") },
                                type = GlassButtonType.OPERATOR,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_subtract"
                            )
                        }

                        // Row 4: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KeypadButton(
                                text = "1",
                                onClick = { viewModel.onDigit("1") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_1"
                            )
                            KeypadButton(
                                text = "2",
                                onClick = { viewModel.onDigit("2") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_2"
                            )
                            KeypadButton(
                                text = "3",
                                onClick = { viewModel.onDigit("3") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_3"
                            )
                            KeypadButton(
                                text = "+",
                                onClick = { viewModel.onOperator("+") },
                                type = GlassButtonType.OPERATOR,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_add"
                            )
                        }

                        // Row 5: ±, 0, ., =
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KeypadButton(
                                text = "±",
                                onClick = { viewModel.onToggleSign() },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_toggle_sign"
                            )
                            KeypadButton(
                                text = "0",
                                onClick = { viewModel.onDigit("0") },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_0"
                            )
                            KeypadButton(
                                text = ".",
                                onClick = { viewModel.onDecimal() },
                                type = GlassButtonType.NUMBER,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_decimal"
                            )
                            KeypadButton(
                                text = "=",
                                onClick = { viewModel.onCalculate() },
                                type = GlassButtonType.EQUALS,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_equals"
                            )
                        }
                    }
                }
            }
        }

        // History Bottom Sheet
        if (isHistoryOpen) {
            HistoryBottomSheet(
                historyList = historyList,
                sheetState = historySheetState,
                onDismiss = { viewModel.setHistoryOpen(false) },
                onSelectHistory = { item -> viewModel.onUseHistory(item) },
                onDeleteHistory = { item -> viewModel.onDeleteHistory(item) },
                onClearAll = { viewModel.onClearAllHistory() }
            )
        }

        // Background / Theme Settings Bottom Sheet
        if (isSettingsOpen) {
            BackgroundSettingsSheet(
                sheetState = settingsSheetState,
                isCustomBackground = customBgPath != null,
                blurRadius = blurRadius,
                overlayDarkness = overlayDarkness,
                currentTheme = appTheme,
                isHapticEnabled = hapticEnabled,
                onThemeChange = { theme -> viewModel.setAppTheme(theme) },
                onHapticToggle = { enabled -> viewModel.setHapticEnabled(enabled) },
                onDismiss = { viewModel.setSettingsOpen(false) },
                onPickImage = { uri -> viewModel.onSetCustomBackground(uri) },
                onResetToDefault = { viewModel.onResetBackground() },
                onBlurChange = { blur -> viewModel.onUpdateBlur(blur) },
                onDarknessChange = { darkness -> viewModel.onUpdateDarkness(darkness) }
            )
        }

        // Tools Catalog Modal Sheet
        if (isCatalogOpen) {
            ConverterCatalogSheet(
                sheetState = catalogSheetState,
                onDismiss = { viewModel.setCatalogOpen(false) },
                onSelectConverter = { type -> viewModel.selectConverter(type) }
            )
        }

        // Draggable In-App Floating Mini Calculator
        if (isFloatingMiniOpen) {
            FloatingMiniCalculator(
                isHapticEnabled = hapticEnabled,
                onClose = { viewModel.setFloatingMiniOpen(false) },
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}

// Compact Picture-in-Picture Mini View (for multitasking over other apps)
@Composable
private fun FloatingPipMiniView(
    expression: String,
    result: String?,
    isEvaluated: Boolean,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.1f))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isEvaluated && result != null) result else if (expression.isEmpty()) "0" else expression,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mini Grid
            val keys = listOf(
                listOf("C", "÷", "×", "⌫"),
                listOf("7", "8", "9", "−"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "="),
                listOf("0", ".", "", "")
            )

            Column(
                modifier = Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (key in row) {
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isOp = key in listOf("÷", "×", "−", "+", "=")
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isOp) Color(0xFF0284C7) else Color.White.copy(0.12f))
                                        .clickable {
                                            when (key) {
                                                "C" -> onClear()
                                                "⌫" -> onBackspace()
                                                "=" -> onEquals()
                                                "÷" -> onOperator("/")
                                                "×" -> onOperator("*")
                                                "−" -> onOperator("-")
                                                "+" -> onOperator("+")
                                                else -> onDigit(key)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        color = Color.White,
                                        fontSize = 12.sp,
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

