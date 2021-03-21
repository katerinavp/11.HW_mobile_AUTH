package org.example.dto.request

data class RepostRequestDto(
    val id: Long = 0,
    val parentPostId: Long,
    val authorId: Long,
    val content: String = ""
)