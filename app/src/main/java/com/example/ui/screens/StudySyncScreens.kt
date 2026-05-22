package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.*
import java.text.SimpleDateFormat
import java.util.*

// A premium Material 3 Card component with thin colored borders and flat elevations
@Composable
fun M3Card(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: ButtonElevation? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

// Custom animated pulsing dot for active study sessions
@Composable
fun PulsingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val opacity by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacity"
    )

    val pulseColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(10.dp)
            .drawBehind {
                drawCircle(
                    color = pulseColor.copy(alpha = opacity),
                    radius = size.minDimension / 2 * scale
                )
                drawCircle(
                    color = pulseColor,
                    radius = size.minDimension / 2 * 0.7f
                )
            }
    )
}

// HOME SCREEN COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: StudySyncViewModel) {
    val profileState by viewModel.profile.collectAsState()
    val activeSessionState by viewModel.activeSession.collectAsState()
    val todosList by viewModel.todos.collectAsState()
    val goalsList by viewModel.goals.collectAsState()

    var showAddTodoDialog by remember { mutableStateOf(false) }
    var todoText by remember { mutableStateOf("") }
    var isTodoRecurring by remember { mutableStateOf(false) }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalSubject by remember { mutableStateOf(ChapterMap.subjects.first()) }
    var selectedGoalHours by remember { mutableStateOf("2") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Profile Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Salam, ${profileState?.displayName ?: "Scholar"}! 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Welcome back to your quiet study circle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { viewModel.navigateTo("profile") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profileState?.avatarEmoji ?: "🦉",
                    fontSize = 28.sp
                )
            }
        }

        // Active Session Status Banner
        activeSessionState?.let { active ->
            M3Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo("room_detail") },
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PulsingDot()
                        Column {
                            Text(
                                text = "Currently Studying!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "${active.subject} — ${active.chapter}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.navigateTo("room_detail") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Re-enter", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Streak Card splitted 50/50 functionally
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            M3Card(
                modifier = Modifier.weight(1f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if ((profileState?.streakDays ?: 0) > 0) "🌿 ACTIVE" else "❄️ INACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((profileState?.streakDays ?: 0) > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profileState?.streakDays ?: 0} Days",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            M3Card(
                modifier = Modifier.weight(1f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⭐ PERSONAL BEST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profileState?.longestStreak ?: 0} Days",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = "Longest Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Daily Goals Progress Indicator
        M3Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Goals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Based on subjects you map for the day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showAddGoalDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (goalsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study targets configured today.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    goalsList.forEach { goal ->
                        val progress = if (goal.targetMinutes > 0) {
                            (goal.studiedMinutes.toFloat() / goal.targetMinutes).coerceIn(0f, 1f)
                        } else 0f

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (progress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                    )
                                    Text(
                                        text = goal.subject,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${goal.studiedMinutes}m / ${goal.targetMinutes}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteGoal(goal.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Goal",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // To-Do Checklist (Interactive)
        M3Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Studies Checklist",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Check off minor tasks easily",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showAddTodoDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (todosList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study tasks left! Keep it up.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    todosList.forEach { todo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (todo.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = todo.isDone,
                                    onCheckedChange = { viewModel.toggleTodoState(todo) }
                                )
                                Column {
                                    Text(
                                        text = todo.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                            fontWeight = if (todo.isDone) FontWeight.Normal else FontWeight.SemiBold,
                                            color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (todo.isRecurringDaily) {
                                        Text(
                                            text = "🔁 Daily Recurring",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.deleteTodo(todo.id) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Todo",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // ADD TODO DIALOG
    if (showAddTodoDialog) {
        AlertDialog(
            onDismissRequest = { showAddTodoDialog = false },
            title = { Text("Add Focus Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = todoText,
                        onValueChange = { todoText = it },
                        label = { Text("What are you checking off?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isTodoRecurring,
                            onCheckedChange = { isTodoRecurring = it }
                        )
                        Text("Repeat daily checklist")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (todoText.isNotBlank()) {
                            viewModel.addTodoItem(todoText, isTodoRecurring)
                            todoText = ""
                            isTodoRecurring = false
                            showAddTodoDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTodoDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ADD GOAL DIALOG
    if (showAddGoalDialog) {
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Set Subject Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose Subject:", style = MaterialTheme.typography.labelMedium)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedGoalSubject)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            ChapterMap.subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = {
                                        selectedGoalSubject = subj
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedGoalHours,
                        onValueChange = { selectedGoalHours = it },
                        label = { Text("Target Focus Time (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hours = selectedGoalHours.toIntOrNull() ?: 1
                        viewModel.addGoalItem(selectedGoalSubject, hours.coerceIn(1, 12))
                        showAddGoalDialog = false
                    }
                ) {
                    Text("Configure")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ROOMS LIST BROWSER SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(viewModel: StudySyncViewModel) {
    val roomsList by viewModel.rooms.collectAsState()

    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var customRoomName by remember { mutableStateOf("") }
    var customRoomSubject by remember { mutableStateOf(ChapterMap.subjects.first()) }
    var customRoomEmoji by remember { mutableStateOf("📚") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Study Rooms",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Join a subject desk & study with peers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showCreateRoomDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Room")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom")
            }
        }

        // Standard pre-seeded focus rooms
        Text(
            text = "SSC & HSC CURRICULUM ROOMS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        val standardRooms = roomsList.filter { !it.isCustom }
        standardRooms.forEach { room ->
            M3Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.joinRoom(room) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(room.emoji, fontSize = 24.sp)
                        }

                        Column {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Category: ${room.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "${room.memberCount} active",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Enter Room", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Custom user rooms
        val customRooms = roomsList.filter { it.isCustom }
        if (customRooms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "USER CREATED DESKS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )

            customRooms.forEach { room ->
                M3Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.joinRoom(room) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(room.emoji, fontSize = 24.sp)
                            }

                            Column {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Custom Table — Code ${room.id * 7 + 104}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Join Circle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            IconButton(onClick = { viewModel.deleteCustomRoom(room) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // CREATE CUSTOM ROOM DIALOG
    if (showCreateRoomDialog) {
        val emojis = listOf("📚", "🧠", "💡", "✍️", "📐", "🚀", "🎨", "💻")
        var subjectExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = { Text("Create Private Study Desk") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customRoomName,
                        onValueChange = { customRoomName = it },
                        label = { Text("Desk Room Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Map to Subject Course:", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { subjectExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(customRoomSubject)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            ChapterMap.subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = {
                                        customRoomSubject = subj
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Choose Emoji Badge:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojis.forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (customRoomEmoji == emo) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (customRoomEmoji == emo) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { customRoomEmoji = emo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customRoomName.isNotBlank()) {
                            viewModel.createCustomRoom(customRoomName, customRoomSubject, customRoomEmoji)
                            customRoomName = ""
                            showCreateRoomDialog = false
                        }
                    }
                ) {
                    Text("Assemble Desk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// LIVE STUDY ROOM DETAIL SCREEN (Focus Center)
@Composable
fun RoomDetailScreen(viewModel: StudySyncViewModel) {
    val activeRoom = viewModel.selectedRoom ?: return
    val activeSessionState by viewModel.activeSession.collectAsState()
    val partnerCircle by viewModel.currentRoomPartners.collectAsState()
    val chatChannelMessages by viewModel.currentRoomChat.collectAsState()
    val profileState by viewModel.profile.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Focus & Table, 1 = Discussion Flow
    var showStartDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf(activeRoom.subject) }
    var selectedChapter by remember { mutableStateOf("") }
    var userMessage by remember { mutableStateOf("") }

    val chapters = ChapterMap.chaptersMap[activeRoom.subject] ?: listOf("General Chapters")
    if (selectedChapter.isEmpty() && chapters.isNotEmpty()) {
        selectedChapter = chapters.first()
    }

    // Core Layout
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { viewModel.leaveRoom() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Rooms", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = activeRoom.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                     Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🟢 Live Connected",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Dual Tab Header (Focus circle vs chat discussions)
                TabRow(selectedTabIndex = activeTab, containerColor = Color.Transparent) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("⏱️ Study Desk", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💬 Disk Discuss", fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${chatChannelMessages.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen contents
            if (activeTab == 0) {
                // FOCUS TIMER & STUDY CIRCLE PANEL
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Center Clock View
                    M3Card(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (activeSessionState != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        backgroundColor = if (activeSessionState != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (activeSessionState != null) "FOCUS RUNNING" else "READY TO FOCUS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = if (activeSessionState != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            // Digital Ticking Clock Display
                            Text(
                                text = viewModel.formatDuration(viewModel.elapsedSeconds),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    shadow = Shadow(
                                        color = if (activeSessionState != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 16f
                                    )
                                ),
                                color = if (activeSessionState != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            activeSessionState?.let { active ->
                                Text(
                                    text = "Active Subject: ${active.subject}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "Chapter: ${active.chapter}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Start/Stop Primary Button
                            if (activeSessionState == null) {
                                Button(
                                    onClick = { showStartDialog = true },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sit & Start Studying", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.stopActiveSession() },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Terminate Session", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Study Table Interactive Seat Display
                    Text(
                        text = "VIRTUAL DESK PARTNERS (STUDYING TOGETHER)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        modifier = Modifier.align(Alignment.Start),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Seats Card list
                    partnerCircle.forEach { partner ->
                        M3Card(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            backgroundColor = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(partner.avatarEmoji, fontSize = 22.sp)
                                    }

                                    Column {
                                        Text(
                                            text = partner.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${partner.subject} — ${partner.chapter}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PulsingDot()
                                    Text(
                                        text = "${partner.elapsedMinutes}m",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                // LIVE DISCUSSION DISCUSSION PANEL
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        reverseLayout = false
                    ) {
                        items(chatChannelMessages) { msg ->
                            val bubbleBg = if (msg.isSelf) MaterialTheme.colorScheme.primaryContainer
                            else if (msg.senderName == "System") Color(0xFF78350F).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant

                            val bubbleBorder = if (msg.senderName == "System") Color(0xFFFFB86C).copy(alpha = 0.3f)
                            else Color.Transparent

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (msg.isSelf) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (msg.isSelf) 12.dp else 0.dp,
                                                bottomEnd = if (msg.isSelf) 0.dp else 12.dp
                                            )
                                        )
                                        .background(bubbleBg)
                                        .border(
                                            1.dp,
                                            bubbleBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = msg.messageText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (msg.senderName == "System") Color(0xFFFFB86C) else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Interactive Quick Reactions Display
                                if (msg.reactions.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        msg.reactions.forEach { (react, count) ->
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.addReactionToMessage(msg.id, react) }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("$react $count", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                // Double Click / Long Press Quick reaction option helper
                                if (msg.senderName != "System") {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
                                    ) {
                                        listOf("👍", "🔥", "🙌").forEach { emoji ->
                                            Text(
                                                text = emoji,
                                                fontSize = 11.sp,
                                                modifier = Modifier
                                                    .clickable { viewModel.addReactionToMessage(msg.id, emoji) }
                                                    .padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom message text sender panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userMessage,
                            onValueChange = { userMessage = it },
                            placeholder = { Text("Ask peer group members...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )

                        IconButton(
                            onClick = {
                                if (userMessage.isNotBlank()) {
                                    viewModel.sendUserChatMessage(userMessage)
                                    userMessage = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Message", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            // ANTI-CHEAT CHECK-IN POPUP BLOCKER
            if (viewModel.checkInPromptActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f))
                        .clickable(enabled = false) {}, // Intercept taps
                    contentAlignment = Alignment.Center
                ) {
                    M3Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        borderColor = Color(0xFFFFB86C),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Anti-Cheat Audit Check",
                                tint = Color(0xFFFFB86C),
                                modifier = Modifier.size(52.dp)
                            )

                            Text(
                                text = "Anti-Fake Study Validation",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )

                            Text(
                                text = "Are you still focused at your study table? Respond to confirm active attendance, otherwise the session auto-terminates with penalty.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Countdown circular loader mock
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${viewModel.checkInSecondsRemaining}s",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { viewModel.confirmUserActivity() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Yes, I am focused!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // START SESSION CHOICE CONFIG DIALOG
    if (showStartDialog) {
        var chapterExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text("Map Session Focus") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selected Room Course: ${activeRoom.subject}", fontWeight = FontWeight.SemiBold)

                    Text("Pick Course Chapter Target:", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { chapterExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedChapter, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = chapterExpanded,
                            onDismissRequest = { chapterExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            chapters.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(chap) },
                                    onClick = {
                                        selectedChapter = chap
                                        chapterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startStudySession(
                            subject = activeRoom.subject,
                            chapter = selectedChapter,
                            roomId = activeRoom.id
                        )
                        showStartDialog = false
                    }
                ) {
                    Text("Sit down and study")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// LEADERBOARD SCREEN
@Composable
fun LeaderboardScreen(viewModel: StudySyncViewModel) {
    val profileState by viewModel.profile.collectAsState()
    val scrollState = rememberScrollState()
    val sessionsList by viewModel.sessions.collectAsState()

    var statsTab by remember { mutableStateOf(0) } // 0 = Weekly Trends, 1 = Subject Split, 2 = Rankings

    val userTotalSeconds = viewModel.totalSeconds.collectAsState(initial = 0L).value ?: 10L
    val userTotalMinutes = userTotalSeconds / 60

    val rankings = listOf(
        Triple("Nafis Tahmid", "🧑‍💻", 345L),
        Triple("Anika Rahman", "🎓", 294L),
        Triple("Nabil Haque", "🦁", 212L),
        Triple(profileState?.displayName ?: "Owl Studier", profileState?.avatarEmoji ?: "🦉", userTotalMinutes),
        Triple("Maliha Chowdhury", "👩‍🎓", 188L),
        Triple("Zubayer Hossain", "✍️", 132L),
        Triple("Sajid Rahman", "🎧", 112L),
        Triple("Taskin Ahmed", "🏀", 92L)
    ).sortedByDescending { it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title block
        Column {
            Text(
                text = "Stats & Rankings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Analyze your focus depth and check real-time desks leaderboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Minimal Pill-shaped Segmented Choice Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labels = listOf("Weekly Trends", "Subjects Split", "Desk Rankings")
            labels.forEachIndexed { index, label ->
                val selected = statsTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary 
                            else Color.Transparent
                        )
                        .clickable { statsTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (statsTab) {
            0 -> {
                // WEEKLY TRENDS VIEW
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "WEEKLY STUDY TIME TREND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Gather dynamic study trend minutes
                    val dayMinutes = remember(sessionsList, userTotalMinutes) {
                        val baseValues = floatArrayOf(45f, 60f, 30f, 95f, 40f, 50f, 20f)
                        val cal = Calendar.getInstance()
                        val currentDayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
                        baseValues[currentDayIndex] += userTotalMinutes.toFloat()
                        baseValues
                    }

                    val dayNames = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val cal = Calendar.getInstance()
                    val todayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7

                    M3Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Activity Log (Minutes)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Current week focus distribution",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "LIVE UPDATE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            // Dynamic Canvas Bar Chart (Adaptive & Responsive mathematically centered)
                            val themePrimary = MaterialTheme.colorScheme.primary
                            val themePrimaryContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val numBars = 7
                                    val barWidth = (canvasWidth / numBars) * 0.45f
                                    val maxVal = dayMinutes.maxOrNull()?.coerceAtLeast(80f) ?: 100f

                                    // Draw dotted grid lines
                                    val levels = listOf(30f, 60f, 90f)
                                    levels.forEach { lvl ->
                                        val y = canvasHeight - (lvl / maxVal) * (canvasHeight - 20.dp.toPx())
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(0f, y),
                                            end = Offset(canvasWidth, y),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                        )
                                    }

                                    // Draw vertical rounded bars mapped precisely to the centers of row layout weights
                                    for (i in 0..6) {
                                        val valMinutes = dayMinutes[i]
                                        val barHeight = (valMinutes / maxVal) * (canvasHeight - 20.dp.toPx())
                                        val centerX = (i + 0.5f) * (canvasWidth / numBars)
                                        val x = centerX - (barWidth / 2f)
                                        val y = canvasHeight - barHeight.coerceAtLeast(8f)

                                        val isToday = i == todayIndex
                                        val barColor = if (isToday) themePrimary else themePrimaryContainer

                                        drawRoundRect(
                                            color = barColor,
                                            topLeft = Offset(x, y),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(8f)),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                    }
                                }
                            }

                            // Label keys mapped precisely with weighted cells
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 0..6) {
                                    val isToday = i == todayIndex
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNames[i],
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Productivity Summary Summary Cards
                    M3Card(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weekly Focus Output",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You represent the top 5% of active virtual desks in Bangladesh this semester.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "🔥 340m",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
            1 -> {
                // SUBJECT WISE BREAKDOWN VIEW
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SUBJECT PORTFOLIO ATTENTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val sortedSubjects = remember(sessionsList, userTotalMinutes) {
                        val baseMap = mutableMapOf(
                            "Physics" to 120L,
                            "Chemistry" to 90L,
                            "Biology" to 80L,
                            "Higher Math" to 150L,
                            "General Math" to 110L,
                            "Bangla 1st" to 40L,
                            "English 1st" to 60L
                        )
                        // Feed active room or completions
                        viewModel.selectedRoom?.let { room ->
                            baseMap[room.subject] = (baseMap[room.subject] ?: 0L) + userTotalMinutes
                        }
                        baseMap.entries.toList().sortedByDescending { it.value }
                    }

                    M3Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Subject-Wise Time Split",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            sortedSubjects.forEach { entry ->
                                val subjName = entry.key
                                val minutes = entry.value
                                val targetMin = 200f
                                val progress = (minutes.toFloat() / targetMin).coerceIn(0f, 1f)

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Text(
                                                text = subjName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Text(
                                            text = "${minutes}m",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = if (progress >= 0.7f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // RANKINGS VIEW (The Original Leaderboard & Podiums)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ACCOUNTABILITY LEADERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Podiums of Top 3 in distinctive layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // #2 place
                        val second = rankings.getOrNull(1)
                        second?.let { sec ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(sec.second, fontSize = 28.sp)
                                Text(sec.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🥈 2nd", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${sec.third}m", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // #1 place
                        val first = rankings.getOrNull(0)
                        first?.let { fst ->
                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👑", fontSize = 20.sp)
                                Text(fst.second, fontSize = 36.sp)
                                Text(fst.first, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🥇 1st", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("${fst.third}m", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        // #3 place
                        val third = rankings.getOrNull(2)
                        third?.let { thd ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(thd.second, fontSize = 28.sp)
                                Text(thd.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🥉 3rd", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                        Text("${thd.third}m", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Leaderboard detailed list
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ALL ACTIVE CONTENDERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    rankings.forEachIndexed { pos, peer ->
                        val isMe = peer.first == (profileState?.displayName ?: "User")
                        val cardBg = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface

                        val cardBorder = if (isMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                        M3Card(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = cardBorder,
                            backgroundColor = cardBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (pos < 3) MaterialTheme.colorScheme.surfaceVariant
                                                else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${pos + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(peer.second, fontSize = 20.sp)
                                    }

                                    Column {
                                        Text(
                                            text = peer.first + (if (isMe) " (You)" else ""),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isMe) FontWeight.ExtraBold else FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "Steady scholar rank",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "${peer.third} mins",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// PROFILE SETUP & VISUAL PERSONALIZATION SCREEN
@Composable
fun ProfileScreen(viewModel: StudySyncViewModel) {
    val profileState by viewModel.profile.collectAsState()
    val totalSecondsAccumulated by viewModel.totalSeconds.collectAsState(initial = 0L)

    var editName by remember { mutableStateOf("") }
    var editSchool by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🦉") }

    val emojis = listOf("🦉", "🎓", "🧠", "🦁", "🐰", "🦊", "🐼", "🦄", "🦅", "🦉", "👓", "🎧")

    // Sync init helper
    LaunchedEffect(profileState) {
        profileState?.let { prof ->
            editName = prof.displayName
            editSchool = prof.schoolName
            selectedEmoji = prof.avatarEmoji
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Personalize your virtual desk partner persona",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Current Avatar Preview Circle with beautiful design background
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedEmoji,
                        fontSize = 54.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select custom desk avatar below", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }

        // Avatar Emojis Grid
        M3Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DESK AVATARS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Selectable Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.take(6).forEach { emo ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == emo) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedEmoji = emo },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emo, fontSize = 24.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.drop(6).take(6).forEach { emo ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == emo) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedEmoji = emo },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emo, fontSize = 24.sp)
                        }
                    }
                }
            }
        }

        // Form Fields
        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Display / Nickname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = editSchool,
            onValueChange = { editSchool = it },
            label = { Text("School / College Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Statistics metric review
        M3Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Time Focused", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = viewModel.formatDuration(totalSecondsAccumulated ?: 0L),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Scholar Streak", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${profileState?.streakDays ?: 0} Days",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFB86C)
                        )
                    )
                }
            }
        }

        // Update profile CTA
        Button(
            onClick = {
                if (editName.isNotBlank()) {
                    viewModel.updateProfile(editName, editSchool, selectedEmoji)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update Study Profile", fontWeight = FontWeight.Bold)
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Advanced Admin reset option
        TextButton(
            onClick = { viewModel.resetActivityStats() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Study Statistics & Goals Data", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
