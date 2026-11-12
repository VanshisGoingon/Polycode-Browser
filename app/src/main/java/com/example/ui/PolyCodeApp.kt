package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiAssistantPane
import com.example.browser.BrowserPane
import com.example.browser.ImportantNoticeDialog
import com.example.browser.TemplateSelectorDialog
import com.example.collab.CollabHubPane
import com.example.game.PolyBirdGame
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.PolyCodeViewModel

@Composable
fun PolyCodeApp(
    viewModel: PolyCodeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showTemplateSelector by remember { mutableStateOf(false) }
    var showImportantNotice by remember { mutableStateOf(false) }

    // Handle back button: if in a sub-view, return to IDE
    BackHandler(enabled = uiState.activeTab != MainNavTab.IDE) {
        viewModel.switchTab(MainNavTab.IDE)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = DarkBackground,
        topBar = {
            // App Branding & Status Header with Templates and Info Shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PolyCode",
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "By Vansh",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = RobotoMonoFontFamily
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Template Selector Button
                    Button(
                        onClick = { showTemplateSelector = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("top_templates_btn")
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Templates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Important Notice Button
                    IconButton(
                        onClick = { showImportantNotice = true },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("top_notice_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Important Notice",
                            tint = WarmAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Minimalist Developer Navigation Bar
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp)
            ) {
                // IDE Tab
                NavigationBarItem(
                    selected = uiState.activeTab == MainNavTab.IDE,
                    onClick = { viewModel.switchTab(MainNavTab.IDE) },
                    icon = {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = "IDE Editor",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("IDE", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = DarkSurfaceVariant,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_ide")
                )

                // Chrome Browser Tab
                NavigationBarItem(
                    selected = uiState.activeTab == MainNavTab.BROWSER,
                    onClick = { viewModel.switchTab(MainNavTab.BROWSER) },
                    icon = {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Chrome Browser",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Chrome", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = DarkSurfaceVariant,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_browser")
                )

                // Collab Tab
                NavigationBarItem(
                    selected = uiState.activeTab == MainNavTab.COLLAB,
                    onClick = { viewModel.switchTab(MainNavTab.COLLAB) },
                    icon = {
                        BadgedBox(
                            badge = {
                                val unresolvedCount = uiState.comments.count { !it.isResolved }
                                if (unresolvedCount > 0) {
                                    Badge(
                                        containerColor = NeonCyan,
                                        contentColor = DarkBackground
                                    ) {
                                        Text("$unresolvedCount", fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Collab Workspace",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    label = { Text("Collab", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = DarkSurfaceVariant,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_collab")
                )

                // BaradAi Tab (Google Gemini)
                NavigationBarItem(
                    selected = uiState.activeTab == MainNavTab.AI,
                    onClick = { viewModel.switchTab(MainNavTab.AI) },
                    icon = {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "BaradAi Assistant",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("BaradAi", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VividViolet,
                        selectedTextColor = VividViolet,
                        indicatorColor = DarkSurfaceVariant,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_ai")
                )

                // Flappy Bird Game Tab
                NavigationBarItem(
                    selected = uiState.activeTab == MainNavTab.GAME,
                    onClick = { viewModel.switchTab(MainNavTab.GAME) },
                    icon = {
                        Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = "Poly Bird Game",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Bird", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmAmber,
                        selectedTextColor = WarmAmber,
                        indicatorColor = DarkSurfaceVariant,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_game")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                MainNavTab.IDE -> {
                    val activeFile = uiState.files.firstOrNull { it.id == uiState.activeFileId }
                        ?: uiState.files.first()
                    IdeEditorPane(
                        files = uiState.files,
                        activeFileId = uiState.activeFileId,
                        onSelectFile = { viewModel.selectFile(it) },
                        onUpdateContent = { viewModel.updateActiveFileContent(it) },
                        onExecuteCode = { stdin -> viewModel.executeActiveCode(stdin) },
                        onClearTerminal = { viewModel.clearTerminal() },
                        onTogglePreview = { viewModel.toggleWebPreview() },
                        onOpenAiWithCode = { viewModel.switchTab(MainNavTab.AI) },
                        terminalLines = uiState.terminalLines,
                        lastExecutionTimeMs = uiState.lastExecutionTimeMs,
                        isRunning = uiState.isRunning,
                        showPreview = uiState.showLiveWebPreview,
                        previewHtml = uiState.liveWebPreviewHtml,
                        activeUsers = uiState.activeUsers,
                        comments = uiState.comments,
                        onAddCommentOnLine = { line ->
                            viewModel.switchTab(MainNavTab.COLLAB)
                        }
                    )
                }

                MainNavTab.BROWSER -> {
                    BrowserPane(
                        initialUrl = uiState.browserInitialUrl,
                        onOpenOfflineGame = { viewModel.switchTab(MainNavTab.GAME) },
                        onInspectSource = { domSource ->
                            viewModel.updateActiveFileContent(domSource)
                            viewModel.switchTab(MainNavTab.IDE)
                        },
                        onOpenAskVanshAi = { viewModel.switchTab(MainNavTab.AI) },
                        onOpenImportantNotice = { showImportantNotice = true }
                    )
                }

                MainNavTab.COLLAB -> {
                    val currentFile = uiState.files.firstOrNull { it.id == uiState.activeFileId }
                    CollabHubPane(
                        activeUsers = uiState.activeUsers,
                        comments = uiState.comments,
                        activities = uiState.activities,
                        currentFileName = currentFile?.name ?: "App.jsx",
                        onAddComment = { line, text -> viewModel.addComment(line, text) },
                        onAddReply = { id, reply -> viewModel.addReply(id, reply) },
                        onToggleResolved = { id -> viewModel.toggleCommentResolved(id) },
                        onNavigateToLine = { line ->
                            viewModel.updateCursorPosition(line, 1)
                            viewModel.switchTab(MainNavTab.IDE)
                        }
                    )
                }

                MainNavTab.AI -> {
                    val activeFile = uiState.files.firstOrNull { it.id == uiState.activeFileId }
                        ?: uiState.files.first()
                    AiAssistantPane(
                        currentCode = activeFile.content,
                        currentLanguage = activeFile.language,
                        onApplyCodeToEditor = { snippet -> viewModel.applyCodeFromAi(snippet) },
                        onRunCodeDirectly = { snippet ->
                            viewModel.applyCodeFromAi(snippet)
                            viewModel.executeActiveCode()
                        }
                    )
                }

                MainNavTab.GAME -> {
                    PolyBirdGame(
                        audioSynthesizer = viewModel.audioSynthesizer,
                        onCloseGame = { viewModel.switchTab(MainNavTab.IDE) }
                    )
                }
            }

            // MODAL DIALOGS
            if (showTemplateSelector) {
                TemplateSelectorDialog(
                    onSelectTemplate = { template ->
                        viewModel.loadTemplate(template)
                    },
                    onDismiss = { showTemplateSelector = false }
                )
            }

            if (showImportantNotice) {
                ImportantNoticeDialog(
                    onDismiss = { showImportantNotice = false }
                )
            }
        }
    }
}
