package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Update
    suspend fun updateProfile(profile: Profile)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY isCustom ASC, id ASC")
    fun getAllRoomsFlow(): Flow<List<StudyRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: StudyRoom): Long

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: Int): StudyRoom?

    @Delete
    suspend fun deleteRoom(room: StudyRoom)

    @Query("UPDATE rooms SET memberCount = :count WHERE id = :id")
    suspend fun updateRoomMemberCount(id: Int, count: Int)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE isActive = 1 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): StudySession?

    @Query("SELECT * FROM study_sessions WHERE isActive = 1 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<StudySession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Update
    suspend fun updateSession(session: StudySession)

    @Query("SELECT SUM(CASE WHEN endTime IS NOT NULL THEN (endTime - startTime) / 1000 ELSE 0 END) FROM study_sessions")
    fun getTotalStudyDurationSecondsFlow(): Flow<Long?>

    @Query("SELECT * FROM study_sessions WHERE startTime >= :startTime")
    fun getSessionsSinceFlow(startTime: Long): Flow<List<StudySession>>
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodosFlow(): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Int)

    @Query("UPDATE todos SET isDone = 0 WHERE isRecurringDaily = 1")
    suspend fun resetRecurringDailyTodos()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoalsFlow(): Flow<List<DailyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoal): Long

    @Update
    suspend fun updateGoal(goal: DailyGoal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)
}
