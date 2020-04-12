package io.github.lexcao.konverterdemo

import konverter.Konvert
import konverter.Konvertable
import konverter.To
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class KonverterDemoApplication

fun main(args: Array<String>) {
    runApplication<KonverterDemoApplication>(*args)
}

@RequestMapping("/users")
@RestController
class UserController {

    private val store: ConcurrentHashMap<String, UserEntity> = ConcurrentHashMap()

    init {
        store["admin"] = UserEntity(1, "admin", "admin", 0)
    }

    @PostMapping("/register")
    fun register(
        @RequestBody user: RegisterDTO
    ) {
        store[user.username]?.run {
            throw IllegalStateException("SameUsername")
        }

        val nextId = store.values.size.toLong()
        store[user.username] = user.toUserEntity(id = nextId)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody user: LoginDTO
    ) {
        val found = store[user.username]
            ?: throw IllegalStateException("UserNotFound")
        if (invalidPassword(user.password, found.password)) {
            throw IllegalArgumentException("WrongPassword")
        }
        // TODO Success Login...
    }

    @GetMapping
    fun listUsers(): List<UserVO> = store.values.map { it.toUserVO() }
}

@Konvertable(
    To(name = "LoginDTO", pick = ["username", "password"]),
    To(name = "UserListDTO", omit = ["password"]),
    To(name = "RegisterDTO", omit = ["id"])
)
@Konvert(to = UserVO::class)
data class UserEntity(
    val id: Long,
    @Konvert.Field("name")
    val username: String,
    val password: String,
    @Konvert.By(GenderEnumConverter::class)
    val gender: Int
)

data class UserVO(
    val id: String,
    val name: String,
    val gender: GenderEnum
)

enum class GenderEnum {
    MALE, FEMALE;
}

object GenderEnumConverter : Konvert.KonvertBy<Int, GenderEnum> {
    override fun Int.forward(): GenderEnum {
        return GenderEnum.values()[this]
    }

    override fun GenderEnum.backward(): Int {
        return this.ordinal
    }
}

fun invalidPassword(input: String, hashed: String): Boolean {
    fun cipher(password: String): String {
        // TODO cipher or hash password with MD5 or SHA256...
        return password
    }
    return cipher(input) != hashed
}
