package org.example.repository

import org.example.model.UserModel

interface UserRepository {

    suspend fun getAll(): List<UserModel>
    suspend fun getById(id: Long): UserModel?
    suspend fun getByIds(ids: Collection<Long>): List<UserModel>
    suspend fun getByUsername(username: String): UserModel?
    suspend fun save(item: UserModel): UserModel

    // USER DATA Похорошему это должна быть отдельная сущность
    suspend fun getUserPostsIds(uId: Long): List<Long>
    suspend fun getUserRepostsIds(uId: Long): List<Long>
    suspend fun getLikesIds(uId: Long): List<Long>
    suspend fun getDislikesIds(uId: Long): List<Long>
    suspend fun getSharedIds(uId: Long): List<Long>

    suspend fun saveUserPost(uId: Long,postId: Long): Long
    suspend fun saveUserRepost(uId: Long,postId: Long): Long
    suspend fun saveLike(uId: Long,postId: Long): Long
    suspend fun saveDislike(uId: Long,postId: Long): Long
    suspend fun saveShared(uId: Long,postId: Long): Long

    suspend fun removeUserPostsById(uId: Long,postId: Long): List<Long>
    suspend fun removeUserRepostsById(uId: Long,postId: Long): List<Long>
    suspend fun removeLikesById(uId: Long,postId: Long): List<Long>
    suspend fun removeDislikesById(uId: Long,postId: Long): List<Long>
    suspend fun removeSharedById(uId: Long,postId: Long): List<Long>

}