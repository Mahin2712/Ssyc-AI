package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class StudySyncRepository(private val db: AppDatabase) {
    val profileFlow: Flow<Profile?> = db.profileDao().getProfileFlow()
    val roomsFlow: Flow<List<StudyRoom>> = db.roomDao().getAllRoomsFlow()
    val sessionsFlow: Flow<List<StudySession>> = db.sessionDao().getAllSessionsFlow()
    val activeSessionFlow: Flow<StudySession?> = db.sessionDao().getActiveSessionFlow()
    val todosFlow: Flow<List<Todo>> = db.todoDao().getAllTodosFlow()
    val goalsFlow: Flow<List<DailyGoal>> = db.goalDao().getAllGoalsFlow()
    val totalSecondsFlow: Flow<Long?> = db.sessionDao().getTotalStudyDurationSecondsFlow()

    suspend fun getProfile(): Profile? = db.profileDao().getProfile()
    suspend fun saveProfile(profile: Profile) = db.profileDao().insertProfile(profile)

    suspend fun insertRoom(room: StudyRoom): Long = db.roomDao().insertRoom(room)
    suspend fun updateRoomMemberCount(roomId: Int, count: Int) = db.roomDao().updateRoomMemberCount(roomId, count)
    suspend fun getRoomById(id: Int) = db.roomDao().getRoomById(id)
    suspend fun deleteRoom(room: StudyRoom) = db.roomDao().deleteRoom(room)

    suspend fun getActiveSession() = db.sessionDao().getActiveSession()
    suspend fun insertSession(session: StudySession) = db.sessionDao().insertSession(session)
    suspend fun updateSession(session: StudySession) = db.sessionDao().updateSession(session)

    suspend fun insertTodo(todo: Todo) = db.todoDao().insertTodo(todo)
    suspend fun updateTodo(todo: Todo) = db.todoDao().updateTodo(todo)
    suspend fun deleteTodoById(id: Int) = db.todoDao().deleteTodoById(id)
    suspend fun resetRecurringDailyTodos() = db.todoDao().resetRecurringDailyTodos()

    suspend fun insertGoal(goal: DailyGoal) = db.goalDao().insertGoal(goal)
    suspend fun updateGoal(goal: DailyGoal) = db.goalDao().updateGoal(goal)
    suspend fun deleteGoalById(id: Int) = db.goalDao().deleteGoalById(id)

    suspend fun seedDatabaseIfEmpty() {
        val existingProfile = db.profileDao().getProfile()
        if (existingProfile == null) {
            db.profileDao().insertProfile(
                Profile(
                    displayName = "🦉 Wise Owl",
                    avatarEmoji = "🦉",
                    schoolName = "StudySync Academy"
                )
            )
        }

        val rooms = db.roomDao().getAllRoomsFlow().firstOrNull() ?: emptyList()
        if (rooms.isEmpty()) {
            val defaultRooms = listOf(
                StudyRoom(name = "General Math", subject = "Math", emoji = "📐", isCustom = false, memberCount = 5),
                StudyRoom(name = "Higher Math", subject = "Higher Math", emoji = "📐", isCustom = false, memberCount = 4),
                StudyRoom(name = "Physics", subject = "Physics", emoji = "⚡", isCustom = false, memberCount = 7),
                StudyRoom(name = "Chemistry", subject = "Chemistry", emoji = "🧪", isCustom = false, memberCount = 6),
                StudyRoom(name = "Biology", subject = "Biology", emoji = "🔬", isCustom = false, memberCount = 3),
                StudyRoom(name = "Bangla 1st", subject = "Bangla", emoji = "📖", isCustom = false, memberCount = 4),
                StudyRoom(name = "Bangla 2nd", subject = "Bangla", emoji = "✍️", isCustom = false, memberCount = 2),
                StudyRoom(name = "English 1st", subject = "English", emoji = "🗣️", isCustom = false, memberCount = 5),
                StudyRoom(name = "English 2nd", subject = "English", emoji = "📝", isCustom = false, memberCount = 4),
                StudyRoom(name = "History / BGS", subject = "History", emoji = "🌍", isCustom = false, memberCount = 3),
                StudyRoom(name = "ICT", subject = "ICT", emoji = "💻", isCustom = false, memberCount = 6),
                StudyRoom(name = "Islam", subject = "Islam", emoji = "🕌", isCustom = false, memberCount = 4),
                StudyRoom(name = "Hinduism", subject = "Hinduism", emoji = "🕉️", isCustom = false, memberCount = 2)
            )
            for (r in defaultRooms) {
                db.roomDao().insertRoom(r)
            }
        }
    }
}
