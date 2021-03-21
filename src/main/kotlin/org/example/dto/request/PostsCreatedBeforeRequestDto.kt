package org.example.dto.request

data class PostsCreatedBeforeRequestDto(
    val idCurPost: Long,
    val countUploadedPosts: Int
)