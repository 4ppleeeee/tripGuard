package com.tencent.news.core.tads.model

// 互动状态
data class AdDisplayInteractState(
    val phase: InteractPhase = InteractPhase.IDLE,
    val progress: Float = 0f,
    val hasTriggered: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        val EMPTY = AdDisplayInteractState()
    }
}

// 互动阶段
enum class InteractPhase {
    IDLE, INTERACTING, FORWARD_COMPLETE, SUCCESS, CANCELLED, FAILED
}

// 扭动方向
enum class TwistDirection {
    NONE, LEFT, RIGHT, UP, DOWN
}
