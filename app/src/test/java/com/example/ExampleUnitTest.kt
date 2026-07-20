package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        // Addition
        assertEquals(15.0, Utils.evaluate("10 + 5"), 1e-9)
        assertEquals(5.5, Utils.evaluate("2.3 + 3.2"), 1e-9)

        // Subtraction
        assertEquals(5.0, Utils.evaluate("10 - 5"), 1e-9)
        assertEquals(-1.0, Utils.evaluate("2 - 3"), 1e-9)

        // Multiplication
        assertEquals(50.0, Utils.evaluate("10 × 5"), 1e-9)
        assertEquals(0.0, Utils.evaluate("0 × 123.45"), 1e-9)

        // Division
        assertEquals(2.0, Utils.evaluate("10 ÷ 5"), 1e-9)
        assertEquals(2.5, Utils.evaluate("5 ÷ 2"), 1e-9)
    }

    @Test
    fun testOperatorPrecedence() {
        // Operator precedence (multiplication/division before addition/subtraction)
        assertEquals(17.0, Utils.evaluate("5 + 4 × 3"), 1e-9)
        assertEquals(23.0, Utils.evaluate("5 × 4 + 3"), 1e-9)
        assertEquals(11.0, Utils.evaluate("12 - 4 ÷ 2 - 1 + 2"), 1e-9)
    }

    @Test
    fun testPercentage() {
        // Percentage operations (division by 100)
        assertEquals(0.5, Utils.evaluate("50%"), 1e-9)
        assertEquals(5.1, Utils.evaluate("5 + 10%"), 1e-9)
        assertEquals(0.025, Utils.evaluate("2.5%"), 1e-9)
    }

    @Test
    fun testScientificFunctions() {
        // Powers / Exponents
        assertEquals(8.0, Utils.evaluate("2^3"), 1e-9)
        assertEquals(1.0, Utils.evaluate("10^0"), 1e-9)

        // Square root
        assertEquals(4.0, Utils.evaluate("sqrt(16)"), 1e-9)

        // Factorial
        assertEquals(120.0, Utils.factorial(5.0), 1e-9)
        assertEquals(1.0, Utils.factorial(0.0), 1e-9)

        // Trigonometry (In Degrees)
        assertEquals(0.5, Utils.evaluate("sin(30)"), 1e-6)
        assertEquals(1.0, Utils.evaluate("cos(0)"), 1e-6)
        assertEquals(1.0, Utils.evaluate("tan(45)"), 1e-6)

        // Logarithms
        assertEquals(2.0, Utils.evaluate("log(100)"), 1e-9)
        assertEquals(1.0, Utils.evaluate("ln(e)"), 1e-9)
    }

    @Test
    fun testBrackets() {
        assertEquals(27.0, Utils.evaluate("(5 + 4) × 3"), 1e-9)
        assertEquals(3.0, Utils.evaluate("(10 - 4) ÷ (3 - 1)"), 1e-9)
    }

    @Test
    fun testDivisionByZero() {
        try {
            Utils.evaluate("5 ÷ 0")
            fail("Expected ArithmeticException")
        } catch (e: ArithmeticException) {
            assertEquals("Cannot divide by zero", e.message)
        }
    }

    @Test
    fun testInvalidInputs() {
        try {
            Utils.evaluate("5 + * 3")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            Utils.evaluate("sin(")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    @Test
    fun testFormatting() {
        assertEquals("5", Utils.formatResult(5.0))
        assertEquals("5.123", Utils.formatResult(5.1230000))
        assertEquals("0.3", Utils.formatResult(0.30000000000000004))
    }
}
