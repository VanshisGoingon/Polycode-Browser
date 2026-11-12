package com.example.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class ChromeDrawerSection {
    MENU,
    DOWNLOADS,
    BOOKMARKS,
    HISTORY,
    RECENT_SEARCHES,
    IMPORTANT_NOTICE
}

@Composable
fun ChromeSideMenu(
    isIncognito: Boolean,
    isDarkMode: Boolean,
    isSafeSearchEnabled: Boolean,
    onToggleIncognito: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleSafeSearch: () -> Unit,
    onNewTab: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenRecentSearches: () -> Unit,
    onOpenAskVanshAi: () -> Unit,
    onOpenImportantNotice: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(if (isDarkMode) DarkSurface else Color(0xFFF8F9FA))
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        // Chrome Menu Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Chrome Colors Circle icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isIncognito) Color(0xFF3C4043) else NeonCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isDarkMode) DarkBackground else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isIncognito) "Chrome Incognito" else "PolyCode Chrome",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                    )
                    Text(
                        text = "Vansh Barad Edition",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("close_chrome_drawer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Menu",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = if (isDarkMode) DarkBorder else Color(0xFFE8EAED), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Menu Items List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // New Tab
            item {
                ChromeMenuItem(
                    icon = Icons.Default.Add,
                    title = "New Tab",
                    subtitle = "Open a fresh browsing tab",
                    isDarkMode = isDarkMode,
                    onClick = onNewTab
                )
            }

            // Incognito Tab Toggle
            item {
                ChromeMenuItem(
                    icon = Icons.Default.VisibilityOff,
                    title = if (isIncognito) "Exit Incognito" else "New Incognito Tab",
                    subtitle = "Browse privately without saving history",
                    iconColor = if (isIncognito) VividViolet else WarmAmber,
                    isDarkMode = isDarkMode,
                    onClick = onToggleIncognito
                )
            }

            // Bookmarks
            item {
                ChromeMenuItem(
                    icon = Icons.Default.Bookmark,
                    title = "Bookmarks",
                    subtitle = "Saved developer & documentation links",
                    iconColor = NeonCyan,
                    isDarkMode = isDarkMode,
                    onClick = onOpenBookmarks
                )
            }

            // Downloads
            item {
                ChromeMenuItem(
                    icon = Icons.Default.Download,
                    title = "Downloads",
                    subtitle = "View offline files & generated APKs",
                    iconColor = EmeraldGreen,
                    isDarkMode = isDarkMode,
                    onClick = onOpenDownloads
                )
            }

            // History
            item {
                ChromeMenuItem(
                    icon = Icons.Default.History,
                    title = "History",
                    subtitle = "Recent websites & domain records",
                    isDarkMode = isDarkMode,
                    onClick = onOpenHistory
                )
            }

            // Recent Searches
            item {
                ChromeMenuItem(
                    icon = Icons.Default.Search,
                    title = "Recent Search",
                    subtitle = "Previous queries & Gemini searches",
                    isDarkMode = isDarkMode,
                    onClick = onOpenRecentSearches
                )
            }

            // Ask Vansh AI / BaradAi
            item {
                ChromeMenuItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Ask Vansh AI (BaradAi)",
                    subtitle = "Google Gemini & AI Studio Powered",
                    iconColor = VividViolet,
                    badge = "AI",
                    isDarkMode = isDarkMode,
                    onClick = onOpenAskVanshAi
                )
            }

            // Important Notice
            item {
                ChromeMenuItem(
                    icon = Icons.Default.Info,
                    title = "Important Notice",
                    subtitle = "Public service & nonprofit mission statement",
                    iconColor = WarmAmber,
                    badge = "FREE",
                    isDarkMode = isDarkMode,
                    onClick = onOpenImportantNotice
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = if (isDarkMode) DarkBorder else Color(0xFFE8EAED), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // SETTINGS TOGGLES (Theme & SafeSearch)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dark / Light Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleTheme() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                        contentDescription = null,
                        tint = if (isDarkMode) NeonCyan else Color(0xFFF29900),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isDarkMode) "Dark Mode" else "Light Mode",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.size(36.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            // SafeSearch Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSafeSearch() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isSafeSearchEnabled) EmeraldGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SafeSearch & Protection",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                        )
                        Text(
                            text = if (isSafeSearchEnabled) "Block unauthorized sites" else "Standard filtering",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                Switch(
                    checked = isSafeSearchEnabled,
                    onCheckedChange = { onToggleSafeSearch() },
                    modifier = Modifier.size(36.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldGreen,
                        checkedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ChromeMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDarkMode: Boolean,
    iconColor: Color = TextPrimary,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDarkMode) iconColor else if (iconColor == TextPrimary) Color(0xFF3C4043) else iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) TextPrimary else Color(0xFF202124)
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VividViolet.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividViolet
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = TextMuted
                )
            }
        }
    }
}
