package dev.kuro9.module.front.discord.app.component.user.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore.*

interface UserInfoStore : Store<Intent, State, Label> {

    sealed class Intent {
        data class SetUserInfo(val user: State.UserInfo) : Intent()
        data object Logout : Intent()
        data object GoLogin : Intent()
    }

    // 로그인 버튼 상태 (로그인 필요 또는 유저네임, 프로필 등)
    data class State(
        val userInfo: UserInfo? = null,
    ) {
        data class UserInfo(
            val id: Long,
            val name: String,
            val avatarUrl: String?,
        )
    }

    // side effect
    sealed class Label {
        data class Redirect(val url: String) : Label()
    }
}