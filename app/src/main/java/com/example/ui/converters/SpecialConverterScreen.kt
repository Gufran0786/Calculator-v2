package com.example.ui.converters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.converters.engine.SpecialConverters
import com.example.converters.model.ConverterType
import com.example.ui.components.GlassSurface
import java.time.LocalDateTime

@Composable
fun SpecialConverterScreen(
    converterType: ConverterType,
    modifier: Modifier = Modifier
) {
    when (converterType) {
        ConverterType.NUMBER_BASE -> NumberBaseView(modifier)
        ConverterType.NUMBER_WORDS_IND -> NumberToWordsIndView(modifier)
        ConverterType.NUMBER_WORDS_INT -> NumberToWordsIntView(modifier)
        ConverterType.COLOUR -> ColourConverterView(modifier)
        ConverterType.AGE -> AgeCalculatorView(modifier)
        ConverterType.TILES -> TilesCalculatorView(modifier)
        ConverterType.GOLD_COMPOSITION -> GoldCompositionView(modifier)
        ConverterType.SHOE_SIZE -> ShoeSizeView(modifier)
        ConverterType.CLOTH_SIZE -> ClothSizeView(modifier)
        ConverterType.TIME_ZONE -> TimeZoneConverterView(modifier)
        ConverterType.TIME_ZONE_COMPARE -> TimeZoneCompareView(modifier)
        ConverterType.BMI -> BmiCalculatorView(modifier)
        else -> Text("Converter coming soon", color = Color.White)
    }
}

// 13. Number Base Converter (Decimal, Binary, Octal, Hex, Roman)
@Composable
private fun NumberBaseView(modifier: Modifier = Modifier) {
    var inputStr by remember { mutableStateOf("255") }
    var baseChoice by remember { mutableStateOf(10) }

    val result = remember(inputStr, baseChoice) {
        SpecialConverters.convertNumberBase(inputStr, baseChoice)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(10 to "DEC", 2 to "BIN", 8 to "OCT", 16 to "HEX", -1 to "ROMAN").forEach { (base, label) ->
                FilterChip(
                    selected = baseChoice == base,
                    onClick = { baseChoice = base },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }

        OutlinedTextField(
            value = inputStr,
            onValueChange = { inputStr = it },
            label = { Text("Enter Value", color = Color.White.copy(alpha = 0.7f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_number_base")
        )

        if (result != null) {
            listOf(
                "Decimal (Base 10)" to result.decimal,
                "Binary (Base 2)" to result.binary,
                "Octal (Base 8)" to result.octal,
                "Hexadecimal (Base 16)" to result.hex,
                "Roman Numerals" to result.roman
            ).forEach { (label, value) ->
                SpecialResultCard(label = label, value = value)
            }
        } else {
            Text("Invalid input for chosen base", color = Color(0xFFF87171), fontSize = 14.sp)
        }
    }
}

// 24. Number to Words (IND)
@Composable
private fun NumberToWordsIndView(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("125000") }
    val words = remember(input) { SpecialConverters.numberToWordsIndian(input) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Amount in Numbers (e.g. 1,25,000)", color = Color.White.copy(alpha = 0.7f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_words_ind")
        )

        SpecialResultCard(label = "Indian Numbering Format (Lakhs / Crores)", value = words)
    }
}

// 27. Number to Words (INT)
@Composable
private fun NumberToWordsIntView(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("1250000") }
    val words = remember(input) { SpecialConverters.numberToWordsInternational(input) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Amount in Numbers (e.g. 1,250,000)", color = Color.White.copy(alpha = 0.7f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_words_int")
        )

        SpecialResultCard(label = "International Format (Millions / Billions)", value = words)
    }
}

// 21. Colour Converter
@Composable
private fun ColourConverterView(modifier: Modifier = Modifier) {
    var red by remember { mutableStateOf(56f) }
    var green by remember { mutableStateOf(189f) }
    var blue by remember { mutableStateOf(248f) }

    val colorRes = remember(red, green, blue) {
        SpecialConverters.convertFromRgb(red.toInt(), green.toInt(), blue.toInt())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Color Swatch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(colorRes.r, colorRes.g, colorRes.b))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .testTag("color_preview_box")
        )

        // RGB Sliders
        Text("Red: ${red.toInt()}", color = Color.White, fontSize = 13.sp)
        Slider(
            value = red,
            onValueChange = { red = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
        )

        Text("Green: ${green.toInt()}", color = Color.White, fontSize = 13.sp)
        Slider(
            value = green,
            onValueChange = { green = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF22C55E), activeTrackColor = Color(0xFF22C55E))
        )

        Text("Blue: ${blue.toInt()}", color = Color.White, fontSize = 13.sp)
        Slider(
            value = blue,
            onValueChange = { blue = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
        )

        SpecialResultCard(label = "HEX Code", value = colorRes.hex)
        SpecialResultCard(label = "RGB", value = "rgb(${colorRes.r}, ${colorRes.g}, ${colorRes.b})")
        SpecialResultCard(label = "HSL", value = "hsl(${colorRes.h.toInt()}°, ${colorRes.s.toInt()}%, ${colorRes.l.toInt()}%)")
        SpecialResultCard(label = "HSV", value = "hsv(${colorRes.hsvH.toInt()}°, ${colorRes.hsvS.toInt()}%, ${colorRes.hsvV.toInt()}%)")
        SpecialResultCard(label = "CMYK", value = "cmyk(${colorRes.c.toInt()}%, ${colorRes.m.toInt()}%, ${colorRes.y.toInt()}%, ${colorRes.k.toInt()}%)")
    }
}

// 46. Age Calculator
@Composable
private fun AgeCalculatorView(modifier: Modifier = Modifier) {
    var birthYear by remember { mutableStateOf("1998") }
    var birthMonth by remember { mutableStateOf("5") }
    var birthDay by remember { mutableStateOf("15") }

    val ageRes = remember(birthYear, birthMonth, birthDay) {
        val y = birthYear.toIntOrNull() ?: 2000
        val m = birthMonth.toIntOrNull() ?: 1
        val d = birthDay.toIntOrNull() ?: 1
        SpecialConverters.calculateAge(y, m, d)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Enter Date of Birth", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = birthDay,
                onValueChange = { birthDay = it },
                label = { Text("Day (1-31)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = birthMonth,
                onValueChange = { birthMonth = it },
                label = { Text("Month (1-12)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = birthYear,
                onValueChange = { birthYear = it },
                label = { Text("Year (YYYY)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1.3f)
            )
        }

        if (ageRes != null) {
            SpecialResultCard(label = "Exact Age", value = "${ageRes.years} Years, ${ageRes.months} Months, ${ageRes.days} Days")
            SpecialResultCard(label = "Days Until Next Birthday", value = "${ageRes.daysUntilNextBirthday} Days")
            SpecialResultCard(label = "Total Days Lived", value = "${ageRes.totalDays} Days (${ageRes.totalWeeks} Weeks)")
            SpecialResultCard(label = "Day of the Week Born", value = ageRes.dayOfWeekBorn)
        } else {
            Text("Please enter a valid past birth date", color = Color(0xFFF87171))
        }
    }
}

// 44. Tiles Calculator
@Composable
private fun TilesCalculatorView(modifier: Modifier = Modifier) {
    var roomLen by remember { mutableStateOf("14") }
    var roomWid by remember { mutableStateOf("12") }
    var tileLen by remember { mutableStateOf("24") } // inches (2ft)
    var tileWid by remember { mutableStateOf("24") } // inches (2ft)
    var perBox by remember { mutableStateOf("4") }
    var wastage by remember { mutableStateOf("10") }

    val result = remember(roomLen, roomWid, tileLen, tileWid, perBox, wastage) {
        val rl = roomLen.toDoubleOrNull() ?: 0.0
        val rw = roomWid.toDoubleOrNull() ?: 0.0
        val tl = tileLen.toDoubleOrNull() ?: 24.0
        val tw = tileWid.toDoubleOrNull() ?: 24.0
        val pb = perBox.toIntOrNull() ?: 4
        val ws = wastage.toDoubleOrNull() ?: 10.0
        SpecialConverters.calculateTiles(rl, rw, tl, tw, pb, ws)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = roomLen, onValueChange = { roomLen = it },
                label = { Text("Room Length (ft)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = roomWid, onValueChange = { roomWid = it },
                label = { Text("Room Width (ft)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = tileLen, onValueChange = { tileLen = it },
                label = { Text("Tile Length (in)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = tileWid, onValueChange = { tileWid = it },
                label = { Text("Tile Width (in)", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = perBox, onValueChange = { perBox = it },
                label = { Text("Tiles/Box", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = wastage, onValueChange = { wastage = it },
                label = { Text("Wastage %", color = Color.White.copy(0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )
        }

        SpecialResultCard(label = "Total Boxes Needed", value = "${result.totalBoxesNeeded} Boxes")
        SpecialResultCard(label = "Total Tiles Needed (with buffer)", value = "${result.tilesWithWastage} Tiles (${result.wastageCount} extra)")
        SpecialResultCard(label = "Room Area", value = "${String.format("%.1f", result.roomAreaSqFt)} sq ft (${String.format("%.2f", result.roomAreaSqM)} m²)")
    }
}

// 45. Gold Composition
@Composable
private fun GoldCompositionView(modifier: Modifier = Modifier) {
    var weightStr by remember { mutableStateOf("15.5") }
    var selectedKarat by remember { mutableStateOf(22) }
    var rate24kStr by remember { mutableStateOf("7200") }

    val result = remember(weightStr, selectedKarat, rate24kStr) {
        val w = weightStr.toDoubleOrNull() ?: 0.0
        val r = rate24kStr.toDoubleOrNull() ?: 0.0
        SpecialConverters.calculateGold(w, selectedKarat, r)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Select Gold Karat", color = Color.White, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(24, 22, 18, 14, 10).forEach { k ->
                FilterChip(
                    selected = selectedKarat == k,
                    onClick = { selectedKarat = k },
                    label = { Text("${k}K") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFBBF24),
                        selectedLabelColor = Color.Black,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White
                    )
                )
            }
        }

        OutlinedTextField(
            value = weightStr,
            onValueChange = { weightStr = it },
            label = { Text("Total Gold Weight (grams)", color = Color.White.copy(0.7f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rate24kStr,
            onValueChange = { rate24kStr = it },
            label = { Text("24K Pure Gold Rate / gram (₹)", color = Color.White.copy(0.7f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        SpecialResultCard(label = "Pure Gold Content", value = "${String.format("%.3f", result.pureGoldGrams)} g (${String.format("%.1f", result.purityPercent)}% pure)")
        SpecialResultCard(label = "Alloy / Other Metal Weight", value = "${String.format("%.3f", result.alloyGrams)} g")
        SpecialResultCard(label = "Estimated Market Value", value = "₹ ${String.format("%,.2f", result.estimatedValue)}")
    }
}

// 28. Shoe Size View
@Composable
private fun ShoeSizeView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Men's International Shoe Size Chart", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SpecialConverters.menShoeSizes.forEach { item ->
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("US: ${item.us}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Text("UK: ${item.uk}", color = Color.White)
                    Text("EU: ${item.eu}", color = Color.White)
                    Text("${item.cm} cm", color = Color.White.copy(alpha = 0.8f))
                    Text("${item.inches}\"", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// 30. Cloth Size View
@Composable
private fun ClothSizeView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Men's Tops International Size Guide", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SpecialConverters.menTopsSizes.forEach { item ->
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.size, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("US: ${item.us}", color = Color.White)
                    Text("EU: ${item.eu}", color = Color.White)
                    Text("Chest: ${item.chestIn}\"", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// 17. Time Zone Converter View
@Composable
private fun TimeZoneConverterView(modifier: Modifier = Modifier) {
    var fromZone by remember { mutableStateOf("Asia/Kolkata") }
    var toZone by remember { mutableStateOf("America/New_York") }

    val converted = remember(fromZone, toZone) {
        SpecialConverters.convertTimeBetweenZones(LocalDateTime.now(), fromZone, toZone)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Current Time Zone Converter", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SpecialResultCard(label = "From (IST)", value = "New Delhi, India (Asia/Kolkata)")
        SpecialResultCard(label = "To (EST)", value = "New York, USA (America/New_York)")
        SpecialResultCard(label = "Converted Time", value = converted)
    }
}

// 18. Time Zone Compare View
@Composable
private fun TimeZoneCompareView(modifier: Modifier = Modifier) {
    val worldTimes = remember { SpecialConverters.getWorldTimes() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("World Clocks Comparison", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        worldTimes.forEach { item ->
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.city, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(item.country, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(item.offsetString, color = Color(0xFF38BDF8), fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(item.timeString, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(item.dateString, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SpecialResultCard(label: String, value: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color.White.copy(alpha = 0.10f),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, color = Color(0xFF38BDF8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// 47. BMI (Body Mass Index) Calculator
@Composable
private fun BmiCalculatorView(modifier: Modifier = Modifier) {
    var isMetric by remember { mutableStateOf(true) }
    var heightCmStr by remember { mutableStateOf("172") }
    var weightKgStr by remember { mutableStateOf("68") }
    var heightFeetStr by remember { mutableStateOf("5") }
    var heightInchesStr by remember { mutableStateOf("8") }
    var weightLbsStr by remember { mutableStateOf("150") }

    val effectiveHeightCm = remember(isMetric, heightCmStr, heightFeetStr, heightInchesStr) {
        if (isMetric) {
            heightCmStr.toDoubleOrNull() ?: 0.0
        } else {
            val ft = heightFeetStr.toDoubleOrNull() ?: 0.0
            val inch = heightInchesStr.toDoubleOrNull() ?: 0.0
            (ft * 12.0 + inch) * 2.54
        }
    }

    val effectiveWeightKg = remember(isMetric, weightKgStr, weightLbsStr) {
        if (isMetric) {
            weightKgStr.toDoubleOrNull() ?: 0.0
        } else {
            val lbs = weightLbsStr.toDoubleOrNull() ?: 0.0
            lbs * 0.45359237
        }
    }

    val bmiResult = remember(effectiveHeightCm, effectiveWeightKg) {
        SpecialConverters.calculateBmi(effectiveHeightCm, effectiveWeightKg)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unit System Chip Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = isMetric,
                onClick = { isMetric = true },
                label = { Text("Metric (cm, kg)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2563EB),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.08f),
                    labelColor = Color.White.copy(alpha = 0.8f)
                )
            )
            FilterChip(
                selected = !isMetric,
                onClick = { isMetric = false },
                label = { Text("Imperial (ft/in, lbs)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2563EB),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.08f),
                    labelColor = Color.White.copy(alpha = 0.8f)
                )
            )
        }

        // Inputs Section
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White.copy(alpha = 0.08f),
            borderColor = Color.White.copy(alpha = 0.18f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Body Dimensions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                if (isMetric) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = heightCmStr,
                            onValueChange = { heightCmStr = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightKgStr,
                            onValueChange = { weightKgStr = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = heightFeetStr,
                            onValueChange = { heightFeetStr = it },
                            label = { Text("Feet (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightInchesStr,
                            onValueChange = { heightInchesStr = it },
                            label = { Text("Inches (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightLbsStr,
                            onValueChange = { weightLbsStr = it },
                            label = { Text("Weight (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // BMI Result Card
        if (bmiResult != null) {
            val badgeColor = Color(bmiResult.categoryColorHex)
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color.White.copy(alpha = 0.12f),
                borderColor = badgeColor.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("YOUR BMI SCORE", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

                    Text(
                        text = bmiResult.bmi.toString(),
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.25f))
                            .border(1.dp, badgeColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = bmiResult.category,
                            color = badgeColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Visual BMI Scale
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.weight(18.5f).fillMaxSize().background(Color(0xFFF59E0B)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.weight(6.4f).fillMaxSize().background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.weight(5f).fillMaxSize().background(Color(0xFFF59E0B)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.weight(10f).fillMaxSize().background(Color(0xFFEF4444)))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("< 18.5", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("18.5 - 24.9", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("25 - 29.9", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("30+", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                    }

                    Text(
                        text = bmiResult.advice,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Key Metrics Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.08f),
                    borderColor = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Healthy Weight", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${bmiResult.minHealthyWeightKg} - ${bmiResult.maxHealthyWeightKg} kg",
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.08f),
                    borderColor = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("BMI Prime", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            bmiResult.prime.toString(),
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.08f),
                    borderColor = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Ponderal Index", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${bmiResult.ponderalIndex} kg/m³",
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
