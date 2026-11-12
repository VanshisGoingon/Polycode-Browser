package com.example.collab

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PeerUserAlex
import com.example.ui.theme.PeerUserDev
import com.example.ui.theme.PeerUserMaya
import com.example.ui.theme.PeerUserYou
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class WorkspaceUser(
    val id: String,
    val name: String,
    val role: String,
    val color: Color,
    val isOnline: Boolean = true,
    val activeFileName: String = "App.jsx",
    val cursorLine: Int = 1,
    val cursorCol: Int = 1,
    val statusMessage: String = "Editing"
)

data class CommentReply(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val authorColor: Color,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class CodeComment(
    val id: String = UUID.randomUUID().toString(),
    val fileId: String,
    val lineNumber: Int,
    val author: String,
    val authorColor: Color,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val isResolved: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

data class CollabActivity(
    val id: String = UUID.randomUUID().toString(),
    val userName: String,
    val userColor: Color,
    val action: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

object CollabDefaults {
    fun initialUsers(): List<WorkspaceUser> = listOf(
        WorkspaceUser(
            id = "u1",
            name = "Vansh (You)",
            role = "Lead Architect",
            color = PeerUserYou,
            isOnline = true,
            activeFileName = "App.jsx",
            cursorLine = 5,
            cursorCol = 14,
            statusMessage = "Editing App.jsx"
        ),
        WorkspaceUser(
            id = "u2",
            name = "Maya",
            role = "Frontend Engineer",
            color = PeerUserMaya,
            isOnline = true,
            activeFileName = "styles.css",
            cursorLine = 12,
            cursorCol = 8,
            statusMessage = "Styling layout"
        ),
        WorkspaceUser(
            id = "u3",
            name = "Alex",
            role = "Backend Engineer",
            color = PeerUserAlex,
            isOnline = true,
            activeFileName = "server.js",
            cursorLine = 18,
            cursorCol = 22,
            statusMessage = "Reviewing endpoints"
        ),
        WorkspaceUser(
            id = "u4",
            name = "Dev",
            role = "Code Reviewer",
            color = PeerUserDev,
            isOnline = true,
            activeFileName = "Main.java",
            cursorLine = 8,
            cursorCol = 4,
            statusMessage = "Auditing performance"
        )
    )

    fun initialComments(): List<CodeComment> = listOf(
        CodeComment(
            id = "c1",
            fileId = "App.jsx",
            lineNumber = 4,
            author = "Dev",
            authorColor = PeerUserDev,
            text = "Consider memoizing this component with React.memo for high-frequency renders.",
            isResolved = false,
            replies = listOf(
                CommentReply(
                    author = "Maya",
                    authorColor = PeerUserMaya,
                    text = "Good catch! Added to the next sprint."
                )
            )
        ),
        CodeComment(
            id = "c2",
            fileId = "styles.css",
            lineNumber = 8,
            author = "Alex",
            authorColor = PeerUserAlex,
            text = "Use CSS variables for theme tokens so dark mode toggles instantly.",
            isResolved = true
        )
    )

    fun initialActivities(): List<CollabActivity> = listOf(
        CollabActivity(
            userName = "Vansh",
            userColor = PeerUserYou,
            action = "Created workspace 'PolyCode Pro Project'"
        ),
        CollabActivity(
            userName = "Maya",
            userColor = PeerUserMaya,
            action = "Joined workspace session"
        ),
        CollabActivity(
            userName = "Alex",
            userColor = PeerUserAlex,
            action = "Updated server.js routing handlers"
        )
    )
}
