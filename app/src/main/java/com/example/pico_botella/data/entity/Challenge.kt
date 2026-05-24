package com.example.pico_botella.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)