package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.runner.CodeRunnerEngine
import com.example.runner.TerminalLineType
import com.example.syntax.SupportedLanguage
import com.example.syntax.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PolyCode By Vansh", appName)
    }

    @Test
    fun `test syntax highlighter tokenizes python and react code`() {
        val pythonCode = "def hello():\n    print('Hello PolyCode')"
        val pyAnnotated = SyntaxHighlighter.highlight(pythonCode, SupportedLanguage.PYTHON)
        assertNotNull(pyAnnotated)
        assertTrue(pyAnnotated.text.contains("def hello"))

        val reactCode = "function App() { return <h1>PolyCode</h1>; }"
        val reactAnnotated = SyntaxHighlighter.highlight(reactCode, SupportedLanguage.REACT_JSX)
        assertNotNull(reactAnnotated)
        assertTrue(reactAnnotated.text.contains("function App"))
    }

    @Test
    fun `test code runner executes python stdout and stderr`() {
        // Test stdout
        val stdoutCode = "print('Hello from PolyCode runner')"
        val stdoutResult = CodeRunnerEngine.runCode(stdoutCode, SupportedLanguage.PYTHON)
        assertEquals(0, stdoutResult.exitCode)
        assertTrue(stdoutResult.lines.any { it.type == TerminalLineType.STDOUT && it.text.contains("Hello from PolyCode runner") })

        // Test stderr with raise Exception
        val errCode = "raise ValueError('Invalid argument in calculation')"
        val errResult = CodeRunnerEngine.runCode(errCode, SupportedLanguage.PYTHON)
        assertEquals(1, errResult.exitCode)
        assertTrue(errResult.lines.any { it.type == TerminalLineType.STDERR && it.text.contains("ValueError: Invalid argument") })
    }

    @Test
    fun `test code runner executes java stdout and stderr`() {
        val javaCode = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Java execution success!");
                    System.err.println("Warning: test error stream log");
                }
            }
        """.trimIndent()
        val javaResult = CodeRunnerEngine.runCode(javaCode, SupportedLanguage.JAVA)
        assertTrue(javaResult.lines.any { it.type == TerminalLineType.STDOUT && it.text.contains("Java execution success!") })
        assertTrue(javaResult.lines.any { it.type == TerminalLineType.STDERR && it.text.contains("Warning: test error stream log") })
    }

    @Test
    fun `test code runner executes nodejs stdout and stderr`() {
        val nodeCode = """
            console.log("Server listening on port 8080");
            console.error("Database connection timeout");
        """.trimIndent()
        val nodeResult = CodeRunnerEngine.runCode(nodeCode, SupportedLanguage.NODEJS)
        assertTrue(nodeResult.lines.any { it.type == TerminalLineType.STDOUT && it.text.contains("Server listening on port 8080") })
        assertTrue(nodeResult.lines.any { it.type == TerminalLineType.STDERR && it.text.contains("Database connection timeout") })
    }

    @Test
    fun `test code runner generates react html bundle`() {
        val jsxCode = "function App() { return <div>Live</div>; }"
        val result = CodeRunnerEngine.runCode(jsxCode, SupportedLanguage.REACT_JSX)
        assertTrue(result.hasWebPreview)
        assertNotNull(result.webPreviewHtml)
        assertTrue(result.webPreviewHtml!!.contains("ReactDOM.createRoot"))
    }
}
