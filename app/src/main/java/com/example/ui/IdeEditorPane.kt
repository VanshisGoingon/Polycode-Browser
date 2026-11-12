package com.example.ui

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.collab.CodeComment
import com.example.collab.WorkspaceUser
import com.example.runner.TerminalLine
import com.example.runner.TerminalLineType
import com.example.syntax.SupportedLanguage
import com.example.syntax.SyntaxHighlighter
import com.example.ui.theme.CodeGutterStyle
import com.example.ui.theme.CodeTextStyle
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TerminalTextStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.PolyFile
import kotlinx.coroutines.launch

enum class SplitRatioMode(val weightEditor: Float, val label: String) {
    BALANCED(0.50f, "50:50"),
    EDITOR_FOCUSED(0.70f, "70:30"),
    CONSOLE_FOCUSED(0.30f, "30:70"),
    MAX_EDITOR(0.92f, "Max Editor"),
    MAX_CONSOLE(0.08f, "Max Console")
}

enum class LogFilterType {
    ALL,
    STDOUT,
    STDERR,
    SYSTEM
}

@Composable
fun IdeEditorPane(
    files: List<PolyFile>,
    activeFileId: String,
    onSelectFile: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onExecuteCode: (stdinInput: String?) -> Unit,
    onClearTerminal: () -> Unit,
    onTogglePreview: () -> Unit,
    onOpenAiWithCode: () -> Unit,
    terminalLines: List<TerminalLine>,
    lastExecutionTimeMs: Long,
    isRunning: Boolean,
    showPreview: Boolean,
    previewHtml: String?,
    activeUsers: List<WorkspaceUser>,
    comments: List<CodeComment>,
    onAddCommentOnLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFile = files.firstOrNull { it.id == activeFileId } ?: files.first()

    var textValue by remember(activeFile.id, activeFile.content) {
        mutableStateOf(TextFieldValue(activeFile.content, TextRange(activeFile.content.length)))
    }

    var splitMode by remember { mutableStateOf(SplitRatioMode.BALANCED) }
    var outputTab by remember { mutableIntStateOf(if (showPreview) 1 else 0) }

    // Console Filtering & Search
    var selectedFilter by remember { mutableStateOf(LogFilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchRow by remember { mutableStateOf(false) }

    // Stdin interactive prompt
    var stdinInput by remember { mutableStateOf("") }
    var showStdinPrompt by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val terminalListState = rememberLazyListState()

    val codeLines = textValue.text.lines()

    // Auto-scroll terminal to bottom when new logs arrive
    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            terminalListState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    // Filtered logs
    val filteredLogs = remember(terminalLines, selectedFilter, searchQuery) {
        terminalLines.filter { line ->
            val matchesType = when (selectedFilter) {
                LogFilterType.ALL -> true
                LogFilterType.STDOUT -> line.type == TerminalLineType.STDOUT
                LogFilterType.STDERR -> line.type == TerminalLineType.STDERR
                LogFilterType.SYSTEM -> line.type == TerminalLineType.SYSTEM || line.type == TerminalLineType.SUCCESS || line.type == TerminalLineType.STDIN
            }
            val matchesQuery = if (searchQuery.isBlank()) true else line.text.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    val stdoutCount = terminalLines.count { it.type == TerminalLineType.STDOUT }
    val stderrCount = terminalLines.count { it.type == TerminalLineType.STDERR }

    // Animate editor & console pane weights
    val editorWeight by animateFloatAsState(
        targetValue = splitMode.weightEditor,
        label = "editorWeight"
    )
    val consoleWeight = 1f - editorWeight

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // TOP FILE TABS STRIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            files.forEach { file ->
                val isSelected = file.id == activeFileId
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonCyan.copy(alpha = 0.6f) else DarkBorder
                    ),
                    modifier = Modifier
                        .clickable { onSelectFile(file.id) }
                        .testTag("tab_${file.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconColor = when (file.language) {
                            SupportedLanguage.REACT_JSX -> NeonCyan
                            SupportedLanguage.PYTHON -> WarmAmber
                            SupportedLanguage.JAVA -> Color(0xFFE53935)
                            SupportedLanguage.NODEJS -> EmeraldGreen
                            SupportedLanguage.HTML -> Color(0xFFFF5722)
                            SupportedLanguage.CSS -> Color(0xFF2196F3)
                            else -> TextMuted
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name,
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextMuted
                        )

                        // Peer presence marker
                        val peersOnFile = activeUsers.filter { it.activeFileName == file.name && it.id != "u1" }
                        if (peersOnFile.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            peersOnFile.forEach { peer ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(peer.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = peer.name.take(1),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACTION COMMAND TOOLBAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // RUN BUTTON
                Button(
                    onClick = { onExecuteCode(if (stdinInput.isNotBlank()) stdinInput else null) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) WarmAmber else EmeraldGreen,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("run_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Code",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRunning) "Running..." else "RUN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Toggle Stdin Input Box
                IconButton(
                    onClick = { showStdinPrompt = !showStdinPrompt },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_stdin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Input,
                        contentDescription = "Simulate Stdin Input",
                        tint = if (showStdinPrompt) WarmAmber else TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Toggle Web Sandbox Preview
                if (activeFile.language == SupportedLanguage.REACT_JSX ||
                    activeFile.language == SupportedLanguage.HTML ||
                    activeFile.language == SupportedLanguage.CSS
                ) {
                    IconButton(
                        onClick = {
                            onTogglePreview()
                            outputTab = 1
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_preview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Live Sandbox Preview",
                            tint = if (showPreview) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // AI Assist on Current Code
                IconButton(
                    onClick = onOpenAiWithCode,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("ai_assist_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask AI to Audit",
                        tint = VividViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy Code
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(textValue.text)) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("copy_code_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Source Code",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Split preset control buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Preset 50:50
                IconButton(
                    onClick = { splitMode = SplitRatioMode.BALANCED },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        text = "50%",
                        fontSize = 10.sp,
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = if (splitMode == SplitRatioMode.BALANCED) FontWeight.Bold else FontWeight.Normal,
                        color = if (splitMode == SplitRatioMode.BALANCED) NeonCyan else TextMuted
                    )
                }
                // Preset 70:30 (Focus Code)
                IconButton(
                    onClick = { splitMode = SplitRatioMode.EDITOR_FOCUSED },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        text = "70%",
                        fontSize = 10.sp,
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = if (splitMode == SplitRatioMode.EDITOR_FOCUSED) FontWeight.Bold else FontWeight.Normal,
                        color = if (splitMode == SplitRatioMode.EDITOR_FOCUSED) NeonCyan else TextMuted
                    )
                }
                // Maximize Editor / Maximize Console toggle
                IconButton(
                    onClick = {
                        splitMode = if (splitMode == SplitRatioMode.MAX_EDITOR) SplitRatioMode.BALANCED else SplitRatioMode.MAX_EDITOR
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (splitMode == SplitRatioMode.MAX_EDITOR) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Maximize Editor",
                        tint = if (splitMode == SplitRatioMode.MAX_EDITOR) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // STDIN INPUT PROMPT ROW (If opened)
        AnimatedVisibility(visible = showStdinPrompt) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, WarmAmber.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STDIN >",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmAmber
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = stdinInput,
                    onValueChange = { stdinInput = it },
                    placeholder = { Text("Standard input stream for Python/Java/Node...", fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("stdin_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = WarmAmber,
                        unfocusedBorderColor = DarkBorder,
                        cursorColor = WarmAmber
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        onExecuteCode(stdinInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmAmber, contentColor = DarkBackground),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Feed & Run", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SPLIT-PANE CONTAINER (UPPER PANE: CODE EDITOR, LOWER PANE: CONSOLE WINDOW)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // UPPER PANE: CODE EDITOR
            if (splitMode != SplitRatioMode.MAX_CONSOLE) {
                Box(
                    modifier = Modifier
                        .weight(editorWeight)
                        .fillMaxWidth()
                        .background(DarkBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // Line Numbers Gutter with Comments Indicator
                        Column(
                            modifier = Modifier
                                .background(DarkSurface)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (index in codeLines.indices) {
                                val lineNum = index + 1
                                val commentsOnLine = comments.filter {
                                    it.fileId == activeFile.name && it.lineNumber == lineNum
                                }
                                val peerOnLine = activeUsers.firstOrNull {
                                    it.activeFileName == activeFile.name && it.cursorLine == lineNum && it.id != "u1"
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .height(19.dp)
                                        .clickable { onAddCommentOnLine(lineNum) }
                                ) {
                                    if (peerOnLine != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(peerOnLine.color)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    if (commentsOnLine.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(WarmAmber)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = "$lineNum",
                                        style = CodeGutterStyle
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Editable Code Input
                        BasicTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                textValue = newValue
                                onUpdateContent(newValue.text)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                                .testTag("code_editor_field"),
                            textStyle = CodeTextStyle.copy(color = TextPrimary),
                            cursorBrush = SolidColor(NeonCyan),
                            visualTransformation = {
                                val highlighted = SyntaxHighlighter.highlight(it.text, activeFile.language)
                                androidx.compose.ui.text.input.TransformedText(
                                    text = highlighted,
                                    offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
                                )
                            }
                        )
                    }
                }
            }

            // INTERACTIVE RESIZABLE SPLIT DIVIDER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(DarkSurfaceVariant)
                    .border(0.5.dp, DarkBorder)
                    .clickable {
                        // Cycle split modes on tap
                        splitMode = when (splitMode) {
                            SplitRatioMode.BALANCED -> SplitRatioMode.CONSOLE_FOCUSED
                            SplitRatioMode.CONSOLE_FOCUSED -> SplitRatioMode.EDITOR_FOCUSED
                            SplitRatioMode.EDITOR_FOCUSED -> SplitRatioMode.BALANCED
                            SplitRatioMode.MAX_EDITOR -> SplitRatioMode.BALANCED
                            SplitRatioMode.MAX_CONSOLE -> SplitRatioMode.BALANCED
                        }
                    }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NeonCyan.copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPLIT: ${splitMode.label.uppercase()}",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tap to Toggle View Ratio",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VerticalSplit,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // LOWER PANE: INTEGRATED CONSOLE WINDOW & LIVE SANDBOX
            if (splitMode != SplitRatioMode.MAX_EDITOR) {
                Column(
                    modifier = Modifier
                        .weight(consoleWeight)
                        .fillMaxWidth()
                        .background(DarkSurface)
                ) {
                    // CONSOLE HEADER & TABS STRIP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = outputTab,
                            containerColor = Color.Transparent,
                            contentColor = NeonCyan,
                            edgePadding = 0.dp,
                            divider = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            Tab(
                                selected = outputTab == 0,
                                onClick = { outputTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Console Logs (${terminalLines.size})", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily)
                                    }
                                }
                            )
                            Tab(
                                selected = outputTab == 1,
                                onClick = { outputTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Live Web Sandbox", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily)
                                    }
                                }
                            )
                        }

                        // Right Console Action Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Search toggle
                            IconButton(
                                onClick = { showSearchRow = !showSearchRow },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Console",
                                    tint = if (showSearchRow) NeonCyan else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            // Clear
                            IconButton(
                                onClick = onClearTerminal,
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("clear_terminal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ClearAll,
                                    contentDescription = "Clear Terminal",
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            // Maximize Console
                            IconButton(
                                onClick = {
                                    splitMode = if (splitMode == SplitRatioMode.MAX_CONSOLE) SplitRatioMode.BALANCED else SplitRatioMode.MAX_CONSOLE
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (splitMode == SplitRatioMode.MAX_CONSOLE) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Maximize Console",
                                    tint = if (splitMode == SplitRatioMode.MAX_CONSOLE) NeonCyan else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (outputTab == 0) {
                        // CONSOLE SUB-HEADER: STREAM FILTER CHIPS & ACTIVE RUNNER INDICATOR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Active Language Runner Pill
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isRunning) WarmAmber else EmeraldGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${activeFile.language.displayName} Runner",
                                        fontFamily = RobotoMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            // Filter All
                            FilterChip(
                                selected = selectedFilter == LogFilterType.ALL,
                                onClick = { selectedFilter = LogFilterType.ALL },
                                label = { Text("All", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkSurfaceVariant,
                                    selectedLabelColor = NeonCyan
                                )
                            )

                            // Filter Stdout
                            FilterChip(
                                selected = selectedFilter == LogFilterType.STDOUT,
                                onClick = { selectedFilter = LogFilterType.STDOUT },
                                label = { Text("Stdout ($stdoutCount)", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkSurfaceVariant,
                                    selectedLabelColor = EmeraldGreen
                                )
                            )

                            // Filter Stderr
                            FilterChip(
                                selected = selectedFilter == LogFilterType.STDERR,
                                onClick = { selectedFilter = LogFilterType.STDERR },
                                label = { Text("Stderr ($stderrCount)", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2A1515),
                                    selectedLabelColor = Color(0xFFEF4444)
                                )
                            )

                            // Filter System
                            FilterChip(
                                selected = selectedFilter == LogFilterType.SYSTEM,
                                onClick = { selectedFilter = LogFilterType.SYSTEM },
                                label = { Text("System", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkSurfaceVariant,
                                    selectedLabelColor = NeonCyan
                                )
                            )
                        }

                        // Search in logs input row (if opened)
                        AnimatedVisibility(visible = showSearchRow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Filter console output text...", fontSize = 10.sp, color = TextMuted) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkBackground,
                                        unfocusedContainerColor = DarkBackground,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = DarkBorder
                                    )
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        // INTEGRATED CONSOLE LOGS LIST
                        LazyColumn(
                            state = terminalListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(DarkBackground)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (filteredLogs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No logs match '$searchQuery'" else "Console ready. Click RUN to execute ${activeFile.language.displayName}.",
                                            fontSize = 11.sp,
                                            fontFamily = RobotoMonoFontFamily,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            items(filteredLogs) { line ->
                                val (tagText, tagColor, textColor, bgColor) = when (line.type) {
                                    TerminalLineType.STDERR -> Quadruple(
                                        "STDERR",
                                        Color(0xFFEF4444),
                                        Color(0xFFFCA5A5),
                                        Color(0xFF261010)
                                    )
                                    TerminalLineType.STDOUT -> Quadruple(
                                        "STDOUT",
                                        EmeraldGreen,
                                        TextPrimary,
                                        Color.Transparent
                                    )
                                    TerminalLineType.SYSTEM -> Quadruple(
                                        "SYSTEM",
                                        NeonCyan,
                                        NeonCyan.copy(alpha = 0.9f),
                                        Color.Transparent
                                    )
                                    TerminalLineType.SUCCESS -> Quadruple(
                                        "SUCCESS",
                                        Color(0xFF22C55E),
                                        Color(0xFF86EFAC),
                                        Color(0xFF0F2615)
                                    )
                                    TerminalLineType.STDIN -> Quadruple(
                                        "STDIN",
                                        WarmAmber,
                                        WarmAmber,
                                        Color(0xFF291E0A)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor, RoundedCornerShape(2.dp))
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = line.timestamp,
                                        style = TerminalTextStyle.copy(
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        ),
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(tagColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = tagText,
                                            fontFamily = RobotoMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = tagColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = line.text.removePrefix("[$tagText]").trim(),
                                        style = TerminalTextStyle.copy(
                                            fontSize = 11.5.sp,
                                            color = textColor
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        // LIVE WEB SANDBOX TAB
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (previewHtml != null) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            loadDataWithBaseURL(null, previewHtml, "text/html", "UTF-8", null)
                                        }
                                    },
                                    update = { webView ->
                                        webView.loadDataWithBaseURL(null, previewHtml, "text/html", "UTF-8", null)
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Run HTML or React JSX code to generate live preview.",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // MINIMALIST STATUS BAR FOOTER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activeFile.language.displayName,
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${codeLines.size} lines",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (lastExecutionTimeMs > 0) {
                    Text(
                        text = "Last Run: ${lastExecutionTimeMs}ms",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 10.sp,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = "${activeUsers.size} peers",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 10.sp,
                    color = NeonCyan
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
