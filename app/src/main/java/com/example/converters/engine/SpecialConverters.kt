package com.example.converters.engine

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

object SpecialConverters {

    // --- 13. Number Base / Roman Converter ---
    data class NumberBaseResult(
        val decimal: String,
        val binary: String,
        val octal: String,
        val hex: String,
        val roman: String
    )

    fun convertNumberBase(input: String, fromBase: Int): NumberBaseResult? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val decimalVal: Long = try {
            when (fromBase) {
                10 -> trimmed.toLong()
                2 -> trimmed.toLong(2)
                8 -> trimmed.toLong(8)
                16 -> trimmed.toLong(16)
                -1 -> romanToDecimal(trimmed)
                else -> trimmed.toLong()
            }
        } catch (e: Exception) {
            return null
        }

        return NumberBaseResult(
            decimal = decimalVal.toString(),
            binary = decimalVal.toString(2),
            octal = decimalVal.toString(8),
            hex = decimalVal.toString(16).uppercase(),
            roman = decimalToRoman(decimalVal)
        )
    }

    private fun decimalToRoman(num: Long): String {
        if (num <= 0 || num > 3999) return "N/A (1..3999)"
        var n = num.toInt()
        val vals = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val romans = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val sb = StringBuilder()
        for (i in vals.indices) {
            while (n >= vals[i]) {
                n -= vals[i]
                sb.append(romans[i])
            }
        }
        return sb.toString()
    }

    private fun romanToDecimal(roman: String): Long {
        val map = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        val r = roman.uppercase()
        var total = 0
        var prev = 0
        for (i in r.length - 1 downTo 0) {
            val curr = map[r[i]] ?: throw IllegalArgumentException("Invalid Roman character")
            if (curr < prev) total -= curr else total += curr
            prev = curr
        }
        return total.toLong()
    }

    // --- 24. Number to Words (IND) ---
    fun numberToWordsIndian(numberStr: String): String {
        val clean = numberStr.replace(",", "").trim()
        if (clean.isEmpty()) return ""
        val parts = clean.split(".")
        val intPart = parts[0].toLongOrNull() ?: return "Number too large"
        if (intPart == 0L && (parts.size == 1 || parts[1] == "00" || parts[1] == "0")) return "Zero Rupees Only"

        val sb = StringBuilder()
        if (intPart < 0) {
            sb.append("Minus ")
        }
        val absInt = kotlin.math.abs(intPart)

        val words = convertIndianNumberToWords(absInt)
        sb.append(words)
        sb.append(" Rupees")

        if (parts.size > 1 && parts[1].isNotEmpty()) {
            val paisaStr = parts[1].take(2).padEnd(2, '0')
            val paisa = paisaStr.toIntOrNull() ?: 0
            if (paisa > 0) {
                sb.append(" and ").append(convertIndianNumberToWords(paisa.toLong())).append(" Paise")
            }
        }
        sb.append(" Only")
        return sb.toString().trim()
    }

    private fun convertIndianNumberToWords(n: Long): String {
        if (n == 0L) return "Zero"
        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun twoDigits(num: Int): String {
            return when {
                num == 0 -> ""
                num < 20 -> units[num]
                else -> {
                    val t = tens[num / 10]
                    val u = units[num % 10]
                    if (u.isEmpty()) t else "$t $u"
                }
            }
        }

        fun threeDigits(num: Int): String {
            val h = num / 100
            val rest = num % 100
            val hStr = if (h > 0) "${units[h]} Hundred" else ""
            val restStr = twoDigits(rest)
            return when {
                hStr.isNotEmpty() && restStr.isNotEmpty() -> "$hStr and $restStr"
                hStr.isNotEmpty() -> hStr
                else -> restStr
            }
        }

        var num = n
        val crore = num / 10000000
        num %= 10000000
        val lakh = num / 100000
        num %= 100000
        val thousand = num / 1000
        num %= 1000
        val remainder = num.toInt()

        val list = mutableListOf<String>()
        if (crore > 0) list.add("${convertIndianNumberToWords(crore)} Crore")
        if (lakh > 0) list.add("${twoDigits(lakh.toInt())} Lakh")
        if (thousand > 0) list.add("${twoDigits(thousand.toInt())} Thousand")
        if (remainder > 0) list.add(threeDigits(remainder))

        return list.joinToString(" ")
    }

    // --- 27. Number to Words (INT) ---
    fun numberToWordsInternational(numberStr: String): String {
        val clean = numberStr.replace(",", "").trim()
        if (clean.isEmpty()) return ""
        val parts = clean.split(".")
        val intPart = parts[0].toLongOrNull() ?: return "Number too large"
        if (intPart == 0L && (parts.size == 1 || parts[1] == "00" || parts[1] == "0")) return "Zero"

        val sb = StringBuilder()
        if (intPart < 0) sb.append("Minus ")
        val absInt = kotlin.math.abs(intPart)

        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
        val scales = arrayOf("", "Thousand", "Million", "Billion", "Trillion")

        fun chunkToWords(n: Int): String {
            val h = n / 100
            val rest = n % 100
            val partsList = mutableListOf<String>()
            if (h > 0) partsList.add("${units[h]} Hundred")
            if (rest in 1..19) {
                partsList.add(units[rest])
            } else if (rest >= 20) {
                val t = tens[rest / 10]
                val u = units[rest % 10]
                partsList.add(if (u.isNotEmpty()) "$t-$u" else t)
            }
            return partsList.joinToString(" ")
        }

        var temp = absInt
        var scaleIndex = 0
        val chunks = mutableListOf<String>()
        while (temp > 0) {
            val chunk = (temp % 1000).toInt()
            if (chunk != 0) {
                val words = chunkToWords(chunk)
                val scale = scales[scaleIndex]
                chunks.add(0, if (scale.isNotEmpty()) "$words $scale" else words)
            }
            temp /= 1000
            scaleIndex++
        }

        sb.append(chunks.joinToString(" "))
        if (parts.size > 1 && parts[1].isNotEmpty()) {
            val decimals = parts[1].take(2).padEnd(2, '0')
            val decNum = decimals.toIntOrNull() ?: 0
            if (decNum > 0) {
                sb.append(" point ").append(chunkToWords(decNum))
            }
        }
        return sb.toString().trim()
    }

    // --- 21. Colour Converter ---
    data class ColorResult(
        val hex: String,
        val r: Int, val g: Int, val b: Int,
        val h: Float, val s: Float, val l: Float,
        val hsvH: Float, val hsvS: Float, val hsvV: Float,
        val c: Float, val m: Float, val y: Float, val k: Float
    )

    fun convertFromRgb(r: Int, g: Int, b: Int): ColorResult {
        val cr = r.coerceIn(0, 255)
        val cg = g.coerceIn(0, 255)
        val cb = b.coerceIn(0, 255)

        val hex = String.format(Locale.US, "#%02X%02X%02X", cr, cg, cb)

        val rf = cr / 255f
        val gf = cg / 255f
        val bf = cb / 255f

        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min

        // HSL
        val l = (max + min) / 2f
        val s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * l - 1f))
        var h = when {
            delta == 0f -> 0f
            max == rf -> ((gf - bf) / delta) % 6f
            max == gf -> ((bf - rf) / delta) + 2f
            else -> ((rf - gf) / delta) + 4f
        } * 60f
        if (h < 0f) h += 360f

        // HSV
        val v = max
        val hsvS = if (max == 0f) 0f else delta / max

        // CMYK
        val k = 1f - max
        val c = if (k == 1f) 0f else (1f - rf - k) / (1f - k)
        val m = if (k == 1f) 0f else (1f - gf - k) / (1f - k)
        val y = if (k == 1f) 0f else (1f - bf - k) / (1f - k)

        return ColorResult(
            hex = hex,
            r = cr, g = cg, b = cb,
            h = h, s = s * 100f, l = l * 100f,
            hsvH = h, hsvS = hsvS * 100f, hsvV = v * 100f,
            c = c * 100f, m = m * 100f, y = y * 100f, k = k * 100f
        )
    }

    fun hexToRgb(hexStr: String): Triple<Int, Int, Int>? {
        val clean = hexStr.removePrefix("#").trim()
        if (clean.length == 6) {
            val r = clean.substring(0, 2).toIntOrNull(16) ?: return null
            val g = clean.substring(2, 4).toIntOrNull(16) ?: return null
            val b = clean.substring(4, 6).toIntOrNull(16) ?: return null
            return Triple(r, g, b)
        } else if (clean.length == 3) {
            val r = ("" + clean[0] + clean[0]).toIntOrNull(16) ?: return null
            val g = ("" + clean[1] + clean[1]).toIntOrNull(16) ?: return null
            val b = ("" + clean[2] + clean[2]).toIntOrNull(16) ?: return null
            return Triple(r, g, b)
        }
        return null
    }

    // --- 28. Shoe Size Converter ---
    data class ShoeSizeItem(val us: Double, val uk: Double, val eu: Double, val cm: Double, val inches: Double)

    val menShoeSizes = listOf(
        ShoeSizeItem(7.0, 6.5, 40.0, 25.0, 9.8),
        ShoeSizeItem(7.5, 7.0, 40.5, 25.5, 10.0),
        ShoeSizeItem(8.0, 7.5, 41.0, 26.0, 10.2),
        ShoeSizeItem(8.5, 8.0, 42.0, 26.5, 10.4),
        ShoeSizeItem(9.0, 8.5, 42.5, 27.0, 10.6),
        ShoeSizeItem(9.5, 9.0, 43.0, 27.5, 10.8),
        ShoeSizeItem(10.0, 9.5, 44.0, 28.0, 11.0),
        ShoeSizeItem(10.5, 10.0, 44.5, 28.5, 11.2),
        ShoeSizeItem(11.0, 10.5, 45.0, 29.0, 11.4),
        ShoeSizeItem(11.5, 11.0, 45.5, 29.5, 11.6),
        ShoeSizeItem(12.0, 11.5, 46.0, 30.0, 11.8)
    )

    // --- 30. Cloth Size Chart ---
    data class ClothSizeItem(val size: String, val us: String, val uk: String, val eu: String, val chestIn: String, val waistIn: String)

    val menTopsSizes = listOf(
        ClothSizeItem("XS", "34", "34", "44", "34-36", "28-30"),
        ClothSizeItem("S", "36", "36", "46", "36-38", "30-32"),
        ClothSizeItem("M", "38", "38", "48", "38-40", "32-34"),
        ClothSizeItem("L", "40", "40", "50", "40-42", "34-36"),
        ClothSizeItem("XL", "42", "42", "52", "42-44", "36-38"),
        ClothSizeItem("XXL", "44", "44", "54", "44-46", "38-40")
    )

    // --- 44. Tiles Calculator ---
    data class TilesResult(
        val roomAreaSqFt: Double,
        val roomAreaSqM: Double,
        val singleTileAreaSqFt: Double,
        val exactTilesNeeded: Int,
        val tilesWithWastage: Int,
        val totalBoxesNeeded: Int,
        val wastageCount: Int
    )

    fun calculateTiles(
        roomLengthFt: Double,
        roomWidthFt: Double,
        tileLengthInches: Double,
        tileWidthInches: Double,
        tilesPerBox: Int,
        wastagePercent: Double
    ): TilesResult {
        val roomAreaSqFt = roomLengthFt * roomWidthFt
        val roomAreaSqM = roomAreaSqFt * 0.092903
        val singleTileSqFt = (tileLengthInches * tileWidthInches) / 144.0

        val exactTiles = if (singleTileSqFt > 0) kotlin.math.ceil(roomAreaSqFt / singleTileSqFt).toInt() else 0
        val wastageCount = kotlin.math.ceil(exactTiles * (wastagePercent / 100.0)).toInt()
        val totalTiles = exactTiles + wastageCount
        val boxes = if (tilesPerBox > 0) kotlin.math.ceil(totalTiles.toDouble() / tilesPerBox).toInt() else 0

        return TilesResult(
            roomAreaSqFt = roomAreaSqFt,
            roomAreaSqM = roomAreaSqM,
            singleTileAreaSqFt = singleTileSqFt,
            exactTilesNeeded = exactTiles,
            tilesWithWastage = totalTiles,
            totalBoxesNeeded = boxes,
            wastageCount = wastageCount
        )
    }

    // --- 45. Gold Composition Calculator ---
    data class GoldResult(
        val totalWeightGrams: Double,
        val karat: Int,
        val purityPercent: Double,
        val pureGoldGrams: Double,
        val alloyGrams: Double,
        val estimatedValue: Double
    )

    fun calculateGold(
        weightGrams: Double,
        karat: Int, // 24, 22, 18, 14, 10
        rate24KPerGram: Double
    ): GoldResult {
        val purity = (karat / 24.0) * 100.0
        val pureGrams = weightGrams * (karat / 24.0)
        val alloyGrams = weightGrams - pureGrams
        val estimatedVal = pureGrams * rate24KPerGram

        return GoldResult(
            totalWeightGrams = weightGrams,
            karat = karat,
            purityPercent = purity,
            pureGoldGrams = pureGrams,
            alloyGrams = alloyGrams,
            estimatedValue = estimatedVal
        )
    }

    // --- 46. Age Calculator ---
    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val totalWeeks: Long,
        val dayOfWeekBorn: String,
        val daysUntilNextBirthday: Long
    )

    fun calculateAge(birthYear: Int, birthMonth: Int, birthDay: Int): AgeResult? {
        return try {
            val birthDate = LocalDate.of(birthYear, birthMonth, birthDay)
            val today = LocalDate.now()
            if (birthDate.isAfter(today)) return null

            val period = Period.between(birthDate, today)
            val totalDays = ChronoUnit.DAYS.between(birthDate, today)
            val totalWeeks = totalDays / 7

            var nextBirthday = birthDate.withYear(today.year)
            if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                nextBirthday = nextBirthday.plusYears(1)
            }
            val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday)

            AgeResult(
                years = period.years,
                months = period.months,
                days = period.days,
                totalDays = totalDays,
                totalWeeks = totalWeeks,
                dayOfWeekBorn = birthDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                daysUntilNextBirthday = daysToNextBirthday
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- 17 & 18. Time Zone Comparison & Converter ---
    data class WorldCityTime(
        val city: String,
        val country: String,
        val zoneId: String,
        val timeString: String,
        val dateString: String,
        val offsetString: String
    )

    val majorWorldZones = listOf(
        "Asia/Kolkata" to ("New Delhi" to "India (IST)"),
        "UTC" to ("UTC" to "Universal Time"),
        "Europe/London" to ("London" to "United Kingdom (GMT)"),
        "America/New_York" to ("New York" to "United States (EST)"),
        "America/Los_Angeles" to ("Los Angeles" to "United States (PST)"),
        "Asia/Dubai" to ("Dubai" to "UAE (GST)"),
        "Asia/Singapore" to ("Singapore" to "Singapore (SGT)"),
        "Asia/Tokyo" to ("Tokyo" to "Japan (JST)"),
        "Australia/Sydney" to ("Sydney" to "Australia (AEST)"),
        "Europe/Paris" to ("Paris" to "France (CET)")
    )

    fun getWorldTimes(): List<WorldCityTime> {
        val now = ZonedDateTime.now()
        val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        val dateFmt = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)

        return majorWorldZones.map { (zoneId, namePair) ->
            val zone = ZoneId.of(zoneId)
            val zdt = now.withZoneSameInstant(zone)
            val offsetHours = zdt.offset.totalSeconds / 3600.0
            val sign = if (offsetHours >= 0) "+" else ""
            val offsetStr = "UTC $sign$offsetHours hrs"

            WorldCityTime(
                city = namePair.first,
                country = namePair.second,
                zoneId = zoneId,
                timeString = zdt.format(timeFmt),
                dateString = zdt.format(dateFmt),
                offsetString = offsetStr
            )
        }
    }

    fun convertTimeBetweenZones(
        dateTime: LocalDateTime,
        fromZoneId: String,
        toZoneId: String
    ): String {
        val fromZone = ZoneId.of(fromZoneId)
        val toZone = ZoneId.of(toZoneId)
        val sourceZdt = dateTime.atZone(fromZone)
        val targetZdt = sourceZdt.withZoneSameInstant(toZone)
        val fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy - hh:mm a (z)", Locale.US)
        return targetZdt.format(fmt)
    }

    // --- BMI Calculator ---
    data class BmiResult(
        val bmi: Double,
        val category: String,
        val categoryColorHex: Long,
        val minHealthyWeightKg: Double,
        val maxHealthyWeightKg: Double,
        val ponderalIndex: Double,
        val prime: Double,
        val advice: String
    )

    fun calculateBmi(heightCm: Double, weightKg: Double): BmiResult? {
        if (heightCm <= 20.0 || weightKg <= 2.0) return null
        val heightM = heightCm / 100.0
        val rawBmi = weightKg / (heightM * heightM)
        val ponderal = weightKg / (heightM * heightM * heightM)
        val prime = rawBmi / 25.0
        val minKg = 18.5 * (heightM * heightM)
        val maxKg = 24.9 * (heightM * heightM)

        val (category, color, advice) = when {
            rawBmi < 16.0 -> Triple("Severe Underweight", 0xFFEF4444, "Severe thinness. Consult a doctor or dietitian.")
            rawBmi < 18.5 -> Triple("Underweight", 0xFFF59E0B, "Below optimal range. Nutrient-rich diet recommended.")
            rawBmi < 25.0 -> Triple("Normal (Healthy)", 0xFF10B981, "Optimal healthy weight! Maintain active lifestyle.")
            rawBmi < 30.0 -> Triple("Overweight", 0xFFF59E0B, "Above ideal range. Daily exercise and diet recommended.")
            rawBmi < 35.0 -> Triple("Obese Class I", 0xFFEF4444, "Moderate health risk. Regular physical activity advised.")
            rawBmi < 40.0 -> Triple("Obese Class II", 0xFFDC2626, "High health risk. Structured fitness guidance advised.")
            else -> Triple("Obese Class III (Severe)", 0xFF991B1B, "Critical health risk. Professional medical guidance advised.")
        }

        return BmiResult(
            bmi = (rawBmi * 10.0).roundToInt() / 10.0,
            category = category,
            categoryColorHex = color,
            minHealthyWeightKg = (minKg * 10.0).roundToInt() / 10.0,
            maxHealthyWeightKg = (maxKg * 10.0).roundToInt() / 10.0,
            ponderalIndex = (ponderal * 10.0).roundToInt() / 10.0,
            prime = (prime * 100.0).roundToInt() / 100.0,
            advice = advice
        )
    }
}
