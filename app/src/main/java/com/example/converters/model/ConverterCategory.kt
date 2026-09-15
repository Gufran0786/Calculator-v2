package com.example.converters.model

enum class ConverterGroup(val title: String) {
    UNIT("Unit Converters"),
    SPECIAL("Special & Lifestyle"),
    FINANCE("Financial Calculators")
}

enum class ConverterType(
    val id: String,
    val title: String,
    val group: ConverterGroup,
    val description: String,
    val iconName: String
) {
    // 1-12 General Units
    LENGTH("length", "Length", ConverterGroup.UNIT, "Meter, km, feet, inch, mile...", "Straighten"),
    AREA("area", "Area", ConverterGroup.UNIT, "Sq meter, sq ft, acre, hectare...", "SquareFoot"),
    DENSITY("density", "Density", ConverterGroup.UNIT, "kg/m³, g/cm³, lb/ft³...", "Grain"),
    FORCE("force", "Force", ConverterGroup.UNIT, "Newton, dyne, lbf, kgf...", "FitnessCenter"),
    WEIGHT("weight", "Weight", ConverterGroup.UNIT, "Kilogram, gram, pound, ounce...", "Scale"),
    TEMPERATURE("temperature", "Temperature", ConverterGroup.UNIT, "Celsius, Fahrenheit, Kelvin...", "Thermostat"),
    PRESSURE("pressure", "Pressure", ConverterGroup.UNIT, "Pascal, bar, psi, atm, torr...", "Speed"),
    ANGLE("angle", "Angle", ConverterGroup.UNIT, "Degree, radian, gradian...", "ChangeHistory"),
    VOLUME("volume", "Volume", ConverterGroup.UNIT, "Liter, gallon, m³, cup, ml...", "WaterDrop"),
    SPEED("speed", "Speed", ConverterGroup.UNIT, "km/h, mph, m/s, knot...", "DirectionsRun"),
    ENERGY("energy", "Energy", ConverterGroup.UNIT, "Joule, calorie, kWh, BTU...", "Bolt"),
    ACCELERATION("acceleration", "Acceleration", ConverterGroup.UNIT, "m/s², km/h², g...", "RocketLaunch"),

    // 13-16 Tech / Physical
    NUMBER_BASE("number_base", "Number", ConverterGroup.SPECIAL, "Decimal, Binary, Hex, Octal, Roman", "Pin"),
    POWER("power", "Power", ConverterGroup.UNIT, "Watt, kW, Horsepower, BTU/h...", "ElectricBolt"),
    ANGULAR_ACCEL("angular_accel", "Angular Acceleration", ConverterGroup.UNIT, "rad/s², deg/s², rpm/s...", "Sync"),
    FREQUENCY("frequency", "Frequency", ConverterGroup.UNIT, "Hertz, kHz, MHz, GHz, RPM...", "Waves"),

    // 17-23 Physical / Time / Light
    TIME_ZONE("time_zone", "Time Zone Converter", ConverterGroup.SPECIAL, "Convert time between world cities", "Public"),
    TIME_ZONE_COMPARE("time_zone_compare", "Time Zone Comparison", ConverterGroup.SPECIAL, "Compare multiple world clocks", "Schedule"),
    TORQUE("torque", "Torque", ConverterGroup.UNIT, "N·m, lbf·ft, kgf·m...", "RotateRight"),
    LUMINANCE("luminance", "Light Luminance", ConverterGroup.UNIT, "cd/m², nit, foot-lambert...", "WbSunny"),
    COLOUR("colour", "Colour", ConverterGroup.SPECIAL, "HEX, RGB, HSL, HSV, CMYK", "Palette"),
    DIGITAL_STORAGE("digital_storage", "Digital Storage", ConverterGroup.UNIT, "Byte, KB, MB, GB, TB, PB...", "Storage"),
    ILLUMINATION("illumination", "Light Illumination", ConverterGroup.UNIT, "Lux, foot-candle, phot...", "Lightbulb"),

    // 24-30 Daily / Words / Sizes
    NUMBER_WORDS_IND("number_words_ind", "Number to Words (IND)", ConverterGroup.SPECIAL, "Lakhs, Crores format", "FormatQuote"),
    FUEL("fuel", "Fuel", ConverterGroup.UNIT, "km/L, L/100km, MPG US/UK...", "LocalGasStation"),
    TIME("time", "Time", ConverterGroup.UNIT, "Sec, min, hr, day, week, year...", "HourglassTop"),
    NUMBER_WORDS_INT("number_words_int", "Number to Words (INT)", ConverterGroup.SPECIAL, "Millions, Billions format", "Translate"),
    SHOE_SIZE("shoe_size", "Shoe Size", ConverterGroup.SPECIAL, "US, UK, EU, CM, INCH sizing", "RollerShades"),
    ELECTRIC_CURRENT("electric_current", "Electric Current", ConverterGroup.UNIT, "Ampere, mA, µA, kA...", "FlashOn"),
    CLOTH_SIZE("cloth_size", "Cloth Size", ConverterGroup.SPECIAL, "International clothing sizes", "Checkroom"),

    // 31-43 Financial Tools
    LOAN_EMI("loan_emi", "Loan EMI", ConverterGroup.FINANCE, "Monthly payment & interest calculator", "AccountBalance"),
    PURCHASE_EMI_GST("purchase_emi_gst", "Purchase EMI (with GST)", ConverterGroup.FINANCE, "EMI breakdown with GST on interest", "ShoppingCart"),
    SIP("sip", "SIP", ConverterGroup.FINANCE, "Systematic Investment Plan", "TrendingUp"),
    SWP("swp", "SWP", ConverterGroup.FINANCE, "Systematic Withdrawal Plan", "Paid"),
    EPF("epf", "EPF", ConverterGroup.FINANCE, "Employee Provident Fund Corpus", "Savings"),
    PPF("ppf", "PPF", ConverterGroup.FINANCE, "Public Provident Fund (15 yr)", "AccountBalanceWallet"),
    NPS("nps", "NPS", ConverterGroup.FINANCE, "National Pension Scheme & Annuity", "Elderly"),
    FD("fd", "FD", ConverterGroup.FINANCE, "Fixed Deposit maturity calculation", "MonetizationOn"),
    RD("rd", "RD", ConverterGroup.FINANCE, "Recurring Deposit returns", "Repeat"),
    COMPOUND_INTEREST("compound_interest", "Compound Interest", ConverterGroup.FINANCE, "P*(1+r/n)^(nt) compound growth", "Calculate"),
    DISCOUNT("discount", "Discount", ConverterGroup.FINANCE, "Discount %, savings & tax", "Sell"),
    ROI("roi", "ROI", ConverterGroup.FINANCE, "Return on Investment & CAGR", "Insights"),
    GST("gst", "GST", ConverterGroup.FINANCE, "Add or Remove GST (5%, 12%, 18%, 28%)", "ReceiptLong"),

    // 44-46 Lifestyle / Construction & Health
    TILES("tiles", "Tiles", ConverterGroup.SPECIAL, "Floor tiles, boxes & wastage buffer", "GridOn"),
    GOLD_COMPOSITION("gold_composition", "Gold Composition", ConverterGroup.SPECIAL, "Karat purity (24K, 22K..), grams & value", "Diamond"),
    AGE("age", "Age", ConverterGroup.SPECIAL, "Years, months, days & next birthday", "Cake"),
    BMI("bmi", "BMI", ConverterGroup.SPECIAL, "Body Mass Index, category & healthy weight", "FitnessCenter"),

    // Currency
    CURRENCY("currency", "Currency", ConverterGroup.UNIT, "USD, EUR, INR, GBP, JPY, CAD, AUD, AED...", "Paid");

    companion object {
        fun findById(id: String): ConverterType? = values().firstOrNull { it.id == id }
    }
}
