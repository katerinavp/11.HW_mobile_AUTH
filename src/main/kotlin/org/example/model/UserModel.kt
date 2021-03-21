package org.example.model

import io.ktor.auth.Principal

data class UserModel(
    val id: Long = 0,
    val username: String,
    val password: String,

    // Так делать не хорошо, нужно создавать отдельную таблицу дял каждой сущности.
    val createdPosts: List<Long> = listOf(),
    val likedPosts: List<Long> = listOf(),
    val dislikedPosts: List<Long> = listOf(),
    val repostedPost: List<Long> = listOf(),
    val sharedPosts: List<Long> = listOf()

) : Principal