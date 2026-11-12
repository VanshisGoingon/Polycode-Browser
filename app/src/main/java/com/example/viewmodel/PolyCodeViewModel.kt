package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSynthesizer
import com.example.collab.CodeComment
import com.example.collab.CollabActivity
import com.example.collab.CollabDefaults
import com.example.collab.CommentReply
import com.example.collab.WorkspaceUser
import com.example.runner.CodeRunnerEngine
import com.example.runner.ExecutionResult
import com.example.runner.TerminalLine
import com.example.runner.TerminalLineType
import com.example.syntax.SupportedLanguage
import com.example.ui.theme.PeerUserYou
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainNavTab {
    IDE,
    BROWSER,
    COLLAB,
    AI,
    GAME
}

data class PolyFile(
    val id: String,
    val name: String,
    val language: SupportedLanguage,
    val content: String
)

data class PolyCodeUiState(
    val activeTab: MainNavTab = MainNavTab.IDE,
    val files: List<PolyFile> = defaultFiles(),
    val activeFileId: String = "app_jsx",
    val terminalLines: List<TerminalLine> = listOf(
        TerminalLine("⚡ PolyCode by Vansh Workspace Initialized", TerminalLineType.SYSTEM),
        TerminalLine("Supported: HTML, CSS, JavaScript, React JSX, Node.js, Python, Java", TerminalLineType.STDOUT),
        TerminalLine("Ready for live editing & collaborative coding.", TerminalLineType.SUCCESS)
    ),
    val lastExecutionTimeMs: Long = 0,
    val isRunning: Boolean = false,
    val liveWebPreviewHtml: String? = null,
    val showLiveWebPreview: Boolean = false,
    val activeUsers: List<WorkspaceUser> = CollabDefaults.initialUsers(),
    val comments: List<CodeComment> = CollabDefaults.initialComments(),
    val activities: List<CollabActivity> = CollabDefaults.initialActivities(),
    val cursorLine: Int = 1,
    val cursorCol: Int = 1,
    val browserInitialUrl: String = "https://developer.mozilla.org"
)

private fun defaultFiles(): List<PolyFile> = listOf(
    PolyFile(
        id = "app_jsx",
        name = "App.jsx",
        language = SupportedLanguage.REACT_JSX,
        content = """
// PolyCode React 18 Workspace
function App() {
  const [likes, setLikes] = React.useState(42);
  const [todos, setTodos] = React.useState([
    "Build PolyCode by Vansh",
    "Real-time syntax highlighting",
    "Run Python & Java scripts",
    "Beat Poly Bird high score"
  ]);

  return (
    <div className="card">
      <h1 style={{color: '#00e5ff'}}>⚡ PolyCode IDE</h1>
      <p>Interactive React component live preview.</p>
      
      <button onClick={() => setLikes(likes + 1)}>
        ❤️ Star Project ({likes})
      </button>

      <h3>Developer Tasks:</h3>
      <ul>
        {todos.map((task, i) => (
          <li key={i}>{task}</li>
        ))}
      </ul>
    </div>
  );
}
""".trimIndent()
    ),
    PolyFile(
        id = "main_py",
        name = "main.py",
        language = SupportedLanguage.PYTHON,
        content = """
# PolyCode Python 3.12 Engine
import math

def calculate_fibonacci(n):
    fib = [0, 1]
    for i in range(2, n):
        fib.append(fib[-1] + fib[-2])
    return fib

print("--- PolyCode Algorithm Execution ---")
numbers = calculate_fibonacci(10)
print(f"Generated Fibonacci sequence: {numbers}")

radius = 5.0
area = math.pi * (radius ** 2)
print(f"Circle area with r={radius}: {area:.2f}")
print("Python execution benchmark: Success!")
""".trimIndent()
    ),
    PolyFile(
        id = "main_java",
        name = "Main.java",
        language = SupportedLanguage.JAVA,
        content = """
// PolyCode Java 21 Execution Environment
public class Main {
    public static void main(String[] args) {
        System.out.println("⚡ Welcome to PolyCode by Vansh (Java Runtime)");
        
        int[] scores = {98, 85, 92, 100, 78};
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        
        double average = (double) sum / scores.length;
        System.out.println("Processing student grades count: " + scores.length);
        System.out.println("Calculated Class Average: " + average);
        System.out.println("Java Sandbox Status: OK (0 errors)");
    }
}
""".trimIndent()
    ),
    PolyFile(
        id = "server_js",
        name = "server.js",
        language = SupportedLanguage.NODEJS,
        content = """
// PolyCode Node.js script runner
console.log("⚡ Starting PolyCode Microservice...");

const endpoints = [
  { path: "/api/health", method: "GET", status: 200 },
  { path: "/api/code/run", method: "POST", status: 200 },
  { path: "/api/collab/users", method: "GET", status: 200 }
];

endpoints.forEach(ep => {
  console.log("[ROUTER] Registered route: " + ep.method + " " + ep.path + " (200 OK)");
});

const memUsage = Math.round(18.4 + Math.random() * 2);
console.log("Node.js Memory usage: " + memUsage + "MB heap used");
console.log("Server listening on port 3000!");
""".trimIndent()
    ),
    PolyFile(
        id = "index_html",
        name = "index.html",
        language = SupportedLanguage.HTML,
        content = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>PolyCode Web</title>
  <style>
    body {
      background: #0f1117;
      color: #f1f5f9;
      font-family: sans-serif;
      padding: 20px;
    }
    .neon-text {
      color: #00e5ff;
      text-shadow: 0 0 10px rgba(0,229,255,0.4);
    }
  </style>
</head>
<body>
  <h1 class="neon-text">🌐 PolyCode Web Showcase</h1>
  <p>Multi-domain developer workspace crafted by Vansh.</p>
</body>
</html>
""".trimIndent()
    ),
    PolyFile(
        id = "styles_css",
        name = "styles.css",
        language = SupportedLanguage.CSS,
        content = """
/* PolyCode Theme Styles */
:root {
  --primary-color: #00e5ff;
  --bg-color: #0f1117;
  --surface-color: #1a1d27;
  --border-color: #2a2e3d;
}

body {
  margin: 0;
  padding: 16px;
  background-color: var(--bg-color);
  color: #f1f5f9;
  font-family: monospace;
}

.card {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
}
""".trimIndent()
    )
)

class PolyCodeViewModel : ViewModel() {

    val audioSynthesizer = AudioSynthesizer()

    private val _uiState = MutableStateFlow(PolyCodeUiState())
    val uiState: StateFlow<PolyCodeUiState> = _uiState.asStateFlow()

    fun switchTab(tab: MainNavTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun selectFile(fileId: String) {
        _uiState.value = _uiState.value.copy(activeFileId = fileId)
    }

    fun loadTemplate(template: com.example.templates.ProjectTemplate) {
        _uiState.value = _uiState.value.copy(
            files = template.files,
            activeFileId = template.files.first().id,
            activeTab = MainNavTab.IDE,
            terminalLines = listOf(
                TerminalLine("⚡ Loaded ${template.title} Workspace", TerminalLineType.SUCCESS),
                TerminalLine("Boilerplate ready for ${template.primaryLanguage.displayName}", TerminalLineType.SYSTEM)
            )
        )
    }

    fun updateActiveFileContent(newContent: String) {
        val currentState = _uiState.value
        val updatedFiles = currentState.files.map { file ->
            if (file.id == currentState.activeFileId) {
                file.copy(content = newContent)
            } else {
                file
            }
        }
        _uiState.value = currentState.copy(files = updatedFiles)
    }

    fun updateCursorPosition(line: Int, col: Int) {
        _uiState.value = _uiState.value.copy(cursorLine = line, cursorCol = col)
    }

    fun executeActiveCode(stdinInput: String? = null) {
        val state = _uiState.value
        val activeFile = state.files.firstOrNull { it.id == state.activeFileId } ?: return

        _uiState.value = state.copy(isRunning = true)

        viewModelScope.launch {
            val fileMap = state.files.associate { it.name to it.content }
            val result: ExecutionResult = CodeRunnerEngine.runCode(
                code = activeFile.content,
                language = activeFile.language,
                associatedFiles = fileMap,
                stdinInput = stdinInput
            )

            val updatedLines = state.terminalLines + result.lines
            _uiState.value = state.copy(
                terminalLines = updatedLines,
                lastExecutionTimeMs = result.executionTimeMs,
                isRunning = false,
                liveWebPreviewHtml = if (result.hasWebPreview) result.webPreviewHtml else state.liveWebPreviewHtml,
                showLiveWebPreview = result.hasWebPreview
            )
        }
    }

    fun clearTerminal() {
        _uiState.value = _uiState.value.copy(
            terminalLines = listOf(
                TerminalLine("Terminal cleared. PolyCode ready for input.", TerminalLineType.SYSTEM)
            )
        )
    }

    fun toggleWebPreview() {
        _uiState.value = _uiState.value.copy(
            showLiveWebPreview = !_uiState.value.showLiveWebPreview
        )
    }

    fun addComment(lineNumber: Int, text: String) {
        val state = _uiState.value
        val activeFile = state.files.firstOrNull { it.id == state.activeFileId }
        val newComment = CodeComment(
            fileId = activeFile?.name ?: "App.jsx",
            lineNumber = lineNumber,
            author = "Vansh (You)",
            authorColor = PeerUserYou,
            text = text
        )
        val updatedComments = state.comments + newComment
        val newActivity = CollabActivity(
            userName = "Vansh",
            userColor = PeerUserYou,
            action = "Commented on line $lineNumber in ${activeFile?.name}"
        )
        _uiState.value = state.copy(
            comments = updatedComments,
            activities = listOf(newActivity) + state.activities
        )
    }

    fun addReply(commentId: String, replyText: String) {
        val state = _uiState.value
        val reply = CommentReply(
            author = "Vansh (You)",
            authorColor = PeerUserYou,
            text = replyText
        )
        val updatedComments = state.comments.map { c ->
            if (c.id == commentId) {
                c.copy(replies = c.replies + reply)
            } else {
                c
            }
        }
        _uiState.value = state.copy(comments = updatedComments)
    }

    fun toggleCommentResolved(commentId: String) {
        val state = _uiState.value
        val updated = state.comments.map { c ->
            if (c.id == commentId) c.copy(isResolved = !c.isResolved) else c
        }
        _uiState.value = state.copy(comments = updated)
    }

    fun applyCodeFromAi(snippet: String) {
        updateActiveFileContent(snippet)
        switchTab(MainNavTab.IDE)
    }

    fun openBrowserWithUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            browserInitialUrl = url,
            activeTab = MainNavTab.BROWSER
        )
    }
}
