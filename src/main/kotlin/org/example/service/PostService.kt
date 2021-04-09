package org.example.service

import io.ktor.features.*
import org.example.dto.request.PostRequestDto
import org.example.dto.request.RepostRequestDto
import org.example.dto.responce.PostResponseDto
import org.example.dto.responce.PostShareResponseDto
import org.example.exception.ForbiddenException
import org.example.model.PostModel
import org.example.model.UserModel
import org.example.repository.PostRepository
import org.example.repository.UserRepository
import java.sql.SQLInvalidAuthorizationSpecException

class PostService(
    private val postRepo: PostRepository,
    private val userRepo: UserRepository
) {

    suspend fun getAll(uId: Long): List<PostResponseDto> {
        return postRepo.getAll().map { PostResponseDto.fromModel(it.castFromUserData(uId)) }
    }

    suspend fun getById(uId: Long, id: Long): PostResponseDto {

        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        val model = postRepo.getById(id) ?: throw NotFoundException()
        // В ответе формируем лист с пользовательскими лайками/дизами/репостами и т.п.
        return PostResponseDto.fromModel(model.castFromUserData(user.id))
    }

    // Сохранение / изменение
//    suspend fun save(request: PostRequestDto, user: UserModel): PostResponseDto {
    suspend fun save(request: PostRequestDto, id: Long): PostResponseDto {
        // Чекаем создан ли Post пользователем.
//        if (request.authorId != user.id) {
//            throw ForbiddenException("Access deny!")
//        }
//
//        // Сохраняем в репозиторий
//        val model = PostRequestDto.toModel(request)
//        postRepo.save(model)
//
//        // Тащим из БД модель
//        val lastPost = postRepo.getAll().size.toLong()
//
//        userRepo.saveUserPost(model.authorId, lastPost)
//
//        return PostResponseDto.fromModel(model)
//    }

        if (request.id != 0L) {
            // concurrency issues ignored
            val existing = postRepo.getById(request.id)!!
            if (existing.authorId != id) {
                throw SQLInvalidAuthorizationSpecException()
            }
        }

        // Сохраняем в репозиторий
        val model = PostRequestDto.toModel(request)
        postRepo.save(model)
//
//        // Тащим из БД модель
        val lastPost = postRepo.getAll().size.toLong()

        userRepo.saveUserPost(model.authorId, lastPost)

        return PostResponseDto.fromModel(model)



    }


//        val model = PostModel(
//                id = request.id,
//                authorId = myId,
//                content = request.content
//            )
//            link = input.link,
//            attachment = input.attachmentId?.let {
//                AttachmentModel(input.attachmentId, mediaType = MediaType.IMAGE)
//            })

//        if (input.id != 0L) {
//            // concurrency issues ignored
//            val existing = repo.getById(input.id)!!
//            if (existing.ownerId != myId) {
//                throw InvalidOwnerException()
//            }
//        }
//
//        val post = repo.save(model)
//        val owners = listOf(userService.getById(myId))
//        val postDto = mapToPostDto(post, null, owners, myId)
//
//        return postDto
//    }

    suspend fun deleteById(id: Long, user: UserModel): PostResponseDto {

        // Ищем Post
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем создан ли post пользователем. По умочанию удалять может только пользователь создавший пост.
        if (model.authorId != user.id) {
            throw ForbiddenException("Access deny!")
        }
        postRepo.removeById(id)
        return PostResponseDto.fromModel(model.castFromUserData(model.id))
    }

    suspend fun likeById(uId: Long, id: Long): PostResponseDto {

        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        // Находим модель
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем лайки юзера
        val userLikedPosts: List<Long> = user.likedPosts

        val copyModel: PostModel
        // Лайкал ли он хотябы раз
        if (userLikedPosts.isNullOrEmpty()) {
            // Сохраняем лайк в репозиторий пользователя
            copyModel = model.copy(likedByMe = 1, likedCount = model.likedCount.inc())
            userRepo.saveLike(uId, id)
        } else {
            // Ищем среди лакнутых постов наш
            if (userLikedPosts.first { id == it } != 0L) {
                // Снимаем лайк
                copyModel = model.copy(likedByMe = 0, likedCount = model.likedCount.dec())
                userRepo.removeLikesById(uId, id)

            } else {
                // Сохраняем лайк
                copyModel = model.copy(likedByMe = 1, likedCount = model.likedCount.inc())
                userRepo.saveLike(uId, id)
            }

        }

        // Сохраняем лайк в репозиторий
        postRepo.save(copyModel)
        return PostResponseDto.fromModel(copyModel.castFromUserData(model.id))
    }


    suspend fun removeLike(uId: Long, id: Long): PostResponseDto {
        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        // Находим модель
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем лайки юзера
        val userLikedPosts: List<Long> = user.likedPosts

        var copyModel: PostModel = model.copy()

        // Ищем среди лакнутых постов наш
        if (userLikedPosts.first { id == it } != 0L) {
            // Снимаем лайк
            copyModel = model.copy(likedByMe = 0, likedCount = model.likedCount.dec())
            userRepo.removeLikesById(uId, id)
        }

        // Сохраняем лайк в репозиторий
        postRepo.save(copyModel)
        return PostResponseDto.fromModel(copyModel.castFromUserData(model.id))
    }


    suspend fun dislikeById(uId: Long, id: Long): PostResponseDto {
        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        // Находим модель
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем лайки юзера
        val userLikedPosts: List<Long> = user.dislikedPosts

        val copyModel: PostModel
        // Лайкал ли он хотябы раз
        if (userLikedPosts.isNullOrEmpty()) {
            // Сохраняем лайк в репозиторий пользователя
            copyModel = model.copy(likedByMe = -1, dislikedCount = model.dislikedCount.inc())
            userRepo.saveDislike(uId, id)
        } else {
            // Ищем среди лакнутых постов наш
            if (userLikedPosts.first { id == it } != 0L) {
                // Снимаем лайк
                copyModel = model.copy(likedByMe = 0, dislikedCount = model.dislikedCount.dec())
                userRepo.removeDislikesById(uId, id)

            } else {
                // Сохраняем лайк
                copyModel = model.copy(likedByMe = -1, dislikedCount = model.dislikedCount.inc())
                userRepo.saveDislike(uId, id)
            }
        }

        // Сохраняем лайк в репозиторий
        postRepo.save(copyModel)
        return PostResponseDto.fromModel(copyModel)
    }


    suspend fun removeDislike(uId: Long, id: Long): PostResponseDto {
        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        // Находим модель
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем лайки юзера
        val userLikedPosts: List<Long> = user.dislikedPosts

        var copyModel: PostModel = model.copy()

        // Ищем среди лакнутых постов наш
        if (userLikedPosts.first { id == it } != 0L) {
            // Снимаем дизлайк
            copyModel = model.copy(likedByMe = 0, likedCount = model.dislikedCount.dec())
            userRepo.removeLikesById(uId, id)
        }

        // Сохраняем лайк в репозиторий
        postRepo.save(copyModel)
        return PostResponseDto.fromModel(copyModel.castFromUserData(model.id))
    }


    suspend fun repostById(request: RepostRequestDto, user1: UserModel): PostResponseDto {

        // находим юзера
        val user = userRepo.getById(user1.id) ?: throw NotFoundException()

        // Находим модель
        val post = postRepo.getById(request.parentPostId) ?: throw NotFoundException()

        val userReposts: List<Long> = user.repostedPost
        val copyModel: PostModel

        if (userReposts.isNullOrEmpty()) {
            // Сохраняем в репозиторий пользователя
            copyModel = post.copy(repostByMe = true, repostCount = post.repostCount.inc())
            userRepo.saveUserRepost(request.authorId, request.parentPostId)
        } else {
            if (userReposts.first { request.parentPostId == it } != 0L) {
                // Снимаем
                copyModel = post.copy(repostByMe = false, repostCount = post.repostCount.dec())
                userRepo.removeUserRepostsById(request.authorId, request.parentPostId)

            } else {
                // Сохраняем
                copyModel = post.copy(repostByMe = true, repostCount = post.repostCount.inc())
                userRepo.saveUserRepost(request.authorId, request.parentPostId)
            }
        }

        println(copyModel)

        // Сохраняем в репозиторий
        postRepo.save(copyModel)

        return PostResponseDto.fromModel(copyModel)
    }


    suspend fun shareById(uId: Long, id: Long): PostShareResponseDto {
        val user = userRepo.getById(uId) ?: throw NotFoundException()
        val model = postRepo.getById(id) ?: throw NotFoundException()

        // Чекаем лайки юзера
        val userSharePosts: List<Long> = user.sharedPosts
        val copyModel: PostModel

        if (userSharePosts.isNullOrEmpty()) {
            // Сохраняем в репозиторий пользователя
            copyModel = model.copy(sharedByMe = true, sharedCount = model.sharedCount.inc())
            userRepo.saveShared(uId, id)
        } else {
            if (userSharePosts.first { id == it } != 0L) {
                // Снимаем
                copyModel = model.copy(sharedByMe = false, sharedCount = model.sharedCount.dec())
                userRepo.removeSharedById(uId, id)

            } else {
                // Сохраняем
                copyModel = model.copy(sharedByMe = true, sharedCount = model.sharedCount.inc())
                userRepo.saveShared(uId, id)
            }
        }

        // Сохраняем лайк в репозиторий
        postRepo.save(copyModel)

        return PostShareResponseDto("http://127.0.0.1:8080/api/v1/posts/$id")
    }

    private suspend fun PostModel.castFromUserData(uId: Long): PostModel {

        // находим юзера
        val user = userRepo.getById(uId) ?: throw NotFoundException()

        val isLikeByMe = when {
            user.likedPosts.find { this.id == it } ?: 0 > 0 -> 1
            user.dislikedPosts.find { this.id == it } ?: 0 > 0 -> -1
            else -> 0
        }

        val isRepostByMe = when {
            user.repostedPost.find { this.id == it } ?: 0 > 0 -> true
            else -> false
        }

        val isSharedByMe = when {
            user.sharedPosts.find { this.id == it } ?: 0 > 0 -> true
            else -> false
        }

        return this.copy(
            likedByMe = isLikeByMe,
            repostByMe = isRepostByMe,
            sharedByMe = isSharedByMe
        )
    }

}