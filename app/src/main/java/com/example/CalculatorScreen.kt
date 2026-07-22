package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
//github.com/tejasbv46/Calculator.gitmport androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showHistorySheet by remember { mutableStateOf(false)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showScientific = state.isScientific || isLandscape

    // Portrait + scientific stacks 4 scientific rows on top of 6 standard rows
    // (10 rows total) in the same column — that's the case that was overflowing
    // and clipping/overlapping. Everything below reacts to that specific case.
    val stackedScientific = showScientific && !isLandscape

    // Helper to copy text to clipboard
    val copyToClipboard = { text: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Only use TopAppBar in portrait. Landscape handles its own header to save space.
            if (!isLandscape) {
                TopAppBar(
                    // Handle horizontal safe areas (notches) and status bar height
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator App Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Calculator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleScientificMode() },
                            modifier = Modifier.testTag("btn_toggle_scientific")
                        ) {
                            Icon(
                                imageVector = if (state.isScientific) Icons.Filled.Science else Icons.Outlined.Science,
                                contentDescription = "Toggle Scientific Mode",
                                tint = if (state.isScientific) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("btn_show_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "View Calculation History"
                            )
                        }
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("btn_logout")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        // Use a Column for the main content to ensure vertical flow is predictable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) PaddingValues(0.dp) else innerPadding)
                .consumeWindowInsets(innerPadding)
                // Handle horizontal notches in landscape mode
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp)
        ) {
            // In landscape, we use a custom header that respects the status bar (clock/battery)
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator App Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Calculator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.size(36.dp).testTag("btn_show_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View Calculation History",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(36.dp).testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Balancing space for displays and buttons
            val compact = stackedScientific || isLandscape
            val standardButtonHeight = if (isLandscape) 32.dp else if (stackedScientific) 44.dp else 64.dp
            val scientificButtonHeight = if (isLandscape) 28.dp else if (stackedScientific) 32.dp else 40.dp
            val standardFontSize = if (isLandscape) 15.sp else if (compact) 16.sp else 22.sp
            val rowSpacing = if (isLandscape) 3.dp else if (compact) 4.dp else 8.dp

            // Displays Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { copyToClipboard(state.currentInput) },
                        onLongClick = { copyToClipboard(state.currentInput) }
                    )
                    .padding(top = if (isLandscape) 16.dp else if (compact) 2.dp else 8.dp, bottom = if (isLandscape) 8.dp else if (compact) 4.dp else 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Secondary Display: Expression Formula
                Text(
                    text = state.previousExpression,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (compact) 2.dp else 8.dp)
                        .testTag("previous_display")
                )

                // Error message banner
                AnimatedVisibility(
                    visible = state.isError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }

                // Primary Display: Output Result / Input (Scales dynamically)
                val primaryFontSize = when {
                    state.currentInput.length > 15 -> 20.sp
                    state.currentInput.length > 11 -> 28.sp
                    state.currentInput.length > 7 -> 36.sp
                    isLandscape -> 24.sp
                    stackedScientific -> 38.sp
                    else -> 56.sp
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.currentInput,
                        fontSize = primaryFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = primaryFontSize * 1.15f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("primary_display")
                    )

                    // Quick Clipboard copy button
                    IconButton(
                        onClick = { copyToClipboard(state.currentInput) },
                        modifier = Modifier.size(if (compact) 32.dp else 40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(if (compact) 14.dp else 18.dp)
                        )
                    }
                }
            }

            // Pushes the keypad to the bottom of the screen
            Spacer(modifier = Modifier.weight(1f))

            // Keypad Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                if (showScientific) {
                    if (isLandscape) {
                        // Landscape Mode Layout (Scientific on left, Standard on right side-by-side)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1.1f),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing)
                            ) {
                                ScientificButtons(viewModel, buttonHeight = scientificButtonHeight, rowSpacing = rowSpacing)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing)
                            ) {
                                StandardButtons(viewModel, buttonHeight = standardButtonHeight, fontSize = standardFontSize, rowSpacing = rowSpacing)
                            }
                        }
                    } else {
                        // Portrait Mode with Scientific on Top
                        ScientificButtons(viewModel, buttonHeight = scientificButtonHeight, rowSpacing = rowSpacing)
                        Spacer(modifier = Modifier.height(rowSpacing))
                        StandardButtons(viewModel, buttonHeight = standardButtonHeight, fontSize = standardFontSize, rowSpacing = rowSpacing)
                    }
                } else {
                    // Standard Portrait Keypad Only
                    StandardButtons(viewModel, buttonHeight = standardButtonHeight, fontSize = standardFontSize, rowSpacing = rowSpacing)
                }
            }

            // Safety bottom padding
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else if (compact) 8.dp else 16.dp))
        }
    }

    // Calculation History Bottom Sheet Drawer
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState,//controls the sheets
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("history_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (historyList.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All History", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryRow(
                                item = item,
                                onSelect = {
                                    viewModel.selectHistoryItem(item)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showHistorySheet = false
                                        }
                                    }
                                },
                                onDelete = { viewModel.deleteHistoryItem(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Scope-safe Row wrappers to ensure weight functions resolve inside RowScope.
// Uses a fixed .height() instead of .aspectRatio() — aspectRatio combined with
// weight() inside a Row that's nested inside another weight()'d, scrollable
// Column produced inconsistent/overlapping measurements. A fixed height has
// no such interaction: it's simply what it is, regardless of what's above,
// below, or around it.
@Composable
fun StandardRow(
    buttonHeight: Dp = 64.dp,
    content: @Composable RowScope.(Modifier) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val buttonModifier = Modifier
            .height(buttonHeight)
            .weight(1f)
        content(buttonModifier)
    }
}

@Composable
fun ScientificRow(
    buttonHeight: Dp = 40.dp,
    content: @Composable RowScope.(Modifier) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val buttonModifier = Modifier
            .height(buttonHeight)
            .weight(1f)
        content(buttonModifier)
    }
}

@Composable
fun StandardButtons(viewModel: CalculatorViewModel, buttonHeight: Dp = 64.dp, fontSize: androidx.compose.ui.unit.TextUnit = 24.sp, rowSpacing: Dp = 8.dp) {
    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        StandardRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "AC",
                action = CalculatorButtonAction.AllClear,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = modifier
            )
            CalculatorButton(
                text = "C",
                action = CalculatorButtonAction.Clear,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = modifier
            )
            CalculatorButton(
                text = "%",
                action = CalculatorButtonAction.Percent,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = modifier
            )
            CalculatorButton(
                text = "÷",
                action = CalculatorButtonAction.Operation(CalculatorOperation.DIVIDE),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier
            )
        }
        StandardRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "7",
                action = CalculatorButtonAction.Number("7"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "8",
                action = CalculatorButtonAction.Number("8"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "9",
                action = CalculatorButtonAction.Number("9"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "×",
                action = CalculatorButtonAction.Operation(CalculatorOperation.MULTIPLY),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier
            )
        }
        StandardRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "4",
                action = CalculatorButtonAction.Number("4"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "5",
                action = CalculatorButtonAction.Number("5"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "6",
                action = CalculatorButtonAction.Number("6"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "-",
                action = CalculatorButtonAction.Operation(CalculatorOperation.SUBTRACT),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier
            )
        }
        StandardRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "1",
                action = CalculatorButtonAction.Number("1"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "2",
                action = CalculatorButtonAction.Number("2"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "3",
                action = CalculatorButtonAction.Number("3"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "+",
                action = CalculatorButtonAction.Operation(CalculatorOperation.ADD),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier
            )
        }
        StandardRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "+/-",
                action = CalculatorButtonAction.ToggleSign,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "0",
                action = CalculatorButtonAction.Number("0"),
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = ".",
                action = CalculatorButtonAction.Decimal,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = modifier
            )
            CalculatorButton(
                text = "=",
                action = CalculatorButtonAction.Equals,
                fontSize = fontSize,
                onClick = { viewModel.onAction(it) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ScientificButtons(viewModel: CalculatorViewModel, buttonHeight: Dp = 40.dp, rowSpacing: Dp = 6.dp) {
    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        ScientificRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "(",
                action = CalculatorButtonAction.OpenParenthesis,
                onClick = { viewModel.onAction(it) },
                fontSize = 16.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = ")",
                action = CalculatorButtonAction.CloseParenthesis,
                onClick = { viewModel.onAction(it) },
                fontSize = 16.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "√",
                action = CalculatorButtonAction.ScientificFunc("sqrt"),
                onClick = { viewModel.onAction(it) },
                fontSize = 16.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "x^y",
                action = CalculatorButtonAction.ScientificFunc("^"),
                onClick = { viewModel.onAction(it) },
                fontSize = 16.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "Del",
                action = CalculatorButtonAction.Backspace,
                onClick = { viewModel.onAction(it) },
                fontSize = 16.sp,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = modifier
            )
        }
        ScientificRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "sin",
                action = CalculatorButtonAction.ScientificFunc("sin"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "cos",
                action = CalculatorButtonAction.ScientificFunc("cos"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "tan",
                action = CalculatorButtonAction.ScientificFunc("tan"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "ln",
                action = CalculatorButtonAction.ScientificFunc("ln"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "log",
                action = CalculatorButtonAction.ScientificFunc("log"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
        }
        ScientificRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "1/x",
                action = CalculatorButtonAction.ScientificFunc("1/x"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "x²",
                action = CalculatorButtonAction.ScientificFunc("x^2"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "x!",
                action = CalculatorButtonAction.ScientificFunc("x!"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "π",
                action = CalculatorButtonAction.Constant("π"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "e",
                action = CalculatorButtonAction.Constant("e"),
                onClick = { viewModel.onAction(it) },
                fontSize = 15.sp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = modifier
            )
        }
        ScientificRow(buttonHeight = buttonHeight) { modifier ->
            CalculatorButton(
                text = "MC",
                action = CalculatorButtonAction.MemoryClear,
                onClick = { viewModel.onAction(it) },
                fontSize = 14.sp,
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "MR",
                action = CalculatorButtonAction.MemoryRecall,
                onClick = { viewModel.onAction(it) },
                fontSize = 14.sp,
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "M+",
                action = CalculatorButtonAction.MemoryAdd,
                onClick = { viewModel.onAction(it) },
                fontSize = 14.sp,
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = modifier
            )
            CalculatorButton(
                text = "M-",
                action = CalculatorButtonAction.MemorySubtract,
                onClick = { viewModel.onAction(it) },
                fontSize = 14.sp,
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = modifier
            )
            Spacer(modifier = modifier) // Align nicely
        }
    }
}

@Composable
fun HistoryRow(
    item: HistoryEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expression,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "= ${item.result}",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}