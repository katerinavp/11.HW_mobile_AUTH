package org.example.model

data class RepostModel(
    val id: Long,               // ID для репостов пользователя
    val postId: Long,           // ID поста который репустнули
    val description: String,    // Описание которое может добавить пользователь
    val likeCount: Int = 0,     // Кол-во лайков под репостом
    val dislikeCount: Int = 0,  // Кол-во дизлайков под репостом
    val commentCount: Int = 0,

    val likedByMe: Int = 0      // Оценка пользователя 1 like, -1 dislike, 0 nothing
)