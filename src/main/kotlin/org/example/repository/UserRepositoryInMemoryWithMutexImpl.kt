package org.example.repository

import io.ktor.features.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.model.UserModel

class UserRepositoryInMemoryWithMutexImpl : UserRepository {

    private var nextId = 1L
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Long): UserModel? = mutex.withLock {
        items.find { it.id == id }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<UserModel> = mutex.withLock {
        items.filter { ids.contains(it.id) }
    }

    override suspend fun getByUsername(username: String): UserModel? = mutex.withLock {
        items.find { it.username == username }
    }

    override suspend fun save(item: UserModel): UserModel =
        mutex.withLock {
            when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    items[index] = item
                    item
                }
            }
        }


    override suspend fun getLikesIds(uId: Long): List<Long> = mutex.withLock {
        val model = getById(uId) ?: throw NotFoundException()
        model.likedPosts
    }

    override suspend fun getDislikesIds(uId: Long): List<Long> = mutex.withLock {
        val model = getById(uId) ?: throw NotFoundException()
        model.dislikedPosts
    }


    override suspend fun getUserPostsIds(uId: Long): List<Long> = mutex.withLock {
        val model = getById(uId) ?: throw NotFoundException()
        model.createdPosts
    }

    override suspend fun getUserRepostsIds(uId: Long): List<Long> = mutex.withLock {
        val model = getById(uId) ?: throw NotFoundException()
        model.repostedPost
    }

    override suspend fun getSharedIds(uId: Long): List<Long> = mutex.withLock {
        val model = getById(uId) ?: throw NotFoundException()
        model.sharedPosts
    }

    override suspend fun saveUserPost(uId: Long, postId: Long): Long = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.createdPosts.toMutableList()
        list.add(postId)

        items.remove(user)
        items.add(user.copy(createdPosts = list.toList()))

        postId
    }

    override suspend fun saveUserRepost(uId: Long, postId: Long): Long = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.repostedPost.toMutableList()
        list.add(postId)

        items.remove(user)
        items.add(user.copy(repostedPost = list.toList()))

        postId
    }

    override suspend fun saveLike(uId: Long, postId: Long): Long = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.likedPosts.toMutableList()
        list.add(postId)

        items.remove(user)
        items.add(user.copy(likedPosts = list.toList()))

        postId
    }

    override suspend fun saveDislike(uId: Long, postId: Long): Long = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.dislikedPosts.toMutableList()
        list.add(postId)

        items.remove(user)
        items.add(user.copy(dislikedPosts = list.toList()))

        postId
    }

    override suspend fun saveShared(uId: Long, postId: Long): Long = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.sharedPosts.toMutableList()
        list.add(postId)

        items.remove(user)
        items.add(user.copy(sharedPosts = list.toList()))

        postId
    }

    override suspend fun removeUserPostsById(uId: Long, postId: Long): List<Long> = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.createdPosts.toMutableList().apply { remove(postId) }

        items.remove(user)
        items.add(user.copy(createdPosts = list))

        list
    }

    override suspend fun removeUserRepostsById(uId: Long, postId: Long): List<Long> = mutex.withLock {
        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.repostedPost.toMutableList().apply { remove(postId) }

        items.remove(user)
        items.add(user.copy(repostedPost = list))

        list
    }

    override suspend fun removeLikesById(uId: Long, postId: Long): List<Long> = mutex.withLock {

        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.likedPosts.toMutableList().apply { remove(postId) }

        items.remove(user)
        items.add(user.copy(likedPosts = list))

        list
    }

    override suspend fun removeDislikesById(uId: Long, postId: Long): List<Long> = mutex.withLock {

        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.dislikedPosts.toMutableList().apply { remove(postId) }

        items.remove(user)
        items.add(user.copy(dislikedPosts = list))

        list
    }

    override suspend fun removeSharedById(uId: Long, postId: Long): List<Long> = mutex.withLock {

        val user = items.find { it.id == uId } ?: throw NotFoundException()
        val list = user.sharedPosts.toMutableList().apply { remove(postId) }

        items.remove(user)
        items.add(user.copy(sharedPosts = list))

        list
    }
}