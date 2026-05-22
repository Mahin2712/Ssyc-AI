package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val displayName: String,
    val avatarEmoji: String = "🎓",
    val schoolName: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val totalStudySeconds: Long = 0,
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String = "" // YYYY-MM-DD
)

@Entity(tableName = "rooms")
data class StudyRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String,
    val emoji: String,
    val isCustom: Boolean = false,
    val memberCount: Int = 0
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val chapter: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isActive: Boolean,
    val roomId: Int? = null,
    val missedCheckins: Int = 0
)

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isDone: Boolean = false,
    val isRecurringDaily: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class DailyGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val targetMinutes: Int,
    val studiedMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
