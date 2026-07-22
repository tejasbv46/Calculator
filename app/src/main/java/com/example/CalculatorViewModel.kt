package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryEntity
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    // Expose Room persistent history reactively to the Compose UI
    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var accumulatedFormula = ""
    private var isLastActionEquals = false

    fun onAction(action: CalculatorButtonAction) {
        _state.update { it.copy(isError = false, errorMessage = "") }

        when (action) {
            is CalculatorButtonAction.Number -> handleNumber(action.number)
            is CalculatorButtonAction.Decimal -> handleDecimal()
            is CalculatorButtonAction.Operation -> handleOperation(action.operation)
            is CalculatorButtonAction.Clear -> handleClear()
            is CalculatorButtonAction.AllClear -> handleAllClear()
            is CalculatorButtonAction.Backspace -> handleBackspace()
            is CalculatorButtonAction.Equals -> handleEquals()
            is CalculatorButtonAction.ToggleSign -> handleToggleSign()
            is CalculatorButtonAction.Percent -> handlePercent()
            is CalculatorButtonAction.ScientificFunc -> handleScientificFunc(action.func)
            is CalculatorButtonAction.Constant -> handleConstant(action.value)
            is CalculatorButtonAction.OpenParenthesis -> handleOpenParenthesis()
            is CalculatorButtonAction.CloseParenthesis -> handleCloseParenthesis()
            is CalculatorButtonAction.MemoryClear -> handleMemoryClear()
            is CalculatorButtonAction.MemoryRecall -> handleMemoryRecall()
            is CalculatorButtonAction.MemoryAdd -> handleMemoryAdd()
            is CalculatorButtonAction.MemorySubtract -> handleMemorySubtract()
        }
    }

    private fun handleNumber(number: String) {
        if (isLastActionEquals || _state.value.currentInput == "0" || _state.value.currentInput == "error") {
            _state.update { it.copy(currentInput = number) }
            isLastActionEquals = false
        } else {
            _state.update { it.copy(currentInput = it.currentInput + number) }
        }
    }

    private fun handleDecimal() {
        if (isLastActionEquals) {
            _state.update { it.copy(currentInput = "0.") }
            isLastActionEquals = false
            return
        }

        val current = _state.value.currentInput
        if (!current.contains(".")) {
            _state.update { it.copy(currentInput = if (current.isEmpty()) "0." else "$current.") }
        }
    }

    private fun handleOperation(op: CalculatorOperation) {
        val current = _state.value.currentInput
        
        if (current == "Error" || current.startsWith("Error")) return

        // If last action was equals, we can continue calculating from the previous result
        if (isLastActionEquals) {
            accumulatedFormula = current
            isLastActionEquals = false
        } else {
            if (current != "0") {
                accumulatedFormula += current
            }
        }

        // Avoid double operations (e.g. "++")
        if (accumulatedFormula.endsWith(" + ") || accumulatedFormula.endsWith(" - ") ||
            accumulatedFormula.endsWith(" × ") || accumulatedFormula.endsWith(" ÷ ")
        ) {
            accumulatedFormula = accumulatedFormula.dropLast(3)
        }

        accumulatedFormula += " ${op.symbol} "
        
        _state.update {
            it.copy(
                previousExpression = accumulatedFormula,
                currentInput = "0",
                operation = op
            )
        }
    }

    private fun handleClear() {
        _state.update { it.copy(currentInput = "0") }
    }

    private fun handleAllClear() {
        accumulatedFormula = ""
        isLastActionEquals = false
        _state.update {
            it.copy(
                currentInput = "0",
                previousExpression = "",
                operation = null,
                isError = false,
                errorMessage = ""
            )
        }
    }

    private fun handleBackspace() {
        if (isLastActionEquals) {
            handleAllClear()
            return
        }

        val current = _state.value.currentInput
        if (current.isNotEmpty() && current != "0") {
            val updated = current.dropLast(1)
            _state.update {
                it.copy(currentInput = if (updated.isEmpty() || updated == "-") "0" else updated)
            }
        }
    }

    private fun handleEquals() {
        val current = _state.value.currentInput
        if (current == "Error" || current.startsWith("Error")) return

        var fullExpression = accumulatedFormula + current

        if (fullExpression.isBlank() || fullExpression == "0") return

        // Balance parentheses if needed
        val openCount = fullExpression.count { it == '(' }
        val closeCount = fullExpression.count { it == ')' }
        if (openCount > closeCount) {
            fullExpression += ")".repeat(openCount - closeCount)
        }

        try {
            val resultValue = Utils.evaluate(fullExpression)
            val formattedResult = Utils.formatResult(resultValue)

            // Save to history in Room database
            viewModelScope.launch {
                repository.insert(
                    HistoryEntity(
                        expression = fullExpression,
                        result = formattedResult
                    )
                )
            }

            _state.update {
                it.copy(
                    previousExpression = "$fullExpression =",
                    currentInput = formattedResult,
                    operation = null
                )
            }
            accumulatedFormula = ""
            isLastActionEquals = true
        } catch (e: ArithmeticException) {
            _state.update {
                it.copy(
                    currentInput = "Error",
                    isError = true,
                    errorMessage = e.message ?: "Calculation error"
                )
            }
            isLastActionEquals = true
        } catch (e: Exception) {
            _state.update {
                it.copy(

                    errorMessage = " error",
                    isError = true,
                    currentInput = "zero dividon error",

                )
            }
            isLastActionEquals = true
        }
    }

    private fun handleToggleSign() {
        val current = _state.value.currentInput
        if (current == "0" || current == "Error") return

        val updated = if (current.startsWith("-")) {
            current.substring(1)
        } else {
            "-$current"
        }
        _state.update { it.copy(currentInput = updated) }
    }

    private fun handlePercent() {
        val current = _state.value.currentInput
        if (current == "0" || current == "Error") return

        try {
            val value = current.toDouble() / 100.0
            _state.update { it.copy(currentInput = Utils.formatResult(value)) }
        } catch (e: Exception) {
            _state.update { it.copy(currentInput = "Error") }
        }
    }

    private fun handleScientificFunc(func: String) {
        val current = _state.value.currentInput
        when (func) {
            "1/x" -> {
                if (current == "0" || current == "Error") {
                    _state.update { it.copy(currentInput = "Error", isError = true, errorMessage = "Cannot divide by zero") }
                    return
                }
                try {
                    val res = 1.0 / current.toDouble()
                    _state.update { it.copy(currentInput = Utils.formatResult(res)) }
                } catch (e: Exception) {
                    _state.update { it.copy(currentInput = "Error") }
                }
            }
            "x^2" -> {
                if (current == "Error") return
                try {
                    val res = current.toDouble().let { it * it }
                    _state.update { it.copy(currentInput = Utils.formatResult(res)) }
                } catch (e: Exception) {
                    _state.update { it.copy(currentInput = "Error") }
                }
            }
            "x!" -> {
                if (current == "Error") return
                try {
                    val res = Utils.factorial(current.toDouble())
                    if (res.isNaN()) {
                        _state.update { it.copy(currentInput = "Error", isError = true, errorMessage = "Integer >= 0 required") }
                    } else {
                        _state.update { it.copy(currentInput = Utils.formatResult(res)) }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(currentInput = "Error") }
                }
            }
            "sqrt" -> {
                accumulatedFormula += "sqrt("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "sin" -> {
                accumulatedFormula += "sin("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "cos" -> {
                accumulatedFormula += "cos("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "tan" -> {
                accumulatedFormula += "tan("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "log" -> {
                accumulatedFormula += "log("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "ln" -> {
                accumulatedFormula += "ln("
                _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
            }
            "^" -> {
                if (current != "Error" && current != "0") {
                    accumulatedFormula += "$current^"
                    _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
                }
            }
        }
    }

    private fun handleConstant(constant: String) {
        val value = if (constant == "π") "π" else "e"
        if (isLastActionEquals || _state.value.currentInput == "0") {
            _state.update { it.copy(currentInput = value) }
            isLastActionEquals = false
        } else {
            _state.update { it.copy(currentInput = _state.value.currentInput + value) }
        }
    }

    private fun handleOpenParenthesis() {
        if (isLastActionEquals) {
            accumulatedFormula = "("
            isLastActionEquals = false
        } else {
            accumulatedFormula += if (_state.value.currentInput == "0") "(" else _state.value.currentInput + "("
        }
        _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
    }

    private fun handleCloseParenthesis() {
        val current = _state.value.currentInput
        accumulatedFormula += if (current == "0") ")" else "$current)"
        _state.update { it.copy(previousExpression = accumulatedFormula, currentInput = "0") }
    }

    // Memory Keys MC, MR, M+, M-
    private fun handleMemoryClear() {
        _state.update { it.copy(memoryValue = 0.0) }
    }

    private fun handleMemoryRecall() {
        _state.update { it.copy(currentInput = Utils.formatResult(it.memoryValue)) }
        isLastActionEquals = false
    }

    private fun handleMemoryAdd() {
        val current = _state.value.currentInput
        if (current != "Error") {
            try {
                val value = current.toDouble()
                _state.update { it.copy(memoryValue = it.memoryValue + value) }
            } catch (e: Exception) {
                // Ignore parsing errors for constants in input, evaluate first or handle
            }
        }
    }

    private fun handleMemorySubtract() {
        val current = _state.value.currentInput
        if (current != "Error") {
            try {
                val value = current.toDouble()
                _state.update { it.copy(memoryValue = it.memoryValue - value) }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleScientificMode() {
        _state.update { it.copy(isScientific = !it.isScientific) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun selectHistoryItem(item: HistoryEntity) {
        _state.update {
            it.copy(
                previousExpression = item.expression,
                currentInput = item.result
            )
        }
        accumulatedFormula = ""
        isLastActionEquals = true
    }

    // Factory Provider
    class Factory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
                return CalculatorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
