package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_math_history")
data class AiMathHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val problemText: String,
    val topic: String,
    val finalAnswer: String,
    val solutionJson: String,
    val hasAttachment: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
