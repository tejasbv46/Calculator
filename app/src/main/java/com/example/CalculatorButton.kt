package com.example

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class CalculatorButtonAction {
    data class Number(val number: String) : CalculatorButtonAction()
    object Decimal : CalculatorButtonAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorButtonAction()
    object Clear : CalculatorButtonAction()
    object AllClear : CalculatorButtonAction()
    object Backspace : CalculatorButtonAction()
    object Equals : CalculatorButtonAction()
    object ToggleSign : CalculatorButtonAction()
    object Percent : CalculatorButtonAction()
    
    // Scientific Operations
    data class ScientificFunc(val func: String) : CalculatorButtonAction() // "sin", "cos", "tan", "log", "ln", "sqrt", "1/x", "x^2", "x!", "^"
    data class Constant(val value: String) : CalculatorButtonAction() // "π", "e"
    object OpenParenthesis : CalculatorButtonAction()
    object CloseParenthesis : CalculatorButtonAction()
    
    // Memory Buttons
    object MemoryClear : CalculatorButtonAction()
    object MemoryRecall : CalculatorButtonAction()
    object MemoryAdd : CalculatorButtonAction()
    object MemorySubtract : CalculatorButtonAction()
}

@Composable
fun CalculatorButton(
    text: String,
    action: CalculatorButtonAction,
    onClick: (CalculatorButtonAction) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = 24.sp,
    aspectRatio: Float = 1f
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Subtle scale feedback on tap
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        label = "button_scale"
    )

    // Ensure we have a descriptive tag for UI testing matching the design guidelines
    val tag = "btn_" + text.lowercase()
        .replace("+/-", "sign")
        .replace("×", "mul")
        .replace("÷", "div")
        .replace("+", "add")
        .replace("-", "sub")
        .replace("=", "equals")
        .replace("(", "paren_open")
        .replace(")", "paren_close")
        .replace(" ", "_")

    Surface(
        modifier = modifier
            .scale(scale)
            .testTag(tag),
        shape = RoundedCornerShape(32.dp), // modern squircle rounded shape
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (isPressed) 8.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current
                ) {
                    // Trigger haptic feedback
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick(action)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}
