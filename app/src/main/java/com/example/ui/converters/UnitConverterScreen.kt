package com.example.ui.converters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.converters.engine.UnitConverters
import com.example.converters.engine.UnitDefinition
import com.example.converters.model.ConverterType
import com.example.ui.components.GlassSurface

@Composable
fun UnitConverterScreen(
    converterType: ConverterType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val unitList = remember(converterType) {
        UnitConverters.getUnitsForCategory(converterType.id)
    }

    var fromUnitId by remember(converterType) {
        mutableStateOf(unitList.firstOrNull()?.id ?: "")
    }
    var toUnitId by remember(converterType) {
        mutableStateOf(if (unitList.size > 1) unitList[1].id else (unitList.firstOrNull()?.id ?: ""))
    }
    var inputValueStr by remember(converterType) { mutableStateOf("1") }

    val fromUnit = unitList.find { it.id == fromUnitId } ?: unitList.firstOrNull()
    val toUnit = unitList.find { it.id == toUnitId } ?: unitList.getOrNull(1) ?: unitList.firstOrNull()

    val numericInput = inputValueStr.toDoubleOrNull() ?: 0.0

    val convertedValue by remember(numericInput, fromUnitId, toUnitId, converterType) {
        derivedStateOf {
            UnitConverters.convert(converterType.id, numericInput, fromUnitId, toUnitId)
        }
    }

    val formattedResult = remember(convertedValue) {
        UnitConverters.formatNumber(convertedValue)
    }

    var fromMenuExpanded by remember { mutableStateOf(false) }
    var toMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Input Glass Card
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            backgroundColor = Color.White.copy(alpha = 0.10f),
            borderColor = Color.White.copy(alpha = 0.20f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "From",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unit selector button
                    Box {
                        GlassSurface(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White.copy(alpha = 0.15f),
                            borderColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { fromMenuExpanded = true }
                                .testTag("btn_from_unit_dropdown")
                        ) {
                            Text(
                                text = "${fromUnit?.name ?: ""} (${fromUnit?.symbol ?: ""}) ▾",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = fromMenuExpanded,
                            onDismissRequest = { fromMenuExpanded = false }
                        ) {
                            unitList.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})") },
                                    onClick = {
                                        fromUnitId = unit.id
                                        fromMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Input Text field
                    OutlinedTextField(
                        value = inputValueStr,
                        onValueChange = { inputValueStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .width(140.dp)
                            .testTag("unit_input_field")
                    )
                }
            }
        }

        // Swap Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                shape = CircleShape,
                backgroundColor = Color(0xFF0284C7).copy(alpha = 0.4f),
                borderColor = Color(0xFF38BDF8).copy(alpha = 0.6f)
            ) {
                IconButton(
                    onClick = {
                        val temp = fromUnitId
                        fromUnitId = toUnitId
                        toUnitId = temp
                    },
                    modifier = Modifier.size(42.dp).testTag("btn_swap_units")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Units",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Output Result Glass Card
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            backgroundColor = Color.White.copy(alpha = 0.12f),
            borderColor = Color(0xFF38BDF8).copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "To",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Copy button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("$formattedResult ${toUnit?.symbol ?: ""}"))
                            Toast.makeText(context, "Copied $formattedResult", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("btn_copy_conversion")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Target Unit selector button
                    Box {
                        GlassSurface(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White.copy(alpha = 0.15f),
                            borderColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { toMenuExpanded = true }
                                .testTag("btn_to_unit_dropdown")
                        ) {
                            Text(
                                text = "${toUnit?.name ?: ""} (${toUnit?.symbol ?: ""}) ▾",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = toMenuExpanded,
                            onDismissRequest = { toMenuExpanded = false }
                        ) {
                            unitList.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})") },
                                    onClick = {
                                        toUnitId = unit.id
                                        toMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Formatted Result
                    Text(
                        text = formattedResult,
                        color = Color(0xFF38BDF8),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("converted_result_text")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // All Units Comparison at a glance
        Text(
            text = "All Units Equivalent",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(unitList, key = { it.id }) { item ->
                val equivVal = UnitConverters.convert(converterType.id, numericInput, fromUnitId, item.id)
                val equivFmt = UnitConverters.formatNumber(equivVal)

                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = if (item.id == toUnitId) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f),
                    borderColor = if (item.id == toUnitId) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.name} (${item.symbol})",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = equivFmt,
                            color = if (item.id == toUnitId) Color(0xFF38BDF8) else Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
