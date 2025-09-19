@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExamplesScreen(
    onNavigateBack: () -> Unit
) {
    val examples = listOf(
        ExampleCategory(
            title = "AI Chat Examples",
            icon = Icons.Default.Chat,
            color = Color(0xFF4CAF50),
            examples = listOf(
                Example(
                    "Create a To-Do List",
                    "Type or Say: 'Create a shopping list with bread, milk, eggs, and tomato'\n\n" +
                    "AI will automatically:\n" +
                    "• Generate checkboxes for each item\n" +
                    "• Close the chat and create a new note\n" +
                    "• Save with proper checkbox formatting\n" +
                    "• Allow you to tap checkboxes to mark as done\n\n" +
                    "Other examples that work:\n" +
                    "• 'I need to buy bread, milk, and eggs'\n" +
                    "• 'Make a grocery list with apples, bananas, and oranges'\n" +
                    "• 'Create a checklist with task A, task B, and task C'"
                ),
                Example(
                    "Meeting Notes",
                    "Type or Say: 'Take notes for my meeting about project deadline on Friday, budget review, and team assignments'\n\n" +
                    "AI will create:\n" +
                    "• Structured meeting notes\n" +
                    "• Action items as checkboxes\n" +
                    "• Save everything as a searchable note"
                ),
                Example(
                    "Quick Reminders",
                    "Type or Say: 'Remind me to call mom at 3 PM tomorrow'\n\n" +
                    "AI will:\n" +
                    "• Create a task with date/time\n" +
                    "• Set up notification reminder\n" +
                    "• Add to your task list"
                ),
                Example(
                    "Brainstorming Ideas",
                    "Type or Say: 'Help me brainstorm ideas for my weekend trip to the mountains'\n\n" +
                    "AI will suggest:\n" +
                    "• Activity ideas with checkboxes\n" +
                    "• Packing list items\n" +
                    "• Save as organized notes"
                ),
                Example(
                    "Recipe Planning",
                    "Type or Say: 'Plan meals for this week with healthy options'\n\n" +
                    "AI creates:\n" +
                    "• Daily meal suggestions\n" +
                    "• Shopping list with checkboxes\n" +
                    "• Cooking instructions as notes"
                )
            )
        ),
        ExampleCategory(
            title = "AI Voice Examples",
            icon = Icons.Default.Mic,
            color = Color(0xFF2196F3),
            examples = listOf(
                Example(
                    "Voice Task Creation",
                    "Simply speak: 'Create task: Buy groceries tomorrow at 10 AM'\n\n" +
                    "Features:\n" +
                    "• Automatic speech-to-text\n" +
                    "• Smart date/time detection\n" +
                    "• Instant task creation\n" +
                    "• Works in Serbian and Slovenian too!"
                ),
                Example(
                    "Quick Voice Notes",
                    "Say: 'Take a note: Great restaurant idea - try the new Italian place on Main Street'\n\n" +
                    "AI will:\n" +
                    "• Convert speech to text accurately\n" +
                    "• Save as searchable note\n" +
                    "• Categorize automatically"
                ),
                Example(
                    "Voice Commands",
                    "Try these commands:\n" +
                    "• 'Show my tasks for today'\n" +
                    "• 'Read my latest notes'\n" +
                    "• 'Create reminder for dentist appointment'\n" +
                    "• 'What meetings do I have tomorrow?'"
                ),
                Example(
                    "Multilingual Support",
                    "Speak in your preferred language:\n\n" +
                    "🇺🇸 English: 'Create a shopping list'\n" +
                    "🇷🇸 Serbian: 'Napravi listu za kupovinu'\n" +
                    "🇸🇮 Slovenian: 'Ustvari nakupovalni seznam'\n\n" +
                    "AI understands and responds appropriately!"
                ),
                Example(
                    "Hands-Free Operation",
                    "Perfect for:\n" +
                    "• Driving (create voice reminders)\n" +
                    "• Cooking (add ingredients to lists)\n" +
                    "• Walking (capture quick thoughts)\n" +
                    "• Working out (track progress notes)"
                )
            )
        ),
        ExampleCategory(
            title = "Smart Features",
            icon = Icons.Default.AutoAwesome,
            color = Color(0xFF9C27B0),
            examples = listOf(
                Example(
                    "Auto-Checkbox Detection",
                    "When you mention multiple items, AI automatically creates checkboxes:\n\n" +
                    "✅ WORKS: 'I need to buy bread, milk, and tomato'\n" +
                    "✅ WORKS: 'Create a list with apples, oranges, and bananas'\n" +
                    "✅ WORKS: 'Make a checklist with:\n1. Clean house\n2. Buy groceries\n3. Call mom'\n\n" +
                    "AI will format as:\n" +
                    "☐ Buy bread\n" +
                    "☐ Buy milk\n" +
                    "☐ Buy tomato\n\n" +
                    "💡 Tip: Use words like 'buy', 'need', 'create list', or separate items with commas and 'and'"
                ),
                Example(
                    "Smart Task Creation",
                    "Create tasks from any conversation:\n\n" +
                    "Chat: 'Meeting with client about website redesign'\n" +
                    "→ Click 'Create Tasks' button\n" +
                    "→ Automatically generates relevant tasks"
                ),
                Example(
                    "Context-Aware Responses",
                    "AI provides human-like, helpful responses:\n\n" +
                    "You: 'I'm feeling overwhelmed with work'\n" +
                    "AI: 'I understand that feeling. Let's break down your tasks into manageable pieces. What's the most urgent thing you need to handle today?'"
                ),
                Example(
                    "Chat Summarization",
                    "Long conversations are automatically summarized when saved as notes:\n\n" +
                    "• Key points extracted\n" +
                    "• Action items highlighted\n" +
                    "• Main topics organized\n" +
                    "• Easy to review later"
                ),
                Example(
                    "Voice Response",
                    "AI speaks back to you in natural language:\n\n" +
                    "• Confirms task creation\n" +
                    "• Reads back important information\n" +
                    "• Provides spoken feedback\n" +
                    "• Matches your language preference"
                )
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "How to Use EchoNote",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome to EchoNote! 🎉",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your intelligent assistant for notes, tasks, and voice commands. Here are some practical examples to get you started:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(examples) { category ->
            ExampleCategoryCard(category = category)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro Tips",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Be specific in your requests for better results\n" +
                        "• Use natural language - speak as you normally would\n" +
                        "• Try different languages for voice commands\n" +
                        "• Use the 'Create Tasks' button after conversations\n" +
                        "• Check the checkbox lists by tapping on them",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ExampleCategoryCard(category: ExampleCategory) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (expanded) 12.dp else 0.dp)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = category.color,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = category.color
                    )
                }
            }

            if (expanded) {
                category.examples.forEach { example ->
                    ExampleCard(
                        example = example,
                        color = category.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ExampleCard(
    example: Example,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = example.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = example.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

data class ExampleCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val examples: List<Example>
)

data class Example(
    val title: String,
    val description: String
)