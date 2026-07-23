package com.tencent.news.core.view.extension

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.tencent.news.core.extension.KColor
import com.tencent.news.core.page.model.StructColor
import com.tencent.news.core.page.model.StructCorner
import com.tencent.news.core.page.model.StructDrawable
import com.tencent.news.core.view.extension.DpEx.dpToPx
import com.tencent.news.core.view.extension.ViewSkinEx.setSkinBackgroundColor
import com.tencent.news.core.view.extension.ViewSkinEx.setSkinBackgroundDrawable

internal object StructDrawableEx {

    fun View?.setStructBgColor(color: StructColor?) {
        this ?: return
        color ?: return

        setSkinBackgroundColor(
            KColor.toColorInt(color.dayColor),
            KColor.toColorInt(color.nightColor)
        )
    }

    fun View?.setStructDrawable(drawable: StructDrawable?) {
        this ?: return
        drawable ?: return

        setSkinBackgroundDrawable(
            createBgDrawable(KColor.toColorInt(drawable.color.dayColor), drawable.corner),
            createBgDrawable(KColor.toColorInt(drawable.color.nightColor), drawable.corner)
        )
    }

    fun createBgDrawable(color: Int, corner: StructCorner?): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            applyStructCorner(corner)
        }
    }

    private fun GradientDrawable.applyStructCorner(corner: StructCorner?) {
        corner ?: return
        val leftTopPx = checkCornerPx(corner.leftTop).toFloat()
        val rightTopPx = checkCornerPx(corner.rightTop).toFloat()
        val rightBottomPx = checkCornerPx(corner.rightBottom).toFloat()
        val leftBottomPx = checkCornerPx(corner.leftBottom).toFloat()

        cornerRadii = floatArrayOf(
            leftTopPx, leftTopPx,
            rightTopPx, rightTopPx,
            rightBottomPx, rightBottomPx,
            leftBottomPx, leftBottomPx
        )
    }

    private fun checkCornerPx(cornerInDp: Int): Int {
        return if (cornerInDp == StructCorner.ROUND) {
            1000.dpToPx() // 用一个足够大的数，撑满圆角
        } else {
            cornerInDp.dpToPx()
        }
    }

}