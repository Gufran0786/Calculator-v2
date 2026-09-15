package com.example.calculator.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

sealed class EvalResult {
    data class Success(val value: String, val numericValue: Double) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

enum class AngleUnit(val label: String) {
    DEG("DEG"),
    RAD("RAD"),
    GRAD("GRAD")
}

object CalculatorEvaluator {

    fun evaluate(expression: String, angleUnit: AngleUnit): EvalResult {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("∛", "cbrt")
            .replace("√", "sqrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("eˣ", "exp")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("π", "pi")
            .replace("nPr", "P")
            .replace("nCr", "C")
            .replace(",", "")
            .trim()

        if (sanitized.isEmpty()) {
            return EvalResult.Success("0", 0.0)
        }

        return try {
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) {
                return EvalResult.Success("0", 0.0)
            }
            val parser = ExpressionParser(tokens, angleUnit)
            val result = parser.parse()
            if (result.isInfinite()) {
                EvalResult.Error("Overflow / Division by 0")
            } else if (result.isNaN()) {
                EvalResult.Error("Invalid calculation")
            } else {
                EvalResult.Success(formatResult(result), result)
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Calculation error")
        } catch (e: Exception) {
            EvalResult.Error("Format Error")
        }
    }

    fun evaluate(expression: String, isDegreeMode: Boolean = true): EvalResult {
        val unit = if (isDegreeMode) AngleUnit.DEG else AngleUnit.RAD
        return evaluate(expression, unit)
    }

    fun preview(expression: String, angleUnit: AngleUnit): String? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        val lastChar = trimmed.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '^', '.', 'P', 'C')) {
            return null
        }

        // Check if there is an actual operator or function
        val hasScientificOrOp = trimmed.any { it in listOf('+', '−', '-', '×', '*', '÷', '/', '%', '^', '!', '√', 'π', 'P', 'C') } ||
                trimmed.contains("sin") || trimmed.contains("cos") || trimmed.contains("tan") ||
                trimmed.contains("log") || trimmed.contains("ln") || trimmed.contains("sqrt")

        if (!hasScientificOrOp) {
            return null
        }

        // Auto-close unbalanced parentheses for live preview
        var previewExpr = trimmed
        val openCount = previewExpr.count { it == '(' }
        val closeCount = previewExpr.count { it == ')' }
        if (openCount > closeCount) {
            previewExpr += ")".repeat(openCount - closeCount)
        }

        return when (val result = evaluate(previewExpr, angleUnit)) {
            is EvalResult.Success -> result.value
            is EvalResult.Error -> null
        }
    }

    fun preview(expression: String, isDegreeMode: Boolean = true): String? {
        val unit = if (isDegreeMode) AngleUnit.DEG else AngleUnit.RAD
        return preview(expression, unit)
    }

    private val FUNCTION_NAMES = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "log", "ln", "sqrt", "cbrt", "abs", "exp"
    )

    private fun tokenize(expr: String): List<String> {
        val rawTokens = mutableListOf<String>()
        var i = 0
        val n = expr.length

        while (i < n) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "+*/()^!" -> {
                    rawTokens.add(c.toString())
                    i++
                }
                c == '%' -> {
                    rawTokens.add("%")
                    i++
                }
                c == '-' -> {
                    // Check if unary minus
                    val isUnary = rawTokens.isEmpty() ||
                            rawTokens.last() in listOf("+", "-", "*", "/", "^", "(", "%") ||
                            rawTokens.last() in FUNCTION_NAMES
                    if (isUnary) {
                        rawTokens.add("u-")
                    } else {
                        rawTokens.add("-")
                    }
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < n && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    rawTokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < n && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    val word = sb.toString().lowercase(Locale.ROOT)
                    rawTokens.add(word)
                }
                else -> i++
            }
        }

        // Insert implicit multiplication tokens where appropriate
        // E.g.: 2(3) -> 2 * (3), 2pi -> 2 * pi, 2sin(30) -> 2 * sin(30), (2)(3) -> (2) * (3)
        val tokens = mutableListOf<String>()
        for (idx in rawTokens.indices) {
            val curr = rawTokens[idx]
            tokens.add(curr)
            if (idx + 1 < rawTokens.size) {
                val next = rawTokens[idx + 1]
                val currIsPrimaryEnd = curr.toDoubleOrNull() != null || curr == "pi" || curr == "e" || curr == ")" || curr == "!" || curr == "%"
                val nextIsPrimaryStart = next.toDoubleOrNull() != null || next == "pi" || next == "e" || next == "(" || next in FUNCTION_NAMES
                if (currIsPrimaryEnd && nextIsPrimaryStart) {
                    tokens.add("*")
                }
            }
        }

        return tokens
    }

    private fun formatResult(value: Double): String {
        if (value == 0.0 || value == -0.0) return "0"

        // For very large or very small numbers, use scientific notation
        val absVal = abs(value)
        if (absVal >= 1e12 || (absVal < 1e-6 && absVal > 0)) {
            return String.format(Locale.US, "%.6e", value).replace("e+0", "e+").replace("e-0", "e-")
        }

        val bd = BigDecimal(value.toString())
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val plain = bd.toPlainString()
        val parts = plain.split(".")
        val integerPart = parts[0].toLongOrNull()

        return if (integerPart != null) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,###", symbols)
            val formattedInt = formatter.format(integerPart)
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                "$formattedInt.${parts[1]}"
            } else {
                formattedInt
            }
        } else {
            plain
        }
    }

    private class ExpressionParser(
        private val tokens: List<String>,
        private val isDegreeMode: Boolean
    ) {
        private var pos = 0

        fun parse(): Double {
            val value = parseAdditionSubtraction()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Unexpected token: ${tokens[pos]}")
            }
            return value
        }

        private fun parseAdditionSubtraction(): Double {
            var left = parseMultiplicationDivision()
            while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
                val op = tokens[pos++]
                val right = parseMultiplicationDivision()
                left = if (op == "+") left + right else left - right
            }
            return left
        }

        private fun parseMultiplicationDivision(): Double {
            var left = parsePower()
            while (pos < tokens.size && (tokens[pos] == "*" || tokens[pos] == "/" || tokens[pos] == "%")) {
                val op = tokens[pos++]
                if (op == "%") {
                    left = left / 100.0
                } else {
                    val right = parsePower()
                    if (op == "*") {
                        left = left * right
                    } else {
                        if (right == 0.0) throw ArithmeticException("Cannot divide by 0")
                        left = left / right
                    }
                }
            }
            return left
        }

        private fun parsePower(): Double {
            var left = parseUnary()
            if (pos < tokens.size && tokens[pos] == "^") {
                pos++
                val right = parseUnary()
                left = left.pow(right)
            }
            return left
        }

        private fun parseUnary(): Double {
            if (pos < tokens.size && tokens[pos] == "u-") {
                pos++
                return -parseUnary()
            }
            if (pos < tokens.size && tokens[pos] == "+") {
                pos++
                return parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (pos < tokens.size && (tokens[pos] == "!" || tokens[pos] == "%")) {
                val op = tokens[pos++]
                if (op == "!") {
                    value = factorial(value)
                } else if (op == "%") {
                    value = value / 100.0
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            if (pos >= tokens.size) throw IllegalArgumentException("Unexpected end of expression")

            val token = tokens[pos++]
            return when {
                token == "(" -> {
                    val value = parseAdditionSubtraction()
                    if (pos < tokens.size && tokens[pos] == ")") {
                        pos++
                    }
                    value
                }
                token == "pi" -> Math.PI
                token == "e" -> Math.E
                FUNCTION_NAMES.contains(token) -> {
                    // Function call, expected to be followed by "("
                    if (pos < tokens.size && tokens[pos] == "(") {
                        pos++
                    }
                    val arg = parseAdditionSubtraction()
                    if (pos < tokens.size && tokens[pos] == ")") {
                        pos++
                    }
                    evaluateFunction(token, arg)
                }
                token.endsWith("%") -> {
                    val num = token.dropLast(1).toDouble()
                    num / 100.0
                }
                else -> {
                    token.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number or symbol: $token")
                }
            }
        }

        private fun evaluateFunction(func: String, arg: Double): Double {
            return when (func) {
                "sin" -> {
                    val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                    val cleanRad = if (isDegreeMode && (abs(arg % 180.0) < 1e-9)) 0.0 else rad
                    val res = kotlin.math.sin(cleanRad)
                    if (abs(res) < 1e-15) 0.0 else res
                }
                "cos" -> {
                    if (isDegreeMode && abs((abs(arg) - 90.0) % 180.0) < 1e-9) {
                        0.0
                    } else {
                        val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                        val res = kotlin.math.cos(rad)
                        if (abs(res) < 1e-15) 0.0 else res
                    }
                }
                "tan" -> {
                    if (isDegreeMode && abs((abs(arg) - 90.0) % 180.0) < 1e-9) {
                        throw ArithmeticException("Undefined (tan 90°)")
                    }
                    val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                    val res = kotlin.math.tan(rad)
                    if (abs(res) < 1e-15) 0.0 else res
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: asin requires [-1, 1]")
                    val rad = kotlin.math.asin(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: acos requires [-1, 1]")
                    val rad = kotlin.math.acos(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = kotlin.math.atan(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "log" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error: log requires > 0")
                    kotlin.math.log10(arg)
                }
                "ln" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error: ln requires > 0")
                    kotlin.math.ln(arg)
                }
                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Domain error: sqrt of negative number")
                    kotlin.math.sqrt(arg)
                }
                "cbrt" -> {
                    kotlin.math.cbrt(arg)
                }
                "abs" -> {
                    abs(arg)
                }
                "exp" -> {
                    kotlin.math.exp(arg)
                }
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) {
                throw ArithmeticException("Factorial is only defined for non-negative integers")
            }
            if (n > 170) {
                throw ArithmeticException("Overflow (result too large)")
            }
            val intN = n.toLong()
            var res = 1.0
            for (i in 2..intN) {
                res *= i
            }
            return res
        }
    }
}
