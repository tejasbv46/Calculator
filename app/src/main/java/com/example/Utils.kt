package com.example

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

object Utils {

    /**
     * Formats a double value nicely to avoid floating point precision issues like 0.30000000000000004.
     * Trims trailing zeros and handles scientific notation for very large or small values.
     */
    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Error: Overflow"
        
        // Handle close-to-zero values to prevent things like -0.0
        val cleanValue = if (abs(value) < 1e-15) 0.0 else value
        
        // Use BigDecimal to format precisely
        return try {
            val bd = BigDecimal(cleanValue.toString())
            if (abs(cleanValue) >= 1e12 || (abs(cleanValue) < 1e-6 && cleanValue != 0.0)) {
                // Return scientific notation for extremely large or small numbers
                String.format("%.6e", cleanValue)
                    .replace("e+0", "e")
                    .replace("e+", "e")
            } else {
                val scaled = bd.setScale(10, RoundingMode.HALF_UP)
                val str = scaled.toPlainString()
                if (str.contains(".")) {
                    var end = str.length
                    while (end > 0 && str[end - 1] == '0') {
                        end--
                    }
                    if (end > 0 && str[end - 1] == '.') {
                        end--
                    }
                    if (end <= 0) "0" else str.substring(0, end)
                } else {
                    str
                }
            }
        } catch (e: Exception) {
            cleanValue.toString()
        }
    }

    /**
     * Parses and evaluates a mathematical string expression with operator precedence,
     * parenthesis support, percentage division, and scientific functions.
     */
    fun evaluate(expression: String): Double {
        val parsedExpr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "3.141592653589793")
            .replace("e", "2.718281828459045")

        return try {
            Parser(parsedExpr).parse()
        } catch (e: ArithmeticException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Expression")
        }
    }

    private class Parser(val str: String) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Grammar rules:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | function factor | factor `%` | factor `^` factor

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else break
            }
            return x
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) {
                        throw ArithmeticException("Cannot divide by zero")
                    }
                    x /= divisor // division
                } else break
            }
            return x
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                val numStr = str.substring(startPos, this.pos)
                x = numStr.toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, this.pos)
                
                // If it's single character constant or log base etc.
                val arg = parseFactor()
                x = when (func) {
                    "sqrt" -> {
                        if (arg < 0.0) throw ArithmeticException("Invalid input for square root")
                        sqrt(arg)
                    }
                    "sin" -> sin(Math.toRadians(arg))
                    "cos" -> cos(Math.toRadians(arg))
                    "tan" -> {
                        // Check for tan(90) which is undefined
                        if (abs(cos(Math.toRadians(arg))) < 1e-10) {
                            throw ArithmeticException("Undefined value")
                        }
                        tan(Math.toRadians(arg))
                    }
                    "log" -> {
                        if (arg <= 0.0) throw ArithmeticException("Invalid log input")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0.0) throw ArithmeticException("Invalid ln input")
                        ln(arg)
                    }
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected character: " + ch.toChar())
            }

            // Suffix operations like percent (%)
            if (eat('%'.code)) {
                x /= 100.0
            }

            // Power/Exponentiation operation (^)
            if (eat('^'.code)) {
                val power = parseFactor()
                x = x.pow(power)
            }

            return x
        }
    }

    /**
     * Computes the factorial of a number
     */
    fun factorial(n: Double): Double {
        if (n < 0.0 || n % 1.0 != 0.0) return Double.NaN
        val intN = n.toInt()
        if (intN > 170) return Double.POSITIVE_INFINITY // Exceeds double capacity
        var result = 1.0
        for (i in 1..intN) {
            result *= i
        }
        return result
    }
}
