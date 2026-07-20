package com.example

data class CalculatorState(
    val currentInput: String = "0",       // The active number being typed or evaluated
    val previousExpression: String = "",  // The upper formula preview (e.g., "12 + 3 ×")
    val operation: CalculatorOperation? = null,
    val memoryValue: Double = 0.0,
    val isScientific: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
