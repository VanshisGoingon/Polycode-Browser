package com.example.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax.SupportedLanguage
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
import kotlinx.coroutines.launch

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val codeSnippet: String? = null,
    val language: SupportedLanguage? = null
)

@Composable
fun AiAssistantPane(
    currentCode: String,
    currentLanguage: SupportedLanguage,
    onApplyCodeToEditor: (String) -> Unit,
    onRunCodeDirectly: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = remember {
        mutableStateListOf(
            AiMessage(
                isUser = false,
                text = "⚡ Greetings! I am BaradAi, powered by Google Gemini and Google AI Studio, integrated by Vansh Barad for public service and developer productivity.\n\nAsk me any programming question, request architecture reviews, or ask me to generate React, Node.js, Python, or Java solutions.",
                codeSnippet = null
            )
        )
    }

    var userInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "Audit & Fix Bugs" to "Audit this code for potential runtime exceptions, edge cases, and provide an optimized fix.",
        "Explain Code" to "Please explain this code step by step and highlight any key concepts.",
        "Generate Tests" to "Write comprehensive unit tests for this code covering happy paths and edge cases.",
        "React Component" to "Generate a modern responsive React 18 component with interactive hooks.",
        "Python Script" to "Write a clean Python script using math and list comprehensions.",
        "Java OOP Class" to "Write a Java 21 class demonstrating encapsulation and stream processing."
    )

    fun sendPrompt(prompt: String) {
        if (prompt.isBlank() || isThinking) return
        val userText = prompt.trim()
        messages.add(AiMessage(isUser = true, text = userText))
        userInput = ""
        isThinking = true

        coroutineScope.launch {
            val systemContext = "You are BaradAi, a high-intelligence software engineering assistant powered by Google Gemini and Google AI Studio in the PolyCode IDE app created by Vansh Barad. Active file language is ${currentLanguage.displayName}. Code context:\n```\n$currentCode\n```"
            val rawResponse = BaradAiService.askGemini(userText, systemContext)

            // Extract code block if any
            val codeRegex = Regex("""```(?:[a-zA-Z0-9_-]+)?\s*([\s\S]*?)```""")
            val match = codeRegex.find(rawResponse)
            val extractedCode = match?.groupValues?.get(1)?.trim()
            val cleanText = if (match != null) rawResponse.replace(codeRegex, "").trim() else rawResponse

            messages.add(
                AiMessage(
                    isUser = false,
                    text = cleanText.ifBlank { "Here is the solution generated by BaradAi (Gemini):" },
                    codeSnippet = extractedCode,
                    language = currentLanguage
                )
            )
            isThinking = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // BaradAi Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(VividViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = VividViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BaradAi",
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VividViolet)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("GEMINI", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                        }
                    }
                    Text(
                        text = "Powered by Google Gemini & AI Studio • Ask Vansh AI",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            if (isThinking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = VividViolet,
                    strokeWidth = 2.dp
                )
            }
        }

        // Quick Prompt Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { (label, prompt) ->
                SuggestionChip(
                    onClick = { sendPrompt(prompt) },
                    label = { Text(label, fontSize = 11.sp, color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = DarkSurface
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = DarkBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AiMessageItem(
                    message = msg,
                    onApplyCode = { code -> onApplyCodeToEditor(code) },
                    onRunCode = { code -> onRunCodeDirectly(code) }
                )
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Ask BaradAi with Google Gemini...", fontSize = 12.sp, color = TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_input_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = VividViolet,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = VividViolet
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { sendPrompt(userInput) },
                enabled = !isThinking && userInput.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .background(if (!isThinking && userInput.isNotBlank()) VividViolet else DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (!isThinking && userInput.isNotBlank()) TextPrimary else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AiMessageItem(
    message: AiMessage,
    onApplyCode: (String) -> Unit,
    onRunCode: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) VividViolet.copy(alpha = 0.25f) else DarkSurface
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (message.isUser) 12.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 12.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (message.isUser) VividViolet else DarkBorder
            ),
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 19.sp
                )

                if (message.codeSnippet != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = message.codeSnippet,
                                fontFamily = RobotoMonoFontFamily,
                                fontSize = 11.5.sp,
                                color = NeonCyan,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onApplyCode(message.codeSnippet) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = NeonCyan),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apply to Editor", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onRunCode(message.codeSnippet) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = DarkBackground),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
