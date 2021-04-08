package org.example.dto.request

import org.example.model.EventModel
import org.example.model.PostModel
import org.example.model.PostType
import org.example.model.VideoModel

data class PostRequestDto(
    val id: Long,
    val authorId: Long? = null,
    val content: String? = null,
//    val created: Long,
//    val imageUrl: String,
//    val likedCount: Int = 0,
//    val dislikedCount: Int = 0,
//    val likedByMe: Int = 0,             // 1 like, -1 dislike, 0 nothing
//    val repostCount: Int = 0,
//    val repostByMe: Boolean = false,
//    val sharedCount: Int = 0,
//    val sharedByMe: Boolean = false,
//    val event: EventModel? = null,       // Событие подразумевает адрес и координаты
//    val video: VideoModel? = null,       // Видеоконтент
//    val postType: PostType = PostType.POST,
//    val countViews: Int = 0,
)
//{
//    companion object {
//        fun toModel(dto: PostRequestDto) = PostModel(
//            id = dto.id,
//            authorId = dto.authorId,
//            content = dto.content,
//            imageUrl = dto.imageUrl,
//            created = dto.created,
//            likedCount = dto.likedCount,
//            dislikedCount = dto.dislikedCount,
//            likedByMe = dto.likedByMe,
//            sharedCount = dto.sharedCount,
//            sharedByMe = dto.sharedByMe,
//            event = dto.event,
//            video = dto.video,
//            postType = dto.postType,
//            countViews = dto.countViews
//        )
//    }
//}