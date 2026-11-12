package com.example.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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

data class ChromeTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var url: String,
    val isIncognito: Boolean = false
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserPane(
    initialUrl: String = "https://developer.mozilla.org",
    onOpenOfflineGame: () -> Unit,
    onInspectSource: (String) -> Unit,
    onOpenAskVanshAi: () -> Unit,
    onOpenImportantNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Chrome Tabs
    val tabs = remember {
        mutableStateListOf(
            ChromeTab(title = "MDN Web Docs", url = initialUrl, isIncognito = false)
        )
    }
    var activeTabId by remember { mutableStateOf(tabs.first().id) }
    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    var currentUrl by remember { mutableStateOf(activeTab.url) }
    var inputUrl by remember { mutableStateOf(activeTab.url) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var isSecure by remember { mutableStateOf(true) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var isSiteBlocked by remember { mutableStateOf(false) }

    // Chrome Side Drawer State
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(true) }
    var isSafeSearchEnabled by remember { mutableStateOf(true) }

    // Sub-dialogs
    var showDownloadsDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRecentSearchesDialog by remember { mutableStateOf(false) }

    // History and Bookmarks store
    val bookmarks = remember {
        mutableStateListOf(
            "MDN Web Docs" to "https://developer.mozilla.org",
            "React 18" to "https://react.dev",
            "Python 3.12" to "https://docs.python.org/3/",
            "GitHub" to "https://github.com",
            "Google AI Studio" to "https://aistudio.google.com"
        )
    }

    val history = remember {
        mutableStateListOf(
            "Google" to "https://www.google.com",
            "MDN Web Docs" to "https://developer.mozilla.org",
            "React Docs" to "https://react.dev"
        )
    }

    val recentSearches = remember {
        mutableStateListOf(
            "React hooks lifecycle",
            "Python list comprehension vs map",
            "Java 21 virtual threads",
            "Node.js streams memory management"
        )
    }

    val focusManager = LocalFocusManager.current

    // Check if domain is blocked by SafeSearch
    fun checkIsBlocked(url: String): Boolean {
        if (!isSafeSearchEnabled) return false
        val lower = url.lowercase()
        val blockedPatterns = listOf(
            "xxx", "adult", "porn", "phishing", "malware", "warez", "torrent", "gambling", "casino"
        )
        return blockedPatterns.any { lower.contains(it) }
    }

    fun navigateTo(rawUrl: String) {
        val target = rawUrl.trim()
        if (target == "poly://bird" || target == "chrome://dino") {
            onOpenOfflineGame()
            return
        }

        // Check unauthorized sites
        if (checkIsBlocked(target)) {
            isSiteBlocked = true
            return
        } else {
            isSiteBlocked = false
        }

        val formattedUrl = when {
            target.startsWith("http://") || target.startsWith("https://") -> {
                if (isSafeSearchEnabled && target.contains("google.com/search") && !target.contains("safe=")) {
                    "$target&safe=active"
                } else target
            }
            target.contains(".") && !target.contains(" ") -> "https://$target"
            else -> {
                recentSearches.add(0, target)
                val safeParam = if (isSafeSearchEnabled) "&safe=active" else ""
                "https://www.google.com/search?q=" + java.net.URLEncoder.encode(target, "UTF-8") + safeParam
            }
        }

        inputUrl = formattedUrl
        currentUrl = formattedUrl
        activeTab.url = formattedUrl
        isSecure = formattedUrl.startsWith("https://")
        hasError = false

        if (!activeTab.isIncognito) {
            history.add(0, (activeTab.title.ifBlank { formattedUrl }) to formattedUrl)
        }

        webViewInstance?.loadUrl(formattedUrl)
        focusManager.clearFocus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) DarkBackground else Color(0xFFF1F3F4))
        ) {
            // CHROME TAB STRIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF1F1F1F) else Color(0xFFDFE1E5))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab.id == activeTabId
                    val tabBg = when {
                        isSelected && tab.isIncognito -> Color(0xFF282828)
                        isSelected && isDarkMode -> DarkSurface
                        isSelected && !isDarkMode -> Color(0xFFFFFFFF)
                        else -> Color.Transparent
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = tabBg),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .clickable {
                                activeTabId = tab.id
                                currentUrl = tab.url
                                inputUrl = tab.url
                                webViewInstance?.loadUrl(tab.url)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tab.isIncognito) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Incognito",
                                    tint = VividViolet,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = tab.title.take(14),
                                fontSize = 11.5.sp,
                                fontFamily = RobotoMonoFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                            )
                            if (tabs.size > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val idx = tabs.indexOf(tab)
                                        tabs.remove(tab)
                                        if (activeTabId == tab.id && tabs.isNotEmpty()) {
                                            val nextTab = tabs.getOrNull(idx) ?: tabs.last()
                                            activeTabId = nextTab.id
                                            currentUrl = nextTab.url
                                            inputUrl = nextTab.url
                                            webViewInstance?.loadUrl(nextTab.url)
                                        }
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close tab",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // New Tab Button
                IconButton(
                    onClick = {
                        val newTab = ChromeTab(title = "New Tab", url = "https://www.google.com")
                        tabs.add(newTab)
                        activeTabId = newTab.id
                        currentUrl = newTab.url
                        inputUrl = newTab.url
                        webViewInstance?.loadUrl(newTab.url)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab",
                        tint = if (isDarkMode) TextPrimary else Color(0xFF3C4043),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // CHROME OMNIBOX ADDRESS BAR & 3-LINE MENU
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) DarkSurface else Color.White)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = {
                        if (webViewInstance?.canGoBack() == true) webViewInstance?.goBack()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("browser_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) TextPrimary else Color(0xFF3C4043),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Forward Button
                IconButton(
                    onClick = {
                        if (webViewInstance?.canGoForward() == true) webViewInstance?.goForward()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("browser_forward_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (isDarkMode) TextPrimary else Color(0xFF3C4043),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Reload Button
                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("browser_reload_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = if (isDarkMode) TextPrimary else Color(0xFF3C4043),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Omnibox Input Field
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("browser_url_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 12.sp,
                        color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = "Security Status",
                            tint = if (isSecure) EmeraldGreen else WarmAmber,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    trailingIcon = {
                        // Quick BaradAi Search Icon inside Omnibox
                        IconButton(
                            onClick = onOpenAskVanshAi,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask BaradAi",
                                tint = VividViolet,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkMode) DarkSurfaceVariant else Color(0xFFF1F3F4),
                        unfocusedContainerColor = if (isDarkMode) DarkSurfaceVariant else Color(0xFFF1F3F4),
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = NeonCyan
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { navigateTo(inputUrl) })
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Inspect DOM / Source Button
                IconButton(
                    onClick = {
                        webViewInstance?.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { htmlJson ->
                            val unescaped = htmlJson?.removeSurrounding("\"")?.replace("\\n", "\n")?.replace("\\\"", "\"") ?: "<!-- No DOM -->"
                            onInspectSource(unescaped)
                        }
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("browser_inspect_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Inspect Page Source",
                        tint = NeonCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // 3-LINE HAMBURGER MENU BUTTON
                IconButton(
                    onClick = { isDrawerOpen = true },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("chrome_3_lines_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Chrome Menu (3 Lines)",
                        tint = if (isDarkMode) TextPrimary else Color(0xFF202124),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Loading Progress Bar
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    color = NeonCyan,
                    trackColor = if (isDarkMode) DarkSurfaceVariant else Color(0xFFE8EAED),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }

            // Quick Bookmark Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) DarkSurfaceVariant else Color(0xFFEDF2F7))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bookmarks.forEach { (label, url) ->
                    SuggestionChip(
                        onClick = { navigateTo(url) },
                        label = { Text(label, fontSize = 10.5.sp, color = if (isDarkMode) TextPrimary else Color(0xFF202124)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isDarkMode) DarkSurface else Color.White
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isDarkMode) DarkBorder else Color(0xFFDADCE0)
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }

                SuggestionChip(
                    onClick = onOpenOfflineGame,
                    label = { Text("poly://bird", fontSize = 10.5.sp, color = WarmAmber, fontFamily = RobotoMonoFontFamily) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isDarkMode) DarkSurface else Color.White
                    ),
                    modifier = Modifier.height(26.dp)
                )
            }

            // WEBVIEW OR RESTRICTED SITE OVERLAY
            Box(modifier = Modifier.weight(1f)) {
                if (isSiteBlocked) {
                    // SafeSearch Block Screen
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Site Restricted by SafeSearch",
                                fontFamily = RobotoMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Access to '$inputUrl' is restricted to maintain a secure and safe developer environment.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isSiteBlocked = false
                                    navigateTo("https://www.google.com")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                            ) {
                                Text("Back to Safety", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        url?.let {
                                            inputUrl = it
                                            isSecure = it.startsWith("https://")
                                        }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        url?.let {
                                            inputUrl = it
                                            activeTab.title = view?.title ?: "Web Page"
                                            activeTab.url = it
                                        }
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            isLoading = false
                                            hasError = true
                                        }
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                        if (newProgress >= 100) isLoading = false
                                    }
                                }

                                loadUrl(currentUrl)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            webViewInstance = webView
                        }
                    )
                }
            }
        }

        // SLIDING CHROME SIDE 3-LINE DRAWER MENU
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            ChromeSideMenu(
                isIncognito = activeTab.isIncognito,
                isDarkMode = isDarkMode,
                isSafeSearchEnabled = isSafeSearchEnabled,
                onToggleIncognito = {
                    val incognito = !activeTab.isIncognito
                    val incTab = ChromeTab(
                        title = if (incognito) "Incognito" else "New Tab",
                        url = "https://www.google.com",
                        isIncognito = incognito
                    )
                    tabs.add(incTab)
                    activeTabId = incTab.id
                    currentUrl = incTab.url
                    inputUrl = incTab.url
                    webViewInstance?.loadUrl(incTab.url)
                    isDrawerOpen = false
                },
                onToggleTheme = { isDarkMode = !isDarkMode },
                onToggleSafeSearch = { isSafeSearchEnabled = !isSafeSearchEnabled },
                onNewTab = {
                    val newTab = ChromeTab(title = "New Tab", url = "https://www.google.com")
                    tabs.add(newTab)
                    activeTabId = newTab.id
                    currentUrl = newTab.url
                    inputUrl = newTab.url
                    webViewInstance?.loadUrl(newTab.url)
                    isDrawerOpen = false
                },
                onOpenDownloads = {
                    showDownloadsDialog = true
                    isDrawerOpen = false
                },
                onOpenBookmarks = {
                    showBookmarksDialog = true
                    isDrawerOpen = false
                },
                onOpenHistory = {
                    showHistoryDialog = true
                    isDrawerOpen = false
                },
                onOpenRecentSearches = {
                    showRecentSearchesDialog = true
                    isDrawerOpen = false
                },
                onOpenAskVanshAi = {
                    isDrawerOpen = false
                    onOpenAskVanshAi()
                },
                onOpenImportantNotice = {
                    isDrawerOpen = false
                    onOpenImportantNotice()
                },
                onCloseDrawer = { isDrawerOpen = false }
            )
        }

        // MODAL DIALOGS
        if (showDownloadsDialog) {
            DownloadsDialog(onDismiss = { showDownloadsDialog = false })
        }

        if (showBookmarksDialog) {
            DialogListViewer(
                title = "BOOKMARKS",
                items = bookmarks,
                onSelectItem = { url ->
                    navigateTo(url)
                    showBookmarksDialog = false
                },
                onDismiss = { showBookmarksDialog = false }
            )
        }

        if (showHistoryDialog) {
            DialogListViewer(
                title = "BROWSING HISTORY",
                items = history,
                onSelectItem = { url ->
                    navigateTo(url)
                    showHistoryDialog = false
                },
                onDismiss = { showHistoryDialog = false }
            )
        }

        if (showRecentSearchesDialog) {
            DialogListViewer(
                title = "RECENT SEARCHES",
                items = recentSearches.map { it to it },
                onSelectItem = { query ->
                    navigateTo(query)
                    showRecentSearchesDialog = false
                },
                onDismiss = { showRecentSearchesDialog = false }
            )
        }
    }
}

@Composable
private fun DialogListViewer(
    title: String,
    items: List<Pair<String, String>>,
    onSelectItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontFamily = RobotoMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeonCyan)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items) { (label, value) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectItem(value) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(value, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
