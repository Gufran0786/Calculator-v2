package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.engine.CalculatorEvaluator
import com.example.calculator.engine.EvalResult
import com.example.data.db.CalculationHistory
import com.example.data.repository.CalculatorRepository
import com.example.converters.model.ConverterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    CALCULATOR,
    CONVERTER
}

enum class AppThemeMode(val id: String, val title: String) {
    FROSTED_DARK("frosted_dark", "Glass Dark"),
    LIGHT("light", "Clean Light"),
    AMOLED("amoled", "AMOLED Dark"),
    NEON("neon", "Cyber Neon");

    companion object {
        fun fromId(id: String): AppThemeMode = values().firstOrNull { it.id == id } ?: FROSTED_DARK
    }
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalculatorRepository(application)

    private val _appMode = MutableStateFlow(AppMode.CALCULATOR)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _appTheme = MutableStateFlow(AppThemeMode.fromId(repository.getAppTheme()))
    val appTheme: StateFlow<AppThemeMode> = _appTheme.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(repository.isHapticEnabled())
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _isScientificMode = MutableStateFlow(repository.isScientificMode())
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(repository.isDegreeMode())
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _isInvMode = MutableStateFlow(false)
    val isInvMode: StateFlow<Boolean> = _isInvMode.asStateFlow()

    private val _isFloatingMiniOpen = MutableStateFlow(false)
    val isFloatingMiniOpen: StateFlow<Boolean> = _isFloatingMiniOpen.asStateFlow()

    private val _selectedConverter = MutableStateFlow(ConverterType.LENGTH)
    val selectedConverter: StateFlow<ConverterType> = _selectedConverter.asStateFlow()

    private val _isCatalogOpen = MutableStateFlow(false)
    val isCatalogOpen: StateFlow<Boolean> = _isCatalogOpen.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    private val _isEvaluated = MutableStateFlow(false)
    val isEvaluated: StateFlow<Boolean> = _isEvaluated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val historyList: StateFlow<List<CalculationHistory>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customBackgroundPath = MutableStateFlow(repository.getBackgroundPath())
    val customBackgroundPath: StateFlow<String?> = _customBackgroundPath.asStateFlow()

    private val _blurRadius = MutableStateFlow(repository.getBlurRadius())
    val blurRadius: StateFlow<Float> = _blurRadius.asStateFlow()

    private val _overlayDarkness = MutableStateFlow(repository.getOverlayDarkness())
    val overlayDarkness: StateFlow<Float> = _overlayDarkness.asStateFlow()

    private val _isHistoryOpen = MutableStateFlow(false)
    val isHistoryOpen: StateFlow<Boolean> = _isHistoryOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    fun onDigit(digit: String) {
        if (_isEvaluated.value) {
            _expression.value = digit
            _isEvaluated.value = false
            _result.value = null
        } else {
            if (_expression.value == "0") {
                _expression.value = digit
            } else {
                _expression.value += digit
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onOperator(op: String) {
        val displayOp = when (op) {
            "+" -> "+"
            "-" -> "−"
            "*" -> "×"
            "/" -> "÷"
            else -> op
        }

        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = res + " " + displayOp + " "
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isEmpty()) {
                if (displayOp == "−") {
                    _expression.value = "-"
                }
                return
            }

            val lastChar = expr.last()
            if (lastChar in listOf('+', '−', '×', '÷')) {
                // Replace the operator
                _expression.value = expr.dropLast(1).trimEnd() + " " + displayOp + " "
            } else {
                _expression.value = expr + " " + displayOp + " "
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onDecimal() {
        if (_isEvaluated.value) {
            _expression.value = "0."
            _isEvaluated.value = false
            _result.value = null
            updatePreview()
            return
        }

        val expr = _expression.value
        val lastToken = expr.split(" ").lastOrNull() ?: ""
        if (!lastToken.contains(".")) {
            if (lastToken.isEmpty() || lastToken.endsWith("+") || lastToken.endsWith("−") ||
                lastToken.endsWith("×") || lastToken.endsWith("÷")
            ) {
                _expression.value = expr + "0."
            } else {
                _expression.value = expr + "."
            }
        }
        updatePreview()
    }

    fun onParenthesis() {
        if (_isEvaluated.value) {
            _expression.value = "("
            _isEvaluated.value = false
            _result.value = null
            updatePreview()
            return
        }

        val expr = _expression.value
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }

        val lastChar = expr.trimEnd().lastOrNull()
        if (openCount > closeCount && lastChar != null && (lastChar.isDigit() || lastChar == ')')) {
            _expression.value = expr + ")"
        } else {
            if (lastChar != null && (lastChar.isDigit() || lastChar == ')')) {
                _expression.value = expr + " × ("
            } else {
                _expression.value = expr + "("
            }
        }
        updatePreview()
    }

    fun onPercentage() {
        val expr = _expression.value.trimEnd()
        if (expr.isEmpty()) return
        val lastChar = expr.last()
        if (lastChar.isDigit()) {
            _expression.value = "$expr%"
            updatePreview()
        }
    }

    fun onToggleSign() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        // If evaluated, toggle the result
        if (_isEvaluated.value && _result.value != null) {
            val res = _result.value!!.replace(",", "")
            val toggled = if (res.startsWith("-")) res.drop(1) else "-$res"
            _expression.value = toggled
            _result.value = toggled
            return
        }

        val tokens = expr.split(" ").toMutableList()
        if (tokens.isNotEmpty()) {
            val last = tokens.last()
            if (last.isNotEmpty()) {
                tokens[tokens.size - 1] = if (last.startsWith("-")) {
                    last.drop(1)
                } else {
                    "-$last"
                }
                _expression.value = tokens.joinToString(" ")
                updatePreview()
            }
        }
    }

    fun onBackspace() {
        if (_isEvaluated.value) {
            _isEvaluated.value = false
            _result.value = null
            return
        }

        val expr = _expression.value
        if (expr.isNotEmpty()) {
            val specialSuffixes = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "asin(", "acos(", "atan(",
                "sqrt(", "cbrt(", "log(", "ln(", "abs(",
                "sin(", "cos(", "tan(", "1 ÷ (", " ^ "
            )
            val matchedSuffix = specialSuffixes.firstOrNull { expr.endsWith(it) }
            if (matchedSuffix != null) {
                _expression.value = expr.dropLast(matchedSuffix.length)
            } else if (expr.endsWith(" ")) {
                _expression.value = expr.dropLast(2)
            } else {
                _expression.value = expr.dropLast(1)
            }
            updatePreview()
        }
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = null
        _result.value = null
        _errorMessage.value = null
        _isEvaluated.value = false
    }

    fun onCalculate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        when (val eval = CalculatorEvaluator.evaluate(expr, _isDegreeMode.value)) {
            is EvalResult.Success -> {
                _result.value = eval.value
                _isEvaluated.value = true
                _previewResult.value = null
                _errorMessage.value = null

                // Save calculation to Room Database
                viewModelScope.launch {
                    repository.insertHistory(expr, eval.value)
                }
            }
            is EvalResult.Error -> {
                _errorMessage.value = eval.message
                _previewResult.value = null
            }
        }
    }

    private fun updatePreview() {
        val expr = _expression.value.trim()
        _previewResult.value = CalculatorEvaluator.preview(expr, _isDegreeMode.value)
    }

    fun toggleScientificMode() {
        val next = !_isScientificMode.value
        _isScientificMode.value = next
        repository.setScientificMode(next)
    }

    fun toggleDegreeMode() {
        val next = !_isDegreeMode.value
        _isDegreeMode.value = next
        repository.setDegreeMode(next)
        updatePreview()
    }

    fun toggleInvMode() {
        _isInvMode.value = !_isInvMode.value
    }

    fun onScientificFunction(funcName: String) {
        val func = if (funcName.endsWith("(")) funcName else "$funcName("
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: ""
            _expression.value = if (res.isNotEmpty()) "$func$res)" else func
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            val lastChar = expr.lastOrNull()
            if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
                _expression.value = "$expr × $func"
            } else {
                _expression.value = expr + func
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onConstant(constant: String) {
        if (_isEvaluated.value) {
            _expression.value = constant
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            val lastChar = expr.lastOrNull()
            if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
                _expression.value = "$expr × $constant"
            } else {
                _expression.value = expr + constant
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onPower() {
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = "$res ^ "
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isNotEmpty()) {
                _expression.value = "$expr ^ "
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onSquare() {
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = "$res²"
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isNotEmpty()) {
                _expression.value = "$expr²"
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onCube() {
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = "$res³"
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isNotEmpty()) {
                _expression.value = "$expr³"
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onFactorial() {
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = "$res!"
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isNotEmpty()) {
                _expression.value = "$expr!"
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onReciprocal() {
        if (_isEvaluated.value) {
            val res = _result.value?.replace(",", "") ?: _expression.value
            _expression.value = "1 ÷ ($res)"
            _isEvaluated.value = false
            _result.value = null
        } else {
            val expr = _expression.value.trimEnd()
            if (expr.isEmpty() || expr.endsWith("+") || expr.endsWith("−") || expr.endsWith("×") || expr.endsWith("÷")) {
                _expression.value = "$expr 1 ÷ ("
            } else {
                _expression.value = "1 ÷ ($expr)"
            }
        }
        _errorMessage.value = null
        updatePreview()
    }

    fun onUseHistory(item: CalculationHistory) {
        _expression.value = item.result.replace(",", "")
        _result.value = item.result
        _isEvaluated.value = true
        _previewResult.value = null
        _errorMessage.value = null
        _isHistoryOpen.value = false
    }

    fun onDeleteHistory(item: CalculationHistory) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun onSetCustomBackground(uri: Uri) {
        viewModelScope.launch {
            val path = repository.saveCustomBackground(uri)
            if (path != null) {
                _customBackgroundPath.value = path
            }
        }
    }

    fun onResetBackground() {
        repository.resetBackgroundToDefault()
        _customBackgroundPath.value = null
    }

    fun onUpdateBlur(blur: Float) {
        _blurRadius.value = blur
        repository.setBlurRadius(blur)
    }

    fun onUpdateDarkness(darkness: Float) {
        _overlayDarkness.value = darkness
        repository.setOverlayDarkness(darkness)
    }

    fun setHistoryOpen(open: Boolean) {
        _isHistoryOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun selectConverter(type: ConverterType) {
        _selectedConverter.value = type
        _appMode.value = AppMode.CONVERTER
        _isCatalogOpen.value = false
    }

    fun setCatalogOpen(open: Boolean) {
        _isCatalogOpen.value = open
    }

    fun setAppTheme(theme: AppThemeMode) {
        _appTheme.value = theme
        repository.setAppTheme(theme.id)
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        repository.setHapticEnabled(enabled)
    }

    fun setFloatingMiniOpen(open: Boolean) {
        _isFloatingMiniOpen.value = open
    }

    fun onPasteExpression(pastedText: String) {
        val sanitized = pastedText.trim()
            .replace(",", "")
            .replace(" ", "")
            .replace("x", "*")
            .replace("X", "*")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
        if (sanitized.isNotEmpty()) {
            if (_isEvaluated.value) {
                _expression.value = sanitized
                _isEvaluated.value = false
                _result.value = null
            } else {
                _expression.value += sanitized
            }
            _errorMessage.value = null
            updatePreview()
        }
    }
}
