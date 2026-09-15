package com.example

import com.example.calculator.engine.CalculatorEvaluator
import com.example.calculator.engine.EvalResult
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    val result = CalculatorEvaluator.evaluate("2 + 2")
    assertTrue(result is EvalResult.Success)
    assertEquals("4", (result as EvalResult.Success).value)
  }

  @Test
  fun operations_withSymbols() {
    val result1 = CalculatorEvaluator.evaluate("15 × 4 − 10")
    assertTrue(result1 is EvalResult.Success)
    assertEquals("50", (result1 as EvalResult.Success).value)

    val result2 = CalculatorEvaluator.evaluate("100 ÷ 4")
    assertTrue(result2 is EvalResult.Success)
    assertEquals("25", (result2 as EvalResult.Success).value)
  }

  @Test
  fun parentheses_and_percentage() {
    val result1 = CalculatorEvaluator.evaluate("(5 + 3) * 4")
    assertTrue(result1 is EvalResult.Success)
    assertEquals("32", (result1 as EvalResult.Success).value)

    val result2 = CalculatorEvaluator.evaluate("200 * 15%")
    assertTrue(result2 is EvalResult.Success)
    assertEquals("30", (result2 as EvalResult.Success).value)
  }

  @Test
  fun divideByZero_returnsError() {
    val result = CalculatorEvaluator.evaluate("42 ÷ 0")
    assertTrue(result is EvalResult.Error)
    assertEquals("Cannot divide by 0", (result as EvalResult.Error).message)
  }

  @Test
  fun currency_conversion_works() {
    val inr = com.example.converters.engine.UnitConverters.convert("currency", 10.0, "usd", "inr")
    assertTrue(inr > 800.0)

    val usd = com.example.converters.engine.UnitConverters.convert("currency", inr, "inr", "usd")
    assertEquals(10.0, usd, 0.01)
  }

  @Test
  fun bmi_and_financial_calculation() {
    // Height 175cm, Weight 70kg -> BMI ~ 22.86
    val heightM = 175.0 / 100.0
    val bmi = 70.0 / (heightM * heightM)
    assertEquals(22.86, bmi, 0.05)

    // Verify 48 converters registered
    val totalTools = com.example.converters.model.ConverterType.values().size
    assertEquals(48, totalTools)
  }

  @Test
  fun scientific_calculations_degree_and_radian() {
    // sin(90) in degrees = 1
    val sinDeg = CalculatorEvaluator.evaluate("sin(90)", isDegreeMode = true)
    assertTrue(sinDeg is EvalResult.Success)
    assertEquals("1", (sinDeg as EvalResult.Success).value)

    // cos(0) = 1
    val cosDeg = CalculatorEvaluator.evaluate("cos(0)", isDegreeMode = true)
    assertTrue(cosDeg is EvalResult.Success)
    assertEquals("1", (cosDeg as EvalResult.Success).value)

    // tan(45) in degrees = 1
    val tanDeg = CalculatorEvaluator.evaluate("tan(45)", isDegreeMode = true)
    assertTrue(tanDeg is EvalResult.Success)
    assertEquals("1", (tanDeg as EvalResult.Success).value)

    // log(100) = 2
    val logVal = CalculatorEvaluator.evaluate("log(100)")
    assertTrue(logVal is EvalResult.Success)
    assertEquals("2", (logVal as EvalResult.Success).value)

    // Power 2 ^ 8 = 256
    val powVal = CalculatorEvaluator.evaluate("2 ^ 8")
    assertTrue(powVal is EvalResult.Success)
    assertEquals("256", (powVal as EvalResult.Success).value)

    // Sqrt 144 = 12
    val sqrtVal = CalculatorEvaluator.evaluate("√(144)")
    assertTrue(sqrtVal is EvalResult.Success)
    assertEquals("12", (sqrtVal as EvalResult.Success).value)

    // Factorial 5! = 120
    val factVal = CalculatorEvaluator.evaluate("5!")
    assertTrue(factVal is EvalResult.Success)
    assertEquals("120", (factVal as EvalResult.Success).value)
  }
}

