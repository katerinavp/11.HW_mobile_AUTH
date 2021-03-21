package org.example.model

data class PostModel(

    val id: Long,
    val authorId: Long,
    val content: String,
    val created: Long,
    val imageUrl: String?,               // Прикрепленное изображение

    // Likes
    val likedCount: Int = 0,
    val dislikedCount: Int = 0,
    val likedByMe: Int = 0,             // 1 like, -1 dislike, 0 nothing

    // Reposts
    val repostCount: Int = 0,
    val repostByMe: Boolean = false,

    // Share
    val sharedCount: Int = 0,
    val sharedByMe: Boolean = false,

    // Прочие события
    val event: EventModel? = null,       // Событие подразумевает адрес и координаты
    val video: VideoModel? = null,       // Видеоконтент

    val postType: PostType = PostType.POST,
    val countViews: Int = 0,
)
