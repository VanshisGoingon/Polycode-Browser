package com.example.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxAttribute
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxOperator
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.SyntaxType
import com.example.ui.theme.TextPrimary
import java.util.regex.Pattern

enum class SupportedLanguage(
    val id: String,
    val displayName: String,
    val extension: String,
    val defaultFileName: String
) {
    HTML("html", "HTML/Web", ".html", "index.html"),
    CSS("css", "CSS", ".css", "styles.css"),
    JAVASCRIPT("javascript", "JavaScript", ".js", "script.js"),
    REACT_JSX("react", "React (JSX)", ".jsx", "App.jsx"),
    NODEJS("nodejs", "Node.js", ".js", "server.js"),
    PYTHON("python", "Python", ".py", "main.py"),
    JAVA("java", "Java", ".java", "Main.java")
}

object SyntaxHighlighter {

    // Keywords per language
    private val PYTHON_KEYWORDS = setOf(
        "def", "class", "import", "from", "return", "if", "elif", "else", "for", "while",
        "in", "is", "not", "and", "or", "try", "except", "finally", "with", "as", "lambda",
        "yield", "pass", "break", "continue", "raise", "assert", "global", "nonlocal", "async", "await"
    )

    private val JS_KEYWORDS = setOf(
        "const", "let", "var", "function", "return", "if", "else", "for", "while", "do",
        "switch", "case", "default", "break", "continue", "try", "catch", "finally", "throw",
        "class", "extends", "super", "this", "new", "import", "export", "from", "as", "default",
        "async", "await", "yield", "typeof", "instanceof", "void", "delete", "null", "undefined",
        "true", "false"
    )

    private val REACT_HOOKS = setOf(
        "useState", "useEffect", "useMemo", "useCallback", "useRef", "useContext", "useReducer"
    )

    private val JAVA_KEYWORDS = setOf(
        "public", "private", "protected", "class", "interface", "enum", "extends", "implements",
        "static", "final", "abstract", "synchronized", "volatile", "transient", "native",
        "void", "boolean", "byte", "char", "short", "int", "long", "float", "double",
        "return", "if", "else", "for", "while", "do", "switch", "case", "default", "break",
        "continue", "try", "catch", "finally", "throw", "throws", "new", "this", "super",
        "import", "package", "instanceof", "true", "false", "null"
    )

    private val CSS_KEYWORDS = setOf(
        "display", "flex", "grid", "margin", "padding", "border", "background", "color",
        "font", "width", "height", "position", "top", "left", "right", "bottom", "z-index",
        "overflow", "opacity", "transition", "transform", "animation", "box-shadow", "border-radius"
    )

    /**
     * Highlights code according to the given language.
     */
    fun highlight(code: String, language: SupportedLanguage): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = TextPrimary), 0, code.length)

            when (language) {
                SupportedLanguage.HTML -> highlightHtml(code, this)
                SupportedLanguage.CSS -> highlightCss(code, this)
                SupportedLanguage.JAVASCRIPT,
                SupportedLanguage.NODEJS -> highlightJs(code, this, isReact = false)
                SupportedLanguage.REACT_JSX -> highlightJs(code, this, isReact = true)
                SupportedLanguage.PYTHON -> highlightPython(code, this)
                SupportedLanguage.JAVA -> highlightJava(code, this)
            }
        }
    }

    private fun highlightPython(code: String, builder: AnnotatedString.Builder) {
        // Comments
        val commentMatcher = Pattern.compile("#.*").matcher(code)
        while (commentMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), commentMatcher.start(), commentMatcher.end())
        }

        // Triple and single/double quoted strings
        val stringMatcher = Pattern.compile("(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"]*\"|'[^']*')").matcher(code)
        while (stringMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), stringMatcher.start(), stringMatcher.end())
        }

        // Numbers
        val numMatcher = Pattern.compile("\\b\\d+(\\.\\d+)?\\b").matcher(code)
        while (numMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber), numMatcher.start(), numMatcher.end())
        }

        // Decorators
        val decoratorMatcher = Pattern.compile("@[A-Za-z_][A-Za-z0-9_]*").matcher(code)
        while (decoratorMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.SemiBold), decoratorMatcher.start(), decoratorMatcher.end())
        }

        // Words
        val wordMatcher = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(code)
        while (wordMatcher.find()) {
            val word = wordMatcher.group()
            val start = wordMatcher.start()
            val end = wordMatcher.end()
            when {
                word in PYTHON_KEYWORDS -> {
                    builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), start, end)
                }
                word == "self" || word == "cls" -> {
                    builder.addStyle(SpanStyle(color = SyntaxAttribute, fontWeight = FontWeight.Medium), start, end)
                }
                word == "print" || word == "len" || word == "range" || word == "str" || word == "int" || word == "list" -> {
                    builder.addStyle(SpanStyle(color = SyntaxFunction), start, end)
                }
            }
        }
    }

    private fun highlightJs(code: String, builder: AnnotatedString.Builder, isReact: Boolean) {
        // Line comments
        val lineComment = Pattern.compile("//.*").matcher(code)
        while (lineComment.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), lineComment.start(), lineComment.end())
        }

        // Block comments
        val blockComment = Pattern.compile("/\\*[\\s\\S]*?\\*/").matcher(code)
        while (blockComment.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), blockComment.start(), blockComment.end())
        }

        // Strings (template strings and regular)
        val stringMatcher = Pattern.compile("(`[\\s\\S]*?`|\"[^\"]*\"|'[^']*')").matcher(code)
        while (stringMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), stringMatcher.start(), stringMatcher.end())
        }

        // Numbers
        val numMatcher = Pattern.compile("\\b\\d+(\\.\\d+)?\\b").matcher(code)
        while (numMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber), numMatcher.start(), numMatcher.end())
        }

        // JSX tags if React
        if (isReact) {
            val jsxTag = Pattern.compile("</?[A-Za-z][A-Za-z0-9_.]*").matcher(code)
            while (jsxTag.find()) {
                val tagStr = jsxTag.group()
                val isComponent = tagStr.length > 1 && tagStr.dropWhile { it == '<' || it == '/' }.firstOrNull()?.isUpperCase() == true
                val color = if (isComponent) SyntaxType else SyntaxTag
                builder.addStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold), jsxTag.start(), jsxTag.end())
            }
        }

        // Words
        val wordMatcher = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(code)
        while (wordMatcher.find()) {
            val word = wordMatcher.group()
            val start = wordMatcher.start()
            val end = wordMatcher.end()
            when {
                word in JS_KEYWORDS -> {
                    builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), start, end)
                }
                isReact && word in REACT_HOOKS -> {
                    builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.SemiBold), start, end)
                }
                word == "console" || word == "Math" || word == "JSON" || word == "document" || word == "window" || word == "React" -> {
                    builder.addStyle(SpanStyle(color = SyntaxType), start, end)
                }
            }
        }
    }

    private fun highlightJava(code: String, builder: AnnotatedString.Builder) {
        // Line comments
        val lineComment = Pattern.compile("//.*").matcher(code)
        while (lineComment.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), lineComment.start(), lineComment.end())
        }

        // Block comments
        val blockComment = Pattern.compile("/\\*[\\s\\S]*?\\*/").matcher(code)
        while (blockComment.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), blockComment.start(), blockComment.end())
        }

        // Strings
        val stringMatcher = Pattern.compile("(\"[^\"]*\"|'[^']*')").matcher(code)
        while (stringMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), stringMatcher.start(), stringMatcher.end())
        }

        // Annotations
        val annotMatcher = Pattern.compile("@[A-Za-z_][A-Za-z0-9_]*").matcher(code)
        while (annotMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Medium), annotMatcher.start(), annotMatcher.end())
        }

        // Numbers
        val numMatcher = Pattern.compile("\\b\\d+(\\.\\d+)?[fFlL]?\\b").matcher(code)
        while (numMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber), numMatcher.start(), numMatcher.end())
        }

        // Words
        val wordMatcher = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(code)
        while (wordMatcher.find()) {
            val word = wordMatcher.group()
            val start = wordMatcher.start()
            val end = wordMatcher.end()
            when {
                word in JAVA_KEYWORDS -> {
                    builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), start, end)
                }
                word == "System" || word == "String" || word == "Integer" || word == "List" || word == "ArrayList" || word == "Map" || word == "HashMap" -> {
                    builder.addStyle(SpanStyle(color = SyntaxType), start, end)
                }
                word == "out" || word == "println" || word == "print" -> {
                    builder.addStyle(SpanStyle(color = SyntaxFunction), start, end)
                }
            }
        }
    }

    private fun highlightHtml(code: String, builder: AnnotatedString.Builder) {
        // Comments
        val commentMatcher = Pattern.compile("<!--[\\s\\S]*?-->").matcher(code)
        while (commentMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), commentMatcher.start(), commentMatcher.end())
        }

        // HTML Tags
        val tagMatcher = Pattern.compile("</?[a-zA-Z0-9]+").matcher(code)
        while (tagMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold), tagMatcher.start(), tagMatcher.end())
        }

        // Attributes
        val attrMatcher = Pattern.compile("\\b[a-zA-Z\\-]+(?=\\=)").matcher(code)
        while (attrMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxAttribute), attrMatcher.start(), attrMatcher.end())
        }

        // Strings inside quotes
        val stringMatcher = Pattern.compile("\"[^\"]*\"|'[^']*'").matcher(code)
        while (stringMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), stringMatcher.start(), stringMatcher.end())
        }
    }

    private fun highlightCss(code: String, builder: AnnotatedString.Builder) {
        // Comments
        val commentMatcher = Pattern.compile("/\\*[\\s\\S]*?\\*/").matcher(code)
        while (commentMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), commentMatcher.start(), commentMatcher.end())
        }

        // CSS properties
        val propMatcher = Pattern.compile("\\b[a-z\\-]+(?=\\s*:)").matcher(code)
        while (propMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxAttribute, fontWeight = FontWeight.Medium), propMatcher.start(), propMatcher.end())
        }

        // Units and values
        val unitMatcher = Pattern.compile("\\b\\d+(px|em|rem|%|vh|vw|s|ms|deg)\\b").matcher(code)
        while (unitMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber), unitMatcher.start(), unitMatcher.end())
        }

        // Hex colors
        val hexMatcher = Pattern.compile("#[0-9a-fA-F]{3,8}\\b").matcher(code)
        while (hexMatcher.find()) {
            builder.addStyle(SpanStyle(color = SyntaxType), hexMatcher.start(), hexMatcher.end())
        }
    }
}
