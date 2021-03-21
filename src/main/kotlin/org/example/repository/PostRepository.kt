package org.example.repository

import org.example.model.PostModel

interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getById(id: Long): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun removeById(id: Long)

    suspend fun likeById(uId: Long, id: Long): PostModel?
    suspend fun dislikeById(uId: Long, id: Long): PostModel?
    suspend fun repostById(uId: Long,id: Long): PostModel?
    suspend fun shareById(uId: Long, id: Long): PostModel?
}