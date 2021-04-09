package org.example.route

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.example.dto.request.*
import org.example.dto.responce.UserResponseDto
import org.example.model.UserModel
import org.example.service.FileService
import org.example.service.PostService
import org.example.service.UserService

class RoutingV1(
    private val staticPath: String,         // Не очень хорошо что мы передаем констатнту в конструктор, а если их будет 1000.
    private val fileService: FileService,
    private val userService: UserService,
    private val postService: PostService
) {

    // Данный exception часто дублируется
    private val idParameterConversionException = ParameterConversionException("id", "Long")

    fun setup(configuration: Routing) {

        with(configuration) {

            route("/api/v1/") {

                // Настройка отдачи статического контента
                static("/static") { files(staticPath) }

                // AUTH
                post("/registration") {
                    val input = call.receive<RegistrationRequestDto>()
                    val response = userService.register(input)
                    call.respond(response)  // Возвращаем токен
                }

                post("/authentication") {
                    val input = call.receive<AuthenticationRequestDto>()
                    val response = userService.authenticate(input)
                    call.respond(response)  // Возвращаем токен
                }

                // Все роуты (маршруты) в данном блоке будут проверятся на наличие токена
                authenticate {

                    // Загрузка файлов
                    route("/media") {
                        post {
                            val multipart = call.receiveMultipart()
                            val response = fileService.save(multipart)
                            call.respond(response)
                        }
                    }

                    // User
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            call.respond(UserResponseDto.fromModel(me ?: throw Throwable()))
                        }
                    }

                    // Posts
                    route("/posts") {

                        get {
                            val me = call.authentication.principal<UserModel>() ?: throw Throwable()
                            call.respond(postService.getAll(me.id))
                        }

                        post {
                            val me = call.authentication.principal<UserModel>() ?: throw Throwable()
                            val post = call.receive<PostRequestDto>()

                            call.respond(postService.save(post, me.id))
                        }

                        get("/{id}") {
                            val me = call.authentication.principal<UserModel>() ?: throw Throwable()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            call.respond(postService.getById(me.id, id))
                        }

                        delete("/{id}") {
                            val me = call.authentication.principal<UserModel>()

                            val id = call.parameters["id"]?.toLongOrNull() ?: idParameterConversionException

                            // Пользователь может удалять только свои посты
                            if (me?.id == id) {
                                postService.deleteById(id, me)
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.Forbidden)
                            }
                        }

                        post("/{id}/like") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            call.respond(postService.likeById(me!!.id, id))
                        }


                        delete("/{id}/like") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            call.respond(postService.removeLike(me!!.id, id))
                        }

                        post("/{id}/dislike") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            call.respond(postService.dislikeById(me!!.id, id))
                        }

                        delete("/{id}/dislike") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            call.respond(postService.removeDislike(me!!.id, id))
                        }

                        post("/repost") {
                            val me = call.authentication.principal<UserModel>() ?: throw Throwable()
                            val repostRequestDto = call.receive<RepostRequestDto>()
                            call.respond(postService.repostById(repostRequestDto, me))
                        }

                        post("/{id}/share") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw idParameterConversionException
                            val uId = call.authentication.principal<UserModel>()?.id ?: throw Throwable()
                            call.respond(postService.shareById(uId, id))
                        }
                    }
                }

            }
        }
    }
}