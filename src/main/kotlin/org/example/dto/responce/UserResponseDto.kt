package org.example.dto.responce

import org.example.model.UserModel

class UserResponseDto(
    val id: Long,
    val username: String,
    val createdPosts: List<Long> = listOf(),
    val likedPosts: List<Long> = listOf(),
    val dislikedPosts: List<Long> = listOf(),
    val repostedPost: List<Long> = listOf(),
    val sharedPosts: List<Long> = listOf(),
) {
    companion object {
        fun fromModel(model: UserModel) = UserResponseDto(
            id = model.id,
            username = model.username,
            createdPosts = model.createdPosts,
            likedPosts = model.likedPosts,
            dislikedPosts = model.dislikedPosts,
            repostedPost = model.repostedPost,
            sharedPosts = model.sharedPosts,
        )
    }
}