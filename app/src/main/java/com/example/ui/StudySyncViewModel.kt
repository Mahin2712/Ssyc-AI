package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StudyPartner(
    val name: String,
    val avatarEmoji: String,
    val subject: String,
    val chapter: String,
    val isStudying: Boolean = true,
    val elapsedMinutes: Int
)

data class ChatItem(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val isSelf: Boolean,
    val messageText: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val reactions: Map<String, Int> = emptyMap()
)

class StudySyncViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = StudySyncRepository(database)

    // UI States
    var currentScreen by mutableStateOf("home") // home, rooms, room_detail, stats, profile
    var isDarkMode by mutableStateOf(true)
    var selectedRoom: StudyRoom? by mutableStateOf(null)

    // Flow bindings
    val profile = repository.profileFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val rooms = repository.roomsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sessions = repository.sessionsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val activeSession = repository.activeSessionFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val todos = repository.todosFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val goals = repository.goalsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val totalSeconds = repository.totalSecondsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // Live Timer & Anti-Cheat
    var elapsedSeconds by mutableStateOf(0L)
    var checkInPromptActive by mutableStateOf(false)
    var checkInSecondsRemaining by mutableStateOf(15)
    var activeSessionStartTime = 0L

    // Live Room Collaboration Simulation
    var currentRoomPartners = mutableStateFlowOf<List<StudyPartner>>(emptyList())
    var currentRoomChat = mutableStateFlowOf<List<ChatItem>>(emptyList())

    private var studyTimerJob: Job? = null
    private var partnerSimulationJob: Job? = null
    private var checkInTimerJob: Job? = null

    // Helper functions for Flow state preservation
    private fun <T> mutableStateFlowOf(initial: T) = MutableStateFlow(initial)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Pull initial active session if app restarts
            val active = repository.getActiveSession()
            if (active != null) {
                resumeTimer(active)
            }
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    // Screen navigation
    fun navigateTo(screen: String) {
        currentScreen = screen
    }

    // Update Profile
    fun updateProfile(name: String, school: String, emoji: String) {
        viewModelScope.launch {
            val current = repository.getProfile() ?: Profile(displayName = "Owl", schoolName = school, avatarEmoji = emoji)
            val updated = current.copy(
                displayName = name,
                schoolName = school,
                avatarEmoji = emoji
            )
            repository.saveProfile(updated)
        }
    }

    // CREATE CUSTOM ROOM
    fun createCustomRoom(name: String, subject: String, emoji: String) {
        viewModelScope.launch {
            val r = StudyRoom(name = name, subject = subject, emoji = emoji, isCustom = true, memberCount = 1)
            repository.insertRoom(r)
        }
    }

    // DELETE CUSTOM ROOM
    fun deleteCustomRoom(room: StudyRoom) {
        viewModelScope.launch {
            repository.deleteRoom(room)
        }
    }

    // JOIN ROOM
    fun joinRoom(room: StudyRoom) {
        selectedRoom = room
        // Reset and mock simulated context
        setupSimulatedRoom(room)
        navigateTo("room_detail")
    }

    // LEAVE ROOM
    fun leaveRoom() {
        // Automatically stop active session if studying inside the room
        stopActiveSession()
        selectedRoom = null
        partnerSimulationJob?.cancel()
        currentRoomPartners.value = emptyList()
        currentRoomChat.value = emptyList()
        navigateTo("rooms")
    }

    // START STUDY SESSION
    fun startStudySession(subject: String, chapter: String, roomId: Int?) {
        viewModelScope.launch {
            // Stop any dangling session first
            stopActiveSession()

            val session = StudySession(
                subject = subject,
                chapter = chapter,
                roomId = roomId,
                isActive = true
            )
            val insertedId = repository.insertSession(session)
            val insertedSession = session.copy(id = insertedId.toInt())
            resumeTimer(insertedSession)

            // Inject starting system message
            appendChatMessage("System", "${profile.value?.displayName ?: "User"} started studying focus topic: $chapter")
        }
    }

    // STOP STUDY SESSION
    fun stopActiveSession() {
        viewModelScope.launch {
            val active = repository.getActiveSession()
            if (active != null) {
                val end = System.currentTimeMillis()
                val updated = active.copy(
                    endTime = end,
                    isActive = false
                )
                repository.updateSession(updated)

                // Calculate duration
                val sessionSeconds = (end - active.startTime) / 1000
                if (sessionSeconds > 0) {
                    val minutesAdded = (sessionSeconds / 60).toInt().coerceAtLeast(1)

                    // Update local study profile summary & totals
                    val prof = repository.getProfile()
                    if (prof != null) {
                        val newTotalSeconds = prof.totalStudySeconds + sessionSeconds
                        // Date logic for streaks
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        var currentStreak = prof.streakDays
                        var lastDate = prof.lastStudyDate

                        if (lastDate != todayStr) {
                            if (lastDate.isEmpty()) {
                                currentStreak = 1
                            } else {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                try {
                                    val lastDateObj = sdf.parse(lastDate)
                                    val todayObj = sdf.parse(todayStr)
                                    val diffMs = todayObj.time - lastDateObj.time
                                    val diffDays = diffMs / (1000 * 60 * 60 * 24)
                                    if (diffDays == 1L) {
                                        currentStreak += 1
                                    } else if (diffDays > 1L) {
                                        currentStreak = 1
                                    }
                                } catch (e: Exception) {
                                    currentStreak = 1
                                }
                            }
                            lastDate = todayStr
                        }

                        val longestStreak = maxOf(prof.longestStreak, currentStreak)
                        repository.saveProfile(
                            prof.copy(
                                totalStudySeconds = newTotalSeconds,
                                streakDays = currentStreak,
                                longestStreak = longestStreak,
                                lastStudyDate = lastDate
                            )
                        )
                    }

                    // Update corresponding Daily Goal
                    val allGoals = goals.value
                    val matchingGoal = allGoals.find { it.subject == active.subject }
                    if (matchingGoal != null) {
                        val updatedGoal = matchingGoal.copy(
                            studiedMinutes = matchingGoal.studiedMinutes + minutesAdded
                        )
                        repository.updateGoal(updatedGoal)
                    }
                }

                appendChatMessage("System", "Finished focus session. Session duration: ${formatDuration(sessionSeconds)}")
            }

            // Cancel any timer jobs
            studyTimerJob?.cancel()
            checkInTimerJob?.cancel()
            checkInPromptActive = false
            elapsedSeconds = 0L
        }
    }

    private fun resumeTimer(session: StudySession) {
        studyTimerJob?.cancel()
        activeSessionStartTime = session.startTime
        elapsedSeconds = (System.currentTimeMillis() - session.startTime) / 1000

        studyTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds = (System.currentTimeMillis() - activeSessionStartTime) / 1000

                // Trigger anti-cheat validation every 60 seconds (for engaging interactive demo feedback)
                if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0L && !checkInPromptActive) {
                    triggerCheckInVerification()
                }
            }
        }
    }

    // Core Anti-Cheat: Trigger user check-in verification
    private fun triggerCheckInVerification() {
        checkInPromptActive = true
        checkInSecondsRemaining = 15
        checkInTimerJob?.cancel()
        checkInTimerJob = viewModelScope.launch {
            while (checkInSecondsRemaining > 0) {
                delay(1000)
                checkInSecondsRemaining--
            }
            // If it reached 0, the user failed to answer the check-in -> auto-terminate focus session
            if (checkInPromptActive) {
                autoPenaltyStop()
            }
        }
    }

    // Confirm that the student is active at their desk/table
    fun confirmUserActivity() {
        checkInPromptActive = false
        checkInTimerJob?.cancel()
        checkInSecondsRemaining = 15
        viewModelScope.launch {
            val pr = profile.value?.displayName ?: "🦉 Owl"
            appendChatMessage("System", "Verified $pr is actively focused at the table. Activity recorded.")
        }
    }

    private suspend fun autoPenaltyStop() {
        val active = repository.getActiveSession()
        if (active != null) {
            val penaltySession = active.copy(
                endTime = System.currentTimeMillis(),
                isActive = false,
                missedCheckins = active.missedCheckins + 1
            )
            repository.updateSession(penaltySession)
            appendChatMessage("System", "⚠️ Alert: Session paused. User missed check-in validation.")
        }
        studyTimerJob?.cancel()
        checkInTimerJob?.cancel()
        checkInPromptActive = false
        elapsedSeconds = 0L
    }

    // TODO ACTIONS
    fun addTodoItem(title: String, recurring: Boolean) {
        viewModelScope.launch {
            val todo = Todo(title = title, isRecurringDaily = recurring)
            repository.insertTodo(todo)
        }
    }

    fun toggleTodoState(todo: Todo) {
        viewModelScope.launch {
            val updated = todo.copy(isDone = !todo.isDone)
            repository.updateTodo(updated)
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            repository.deleteTodoById(id)
        }
    }

    // GOAL ACTIONS
    fun addGoalItem(subject: String, targetHours: Int) {
        viewModelScope.launch {
            val goal = DailyGoal(subject = subject, targetMinutes = targetHours * 60)
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }

    // Clean study history
    fun resetActivityStats() {
        viewModelScope.launch {
            // Delete historic sessions, goals
            // Reset profile accumulated seconds to zero
            val p = repository.getProfile()
            if (p != null) {
                repository.saveProfile(
                    p.copy(
                        totalStudySeconds = 0,
                        streakDays = 0,
                        longestStreak = 0,
                        lastStudyDate = ""
                    )
                )
            }
        }
    }

    // Dynamic Live Room Simulated Partners
    private fun setupSimulatedRoom(room: StudyRoom) {
        partnerSimulationJob?.cancel()

        // Preseed simulated chat greetings from the active study room partners
        val defaultChat = listOf(
            ChatItem(senderName = "Maliha Chowdhury", isSelf = false, messageText = "Welcome to ${room.name} study table! Is anyone starting General Math Chapter 3 today?"),
            ChatItem(senderName = "Nafis Tahmid", isSelf = false, messageText = "Yeah! Just started my timer, let's keep focused, team. No distractions!"),
            ChatItem(senderName = "Zubayer Hossain", isSelf = false, messageText = "Joining too. Best study circle online. Let's hit our goals!")
        )
        currentRoomChat.value = defaultChat

        // Preseed simulated study members sitting at the virtual desk
        val chapters = ChapterMap.chaptersMap[room.name] ?: listOf("Chapter 1", "Chapter 2")
        val defaultPartners = listOf(
            StudyPartner(
                name = "Maliha Chowdhury",
                avatarEmoji = "👩‍🎓",
                subject = room.name,
                chapter = chapters.getOrNull(0) ?: "Chapter 1",
                elapsedMinutes = 35
            ),
            StudyPartner(
                name = "Nafis Tahmid",
                avatarEmoji = "🧑‍💻",
                subject = room.name,
                chapter = chapters.getOrNull(2) ?: "Chapter 3",
                elapsedMinutes = 12
            ),
            StudyPartner(
                name = "Zubayer Hossain",
                avatarEmoji = "✍️",
                subject = room.name,
                chapter = chapters.getOrNull(1) ?: "Chapter 2",
                elapsedMinutes = 54
            )
        )
        currentRoomPartners.value = defaultPartners

        // Launch real-time simulation logic
        partnerSimulationJob = viewModelScope.launch {
            val messages = listOf(
                "Nafis Tahmid" to "I'm on a 2-hour streak! Feel so productive here.",
                "Maliha Chowdhury" to "Just finished real-time check-in! Keep up the focus.",
                "Zubayer Hossain" to "The questions in Chapter 2 are starting to click.",
                "Nafis Tahmid" to "Studying with everyone virtually is so cozy and motivating.",
                "Maliha Chowdhury" to "Perfect, done with my daily Math focus quota!",
                "Anika Rahman" to "Joined the table! Let's hit the books hard."
            )

            var index = 0
            while (true) {
                delay(25000) // Message every 25 seconds of room detail being open
                if (index < messages.size) {
                    val msg = messages[index]
                    appendChatMessage(msg.first, msg.second)
                    index++
                } else {
                    index = 0 // loop messages
                }

                // Randomly scale simulated member study times
                currentRoomPartners.value = currentRoomPartners.value.map {
                    it.copy(elapsedMinutes = it.elapsedMinutes + 1)
                }
            }
        }
    }

    // Append standard or user chat
    fun sendUserChatMessage(text: String) {
        val userName = profile.value?.displayName ?: "User"
        appendChatMessage(userName, text, isSelf = true)

        // Make random simulated reply after short typing animation
        viewModelScope.launch {
            delay(1500)
            val partners = currentRoomPartners.value
            if (partners.isNotEmpty()) {
                val randomPartner = partners.random()
                val replies = listOf(
                    "Great point!",
                    "Let's focus up, back to studying!",
                    "Got my focus on. Thanks for sharing!",
                    "Awesome, we are making incredible progress",
                    "Let me check that chapter too after this session concludes."
                )
                appendChatMessage(randomPartner.name, replies.random(), isSelf = false)
            }
        }
    }

    private fun appendChatMessage(sender: String, text: String, isSelf: Boolean = false) {
        val current = currentRoomChat.value.toMutableList()
        current.add(ChatItem(senderName = sender, isSelf = isSelf, messageText = text))
        currentRoomChat.value = current
    }

    // Reaction mechanism
    fun addReactionToMessage(itemId: String, reaction: String) {
        val current = currentRoomChat.value.map { item ->
            if (item.id == itemId) {
                val currentReactions = item.reactions.toMutableMap()
                val previousVal = currentReactions[reaction] ?: 0
                currentReactions[reaction] = previousVal + 1
                item.copy(reactions = currentReactions)
            } else {
                item
            }
        }
        currentRoomChat.value = current
    }

    // Utility formatting for display
    fun formatDuration(totalSeconds: Long): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}
