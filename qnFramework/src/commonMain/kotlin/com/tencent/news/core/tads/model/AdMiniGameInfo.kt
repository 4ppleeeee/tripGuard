package com.tencent.news.core.tads.model

import com.tencent.news.core.list.model.BaseKmmModel
import kotlinx.serialization.Serializable

interface IAdMiniGameInfo {

    val miniGameId: String

    val enableInnerOpen: Boolean

    val needLogin: Boolean
}

@Serializable
class AdMiniGameInfo : BaseKmmModel(), IAdMiniGameInfo {

    private var mini_game_id: String = ""
    override val miniGameId: String
        get() = mini_game_id

    private var enable_inner_open: Boolean = false
    override val enableInnerOpen: Boolean
        get() = enable_inner_open

    private var need_login: Boolean = false
    override val needLogin: Boolean
        get() = need_login
}
