package com.tencent.kmm.demo

/**
 * 横屏 Kuikly 页面容器。
 *
 * AndroidManifest 中为该 Activity 声明 sensorLandscape，让横屏播放页在 Window 创建阶段
 * 就进入横屏尺寸，避免通用 KuiklyRenderActivity 先按 portrait 创建内容后再旋转。
 */
class LandscapeKuiklyRenderActivity : KuiklyRenderActivity()
