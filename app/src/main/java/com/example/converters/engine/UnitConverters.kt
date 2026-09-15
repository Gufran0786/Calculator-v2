package com.example.converters.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class UnitDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val factorToBase: Double // baseValue = input * factorToBase (for linear units)
)

object UnitConverters {

    // 1. Length (Base: Meter)
    val lengthUnits = listOf(
        UnitDefinition("m", "Meter", "m", 1.0),
        UnitDefinition("km", "Kilometer", "km", 1000.0),
        UnitDefinition("cm", "Centimeter", "cm", 0.01),
        UnitDefinition("mm", "Millimeter", "mm", 0.001),
        UnitDefinition("um", "Micrometer", "µm", 1e-6),
        UnitDefinition("nm", "Nanometer", "nm", 1e-9),
        UnitDefinition("mi", "Mile", "mi", 1609.344),
        UnitDefinition("yd", "Yard", "yd", 0.9144),
        UnitDefinition("ft", "Foot", "ft", 0.3048),
        UnitDefinition("in", "Inch", "in", 0.0254),
        UnitDefinition("nmi", "Nautical Mile", "NM", 1852.0)
    )

    // 2. Area (Base: Square Meter)
    val areaUnits = listOf(
        UnitDefinition("sq_m", "Square Meter", "m²", 1.0),
        UnitDefinition("sq_km", "Square Kilometer", "km²", 1e6),
        UnitDefinition("sq_cm", "Square Centimeter", "cm²", 1e-4),
        UnitDefinition("sq_mm", "Square Millimeter", "mm²", 1e-6),
        UnitDefinition("ha", "Hectare", "ha", 10000.0),
        UnitDefinition("ac", "Acre", "ac", 4046.8564224),
        UnitDefinition("sq_ft", "Square Foot", "ft²", 0.09290304),
        UnitDefinition("sq_yd", "Square Yard", "yd²", 0.83612736),
        UnitDefinition("sq_in", "Square Inch", "in²", 0.00064516)
    )

    // 3. Density (Base: kg/m³)
    val densityUnits = listOf(
        UnitDefinition("kg_m3", "Kilogram/m³", "kg/m³", 1.0),
        UnitDefinition("g_cm3", "Gram/cm³", "g/cm³", 1000.0),
        UnitDefinition("kg_l", "Kilogram/Liter", "kg/L", 1000.0),
        UnitDefinition("g_ml", "Gram/mL", "g/mL", 1000.0),
        UnitDefinition("lb_ft3", "Pound/ft³", "lb/ft³", 16.018463),
        UnitDefinition("lb_in3", "Pound/in³", "lb/in³", 27679.904)
    )

    // 4. Force (Base: Newton)
    val forceUnits = listOf(
        UnitDefinition("n", "Newton", "N", 1.0),
        UnitDefinition("kn", "Kilonewton", "kN", 1000.0),
        UnitDefinition("dyn", "Dyne", "dyn", 1e-5),
        UnitDefinition("lbf", "Pound-force", "lbf", 4.4482216),
        UnitDefinition("kgf", "Kilogram-force", "kgf", 9.80665)
    )

    // 5. Weight / Mass (Base: Kilogram)
    val weightUnits = listOf(
        UnitDefinition("kg", "Kilogram", "kg", 1.0),
        UnitDefinition("g", "Gram", "g", 0.001),
        UnitDefinition("mg", "Milligram", "mg", 1e-6),
        UnitDefinition("t", "Metric Ton", "t", 1000.0),
        UnitDefinition("lb", "Pound", "lb", 0.45359237),
        UnitDefinition("oz", "Ounce", "oz", 0.028349523125),
        UnitDefinition("ct", "Carat", "ct", 0.0002),
        UnitDefinition("st", "Stone", "st", 6.35029318)
    )

    // 6. Temperature
    val tempUnits = listOf(
        UnitDefinition("c", "Celsius", "°C", 1.0),
        UnitDefinition("f", "Fahrenheit", "°F", 1.0),
        UnitDefinition("k", "Kelvin", "K", 1.0),
        UnitDefinition("r", "Rankine", "°R", 1.0)
    )

    // 7. Pressure (Base: Pascal)
    val pressureUnits = listOf(
        UnitDefinition("pa", "Pascal", "Pa", 1.0),
        UnitDefinition("kpa", "Kilopascal", "kPa", 1000.0),
        UnitDefinition("bar", "Bar", "bar", 100000.0),
        UnitDefinition("psi", "Pound/sq inch", "psi", 6894.757293),
        UnitDefinition("atm", "Atmosphere", "atm", 101325.0),
        UnitDefinition("torr", "Torr / mmHg", "mmHg", 133.3223684)
    )

    // 8. Angle (Base: Degree)
    val angleUnits = listOf(
        UnitDefinition("deg", "Degree", "°", 1.0),
        UnitDefinition("rad", "Radian", "rad", 57.295779513),
        UnitDefinition("grad", "Gradian", "grad", 0.9),
        UnitDefinition("arcmin", "Minute of Arc", "arcmin", 1.0 / 60.0),
        UnitDefinition("arcsec", "Second of Arc", "arcsec", 1.0 / 3600.0)
    )

    // 9. Volume (Base: Liter)
    val volumeUnits = listOf(
        UnitDefinition("l", "Liter", "L", 1.0),
        UnitDefinition("ml", "Milliliter", "mL", 0.001),
        UnitDefinition("m3", "Cubic Meter", "m³", 1000.0),
        UnitDefinition("cm3", "Cubic Centimeter", "cm³", 0.001),
        UnitDefinition("gal", "Gallon (US)", "gal", 3.785411784),
        UnitDefinition("qt", "Quart (US)", "qt", 0.946352946),
        UnitDefinition("pt", "Pint (US)", "pt", 0.473176473),
        UnitDefinition("cup", "Cup (US)", "cup", 0.2365882365),
        UnitDefinition("floz", "Fluid Ounce (US)", "fl oz", 0.0295735295625),
        UnitDefinition("tbsp", "Tablespoon", "tbsp", 0.01478676478125),
        UnitDefinition("tsp", "Teaspoon", "tsp", 0.00492892159375)
    )

    // 10. Speed (Base: m/s)
    val speedUnits = listOf(
        UnitDefinition("mps", "Meter/second", "m/s", 1.0),
        UnitDefinition("kmh", "Kilometer/hour", "km/h", 1.0 / 3.6),
        UnitDefinition("mph", "Miles/hour", "mph", 0.44704),
        UnitDefinition("knot", "Knot", "kn", 0.514444444),
        UnitDefinition("fps", "Feet/second", "ft/s", 0.3048)
    )

    // 11. Energy (Base: Joule)
    val energyUnits = listOf(
        UnitDefinition("j", "Joule", "J", 1.0),
        UnitDefinition("kj", "Kilojoule", "kJ", 1000.0),
        UnitDefinition("cal", "Calorie", "cal", 4.184),
        UnitDefinition("kcal", "Kilocalorie", "kcal", 4184.0),
        UnitDefinition("wh", "Watt-hour", "Wh", 3600.0),
        UnitDefinition("kwh", "Kilowatt-hour", "kWh", 3.6e6),
        UnitDefinition("ev", "Electronvolt", "eV", 1.602176634e-19),
        UnitDefinition("btu", "British Thermal Unit", "BTU", 1055.05585)
    )

    // 12. Acceleration (Base: m/s²)
    val accelUnits = listOf(
        UnitDefinition("mps2", "Meter/s²", "m/s²", 1.0),
        UnitDefinition("kmh2", "Kilometer/h²", "km/h²", 1.0 / 12960.0),
        UnitDefinition("fps2", "Feet/s²", "ft/s²", 0.3048),
        UnitDefinition("g", "Standard Gravity", "g", 9.80665)
    )

    // 14. Power (Base: Watt)
    val powerUnits = listOf(
        UnitDefinition("w", "Watt", "W", 1.0),
        UnitDefinition("kw", "Kilowatt", "kW", 1000.0),
        UnitDefinition("mw", "Megawatt", "MW", 1e6),
        UnitDefinition("hp_m", "Metric Horsepower", "hp(M)", 735.49875),
        UnitDefinition("hp_i", "Mechanical Horsepower", "hp(I)", 745.69987),
        UnitDefinition("btuh", "BTU/hour", "BTU/h", 0.293071)
    )

    // 15. Angular Acceleration (Base: rad/s²)
    val angularAccelUnits = listOf(
        UnitDefinition("rads2", "Radian/s²", "rad/s²", 1.0),
        UnitDefinition("degs2", "Degree/s²", "°/s²", Math.PI / 180.0),
        UnitDefinition("revs2", "Revolution/s²", "rev/s²", 2.0 * Math.PI),
        UnitDefinition("revm2", "Revolution/min²", "rev/min²", (2.0 * Math.PI) / 3600.0)
    )

    // 16. Frequency (Base: Hertz)
    val frequencyUnits = listOf(
        UnitDefinition("hz", "Hertz", "Hz", 1.0),
        UnitDefinition("khz", "Kilohertz", "kHz", 1000.0),
        UnitDefinition("mhz", "Megahertz", "MHz", 1e6),
        UnitDefinition("ghz", "Gigahertz", "GHz", 1e9),
        UnitDefinition("rpm", "Revolutions/min", "RPM", 1.0 / 60.0)
    )

    // 19. Torque (Base: N·m)
    val torqueUnits = listOf(
        UnitDefinition("nm", "Newton-meter", "N·m", 1.0),
        UnitDefinition("lbft", "Pound-foot", "lbf·ft", 1.35581794833),
        UnitDefinition("lbin", "Pound-inch", "lbf·in", 0.112984829),
        UnitDefinition("kgfm", "Kilogram-meter", "kgf·m", 9.80665),
        UnitDefinition("dyncm", "Dyne-centimeter", "dyn·cm", 1e-7)
    )

    // 20. Light Luminance (Base: cd/m² / nit)
    val luminanceUnits = listOf(
        UnitDefinition("cdm2", "Candela/m²", "cd/m²", 1.0),
        UnitDefinition("nit", "Nit", "nit", 1.0),
        UnitDefinition("fl", "Foot-lambert", "fL", 3.426259),
        UnitDefinition("lam", "Lambert", "L", 3183.09886),
        UnitDefinition("sb", "Stilb", "sb", 10000.0)
    )

    // 22. Digital Storage (Base: Byte)
    val storageUnits = listOf(
        UnitDefinition("b", "Byte", "B", 1.0),
        UnitDefinition("bit", "Bit", "bit", 0.125),
        UnitDefinition("kb", "Kilobyte (1000 B)", "KB", 1000.0),
        UnitDefinition("mb", "Megabyte (1000 KB)", "MB", 1e6),
        UnitDefinition("gb", "Gigabyte (1000 MB)", "GB", 1e9),
        UnitDefinition("tb", "Terabyte (1000 GB)", "TB", 1e12),
        UnitDefinition("pb", "Petabyte (1000 TB)", "PB", 1e15),
        UnitDefinition("kib", "Kibibyte (1024 B)", "KiB", 1024.0),
        UnitDefinition("mib", "Mebibyte (1024 KiB)", "MiB", 1048576.0),
        UnitDefinition("gib", "Gibibyte (1024 MiB)", "GiB", 1073741824.0),
        UnitDefinition("tib", "Tebibyte (1024 GiB)", "TiB", 1099511627776.0)
    )

    // 23. Light Illumination (Base: Lux)
    val illuminationUnits = listOf(
        UnitDefinition("lux", "Lux", "lx", 1.0),
        UnitDefinition("fc", "Foot-candle", "fc", 10.76391),
        UnitDefinition("phot", "Phot", "ph", 10000.0),
        UnitDefinition("lm_m2", "Lumen/m²", "lm/m²", 1.0)
    )

    // 25. Fuel (Special non-linear for L/100km)
    val fuelUnits = listOf(
        UnitDefinition("kml", "Kilometer/Liter", "km/L", 1.0),
        UnitDefinition("l100km", "Liter/100km", "L/100km", 1.0),
        UnitDefinition("mpg_us", "Miles/Gallon (US)", "MPG (US)", 1.0),
        UnitDefinition("mpg_uk", "Miles/Gallon (UK)", "MPG (UK)", 1.0),
        UnitDefinition("mil", "Miles/Liter", "mi/L", 1.0)
    )

    // 26. Time (Base: Second)
    val timeUnits = listOf(
        UnitDefinition("ms", "Millisecond", "ms", 0.001),
        UnitDefinition("s", "Second", "s", 1.0),
        UnitDefinition("min", "Minute", "min", 60.0),
        UnitDefinition("hr", "Hour", "h", 3600.0),
        UnitDefinition("day", "Day", "d", 86400.0),
        UnitDefinition("wk", "Week", "wk", 604800.0),
        UnitDefinition("mo", "Month (avg)", "mo", 2629746.0),
        UnitDefinition("yr", "Year (365d)", "yr", 31536000.0)
    )

    // 29. Electric Current (Base: Ampere)
    val currentUnits = listOf(
        UnitDefinition("a", "Ampere", "A", 1.0),
        UnitDefinition("ma", "Milliampere", "mA", 0.001),
        UnitDefinition("ua", "Microampere", "µA", 1e-6),
        UnitDefinition("ka", "Kiloampere", "kA", 1000.0),
        UnitDefinition("bi", "Biot", "Bi", 10.0),
        UnitDefinition("aba", "Abampere", "abA", 10.0),
        UnitDefinition("stata", "Statampere", "statA", 3.33564e-10)
    )

    // Currency (Base: USD = 1.0)
    val currencyUnits = listOf(
        UnitDefinition("usd", "US Dollar", "$ USD", 1.0),
        UnitDefinition("inr", "Indian Rupee", "₹ INR", 1.0 / 83.95),
        UnitDefinition("eur", "Euro", "€ EUR", 1.085),
        UnitDefinition("gbp", "British Pound", "£ GBP", 1.305),
        UnitDefinition("jpy", "Japanese Yen", "¥ JPY", 1.0 / 149.5),
        UnitDefinition("cad", "Canadian Dollar", "C$ CAD", 0.728),
        UnitDefinition("aud", "Australian Dollar", "A$ AUD", 0.672),
        UnitDefinition("aed", "UAE Dirham", "AED", 0.2723),
        UnitDefinition("sar", "Saudi Riyal", "SAR", 0.2664),
        UnitDefinition("sgd", "Singapore Dollar", "S$ SGD", 0.768),
        UnitDefinition("chf", "Swiss Franc", "CHF", 1.155),
        UnitDefinition("cny", "Chinese Yuan", "¥ CNY", 0.141),
        UnitDefinition("kwd", "Kuwaiti Dinar", "KD KWD", 3.268),
        UnitDefinition("bhd", "Bahraini Dinar", "BD BHD", 2.653),
        UnitDefinition("omr", "Omani Rial", "OMR", 2.597),
        UnitDefinition("nzd", "New Zealand Dollar", "NZ$ NZD", 0.612),
        UnitDefinition("brl", "Brazilian Real", "R$ BRL", 0.178),
        UnitDefinition("zar", "South African Rand", "R ZAR", 0.057),
        UnitDefinition("rub", "Russian Ruble", "₽ RUB", 0.0105),
        UnitDefinition("thb", "Thai Baht", "฿ THB", 0.0302),
        UnitDefinition("qar", "Qatari Riyal", "QAR", 0.2747)
    )

    fun convert(
        category: String,
        value: Double,
        fromUnitId: String,
        toUnitId: String
    ): Double {
        if (fromUnitId == toUnitId) return value

        when (category) {
            "temperature" -> return convertTemperature(value, fromUnitId, toUnitId)
            "fuel" -> return convertFuel(value, fromUnitId, toUnitId)
            else -> {
                val list = getUnitsForCategory(category)
                val fromDef = list.find { it.id == fromUnitId } ?: return value
                val toDef = list.find { it.id == toUnitId } ?: return value
                val base = value * fromDef.factorToBase
                return base / toDef.factorToBase
            }
        }
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        // Convert from any to Celsius
        val celsius = when (from) {
            "c" -> value
            "f" -> (value - 32.0) * (5.0 / 9.0)
            "k" -> value - 273.15
            "r" -> (value - 491.67) * (5.0 / 9.0)
            else -> value
        }
        // Convert Celsius to target
        return when (to) {
            "c" -> celsius
            "f" -> celsius * (9.0 / 5.0) + 32.0
            "k" -> celsius + 273.15
            "r" -> (celsius + 273.15) * (9.0 / 5.0)
            else -> celsius
        }
    }

    private fun convertFuel(value: Double, from: String, to: String): Double {
        if (value <= 0) return 0.0
        // Convert from source to km/L
        val kmPerLiter = when (from) {
            "kml" -> value
            "l100km" -> 100.0 / value
            "mpg_us" -> value * 0.425143707
            "mpg_uk" -> value * 0.354006
            "mil" -> value * 1.609344
            else -> value
        }

        // Convert km/L to target
        return when (to) {
            "kml" -> kmPerLiter
            "l100km" -> 100.0 / kmPerLiter
            "mpg_us" -> kmPerLiter / 0.425143707
            "mpg_uk" -> kmPerLiter / 0.354006
            "mil" -> kmPerLiter / 1.609344
            else -> kmPerLiter
        }
    }

    fun getUnitsForCategory(category: String): List<UnitDefinition> {
        return when (category) {
            "length" -> lengthUnits
            "area" -> areaUnits
            "density" -> densityUnits
            "force" -> forceUnits
            "weight" -> weightUnits
            "temperature" -> tempUnits
            "pressure" -> pressureUnits
            "angle" -> angleUnits
            "volume" -> volumeUnits
            "speed" -> speedUnits
            "energy" -> energyUnits
            "acceleration" -> accelUnits
            "power" -> powerUnits
            "angular_accel" -> angularAccelUnits
            "frequency" -> frequencyUnits
            "torque" -> torqueUnits
            "luminance" -> luminanceUnits
            "digital_storage" -> storageUnits
            "illumination" -> illuminationUnits
            "fuel" -> fuelUnits
            "time" -> timeUnits
            "electric_current" -> currentUnits
            "currency" -> currencyUnits
            else -> emptyList()
        }
    }

    fun formatNumber(value: Double): String {
        if (value == 0.0) return "0"
        val abs = kotlin.math.abs(value)
        if (abs >= 1e9 || (abs < 1e-4 && abs > 0)) {
            return String.format(Locale.US, "%.5e", value)
        }
        val bd = BigDecimal(value.toString()).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()
        val parts = plain.split(".")
        val intPart = parts[0].toLongOrNull()
        return if (intPart != null) {
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
            val formattedInt = formatter.format(intPart)
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                "$formattedInt.${parts[1]}"
            } else {
                formattedInt
            }
        } else {
            plain
        }
    }
}
