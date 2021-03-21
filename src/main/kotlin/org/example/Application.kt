package org.example

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.server.cio.EngineMain
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.runBlocking
import org.example.repository.PostRepository
import org.example.repository.PostRepositoryInMemoryWithMutexImpl
import org.example.repository.UserRepository
import org.example.repository.UserRepositoryInMemoryWithMutexImpl
import org.example.route.RoutingV1
import org.example.service.FileService
import org.example.service.JWTTokenService
import org.example.service.PostService
import org.example.service.UserService
import org.kodein.di.generic.*
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.naming.ConfigurationException

private const val UPLOAD_DIR = "user.upload.dir"

fun main(args: Array<String>): Unit = EngineMain.main(args) // Старт движка отчечающего за работу

// Тут конфигурируется наш сервер. Для конфигурации Ktor использует фичи.
fun Application.module() {

    // Включаем логирование
    install(CallLogging)

    // Механизм, позволяющий автоматически преобразовывать контент
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting() // Включаем форматирование
            serializeNulls()    // Серилизуем поля, по умолчанию gson пропускает данные поля
        }
    }

    // Обрабатывам ошибки
    install(StatusPages) {
        exception<NotImplementedError> {
            call.respond(HttpStatusCode.NotImplemented)
            throw it
        }

        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
            throw it
        }

        // NotFoundException (ошибка 404)
        exception<NotFoundException> {
            call.respond(HttpStatusCode.NotFound)
            throw it
        }

        // ParameterConversionException (ошибка 400)
        exception<ParameterConversionException> {
            call.respond(HttpStatusCode.BadRequest)
            throw it
        }
    }

    // Внедряем DI
    install(KodeinFeature) {

        // Kodein позволяет предоставлять константы для DI след образом
        constant(UPLOAD_DIR) with (environment.config.propertyOrNull(UPLOAD_DIR)?.getString()
            ?: throw ConfigurationException("Upload dir is not specified"))

        // Repository
        bind<PostRepository>() with singleton { PostRepositoryInMemoryWithMutexImpl() }

        // eagerSingleton позволяет инстанциировать объект"не лениво", а
        // сразу при старте. Таким образом, мы упадём, если что-то пойдёт не так.
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithMutexImpl() }

        // Services
        bind<PostService>() with eagerSingleton { PostService(postRepo = instance(),userRepo = instance() ) }
        bind<FileService>() with eagerSingleton { FileService(uploadPath = instance(UPLOAD_DIR)) }
        bind<UserService>() with eagerSingleton {
            UserService(
                repo = instance(),
                tokenService = instance(),
                passwordEncoder = instance()
            ).apply { addTestUser() }
        }

        // Encoder
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService() }
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }

        // Вместо instance Kodein подставит объект нужного типа. Это позволит легко сделать независимыми компоненты
        // от Kodein (они, фактически, ничего ни о каком DI не знают)
        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                staticPath = instance(UPLOAD_DIR),
                fileService = instance(),
                userService = instance(),
                postService = instance()
            )
        }
    }

    // Механизм авторизации
    install(Authentication) {
        jwt {
            // Получаем инстансы сервисов
            val jwtService by kodein().instance<JWTTokenService>()
            val userService by kodein().instance<UserService>()

            // Проверяем формат и подпись токена
            verifier(jwtService.verifier)

            // Валидируем пользователя. Чекаем ID
            validate {
                val id = it.payload.getClaim("id").asLong()
                userService.getModelById(id)
            }
        }
    }

    // Роутинг | дерево URL
    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }

}

// Создаем пользователя для проверки
private fun UserService.addTestUser() {
    runBlocking {
        saveNewModel(username = "Test", password = "qwerty")
    }
}
