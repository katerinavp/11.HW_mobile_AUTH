package org.example.repository

import io.ktor.features.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.model.PostModel

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private val items = mutableListOf<PostModel>()
    private var nextId = 1L

    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> = mutex.withLock {

        // Увеличиваем счетчик просмотров на всех item-ах
        items.forEachIndexed { index, postModel ->
            items[index] = postModel.copy(countViews = postModel.countViews.inc())
        }

        // Возвращаем перевернутый список
        items.reversed()
    }

    override suspend fun getById(id: Long): PostModel = mutex.withLock {

        val item = items.find { it.id == id } ?: throw NotFoundException()

        // Увеличиваем счетчик.
        val index = items.indexOfFirst { it.id == item.id }
        items[index] = item.copy(
            countViews = items[index].countViews + 1
        )

        return items[index]
    }

    override suspend fun save(item: PostModel): PostModel = mutex.withLock {
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

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(uId: Long, id: Long): PostModel? = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copyItem = item.copy(likedCount = item.likedCount.inc())
                items[index] = copyItem
                copyItem
            }
        }
    }

    override suspend fun dislikeById(uId: Long, id: Long): PostModel? = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copyItem = item.copy(dislikedCount = item.dislikedCount.inc())
                items[index] = copyItem
                copyItem
            }
        }
    }


    override suspend fun repostById(uId: Long, id: Long): PostModel? = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copyItem = item.copy(
                    repostByMe = true,
                    repostCount = item.repostCount.inc()
                )
                items[index] = copyItem
                copyItem
            }
        }
    }

    override suspend fun shareById(uId: Long, id: Long): PostModel? = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copyItem = item.copy(
                    sharedByMe = true,
                    sharedCount = item.repostCount.inc()
                )
                items[index] = copyItem
                copyItem
            }
        }
    }
}