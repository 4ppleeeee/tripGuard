package com.tencent.news.core.view.extension

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tencent.news.core.app.AndroidContextWrapper
import com.tencent.news.core.list.vm.ClickAction
import com.tencent.news.core.list.vm.IBtnVM
import com.tencent.news.core.list.vm.IClickVM
import com.tencent.news.core.list.vm.IStructBg
import com.tencent.news.core.list.vm.IStructSize
import com.tencent.news.core.list.vm.createValidAction
import com.tencent.news.core.list.vm.runAll
import com.tencent.news.core.page.model.StructBg
import com.tencent.news.core.page.model.StructRect
import com.tencent.news.core.page.model.StructSize
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.view.extension.DpEx.dpToPx
import com.tencent.news.core.view.extension.StructDrawableEx.setStructBgColor
import com.tencent.news.core.view.extension.StructDrawableEx.setStructDrawable
import com.tencent.news.core.view.extension.ViewSizeEx.setHeightPx
import com.tencent.news.core.view.extension.ViewSizeEx.setPaddingDp
import com.tencent.news.core.view.extension.ViewSizeEx.setViewMarginDp
import com.tencent.news.core.view.extension.ViewSizeEx.setWidthPx

object AndroidViewEx4VM {

    @SuppressLint("SetTextI18n")
    fun TextView?.setBtnVM(vm: IBtnVM?) {
        setBtnTextVM(vm)
        setClickVM(vm)
    }

    fun TextView?.setBtnTextVM(vm: IBtnVM?) {
        this ?: return
        setBtnText(vm)
        setSize(vm)
    }

    fun TextView.setBtnText(vm: IBtnVM?) {
        if (vm != null) {
            val btnText = vm.btnText
            val btnTextSelected = vm.btnTextSelected
            if (btnTextSelected.isEmpty()) {
                this.text = btnText
            } else {
                if (this.isSelected) {
                    this.text = btnTextSelected
                } else {
                    this.text = btnText
                }
            }
        } else {
            this.text = ""
        }
    }

    fun View?.setClickVM(vm: IClickVM?, replaceClickAction: ClickAction? = null) {
        this ?: return

        val clickAction = vm.createValidAction(AndroidContextWrapper(context), replaceClickAction)
        if (clickAction != null) {
            setOnClickListener {
                vm.runAll(clickAction)
            }
        } else {
            setOnClickListener(null)
            isClickable = false
        }
    }

    fun View?.setSize(structSize: IStructSize?) {
        this ?: return
        val size = structSize?.size ?: return

        val widthInPx: Int
        val heightInPx: Int

        if (size.width > 0 && size.height > 0) {
            widthInPx = size.width.dpToPx()
            heightInPx = size.height.dpToPx()
        } else if (size.width > 0 && size.aspectRatio > 0) {
            widthInPx = size.width.dpToPx()
            heightInPx = (widthInPx / size.aspectRatio).toInt()
        } else if (size.height > 0 && size.aspectRatio > 0) {
            heightInPx = size.height.dpToPx()
            widthInPx = (heightInPx * size.aspectRatio).toInt()
        } else {
            widthInPx = if (size.width > 0) size.width.dpToPx() else INVALID_NUM
            heightInPx = if (size.height > 0) size.height.dpToPx() else INVALID_NUM
        }

        if (widthInPx > 0) {
            setWidthPx(widthInPx)
        } else if (size.width in setOf(StructSize.MATCH_PARENT, StructSize.WRAP_CONTENT)) {
            setWidthPx(mapStructSizeToAndroid(size.width))
        }
        if (heightInPx > 0) {
            setHeightPx(heightInPx)
        } else if (size.height in setOf(StructSize.MATCH_PARENT, StructSize.WRAP_CONTENT)) {
            setHeightPx(mapStructSizeToAndroid(size.height))
        }
    }

    private fun mapStructSizeToAndroid(structSize: Int): Int {
        return when (structSize) {
            StructSize.MATCH_PARENT -> ViewGroup.LayoutParams.MATCH_PARENT
            StructSize.WRAP_CONTENT -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> structSize
        }
    }

    fun View?.setBg(structBg: IStructBg?) {
        this ?: return
        setBg(structBg?.bg)
    }

    fun View?.setBg(structBg: StructBg?) {
        this ?: return
        val bg = structBg ?: return

        setStructDrawable(bg.drawable)
        setStructBgColor(bg.color)
        setPadding(bg.padding)
        setMargin(bg.margin)
    }

    fun View?.setPadding(structPadding: StructRect?) {
        structPadding ?: return
        setPaddingDp(
            structPadding.left,
            structPadding.top,
            structPadding.right,
            structPadding.bottom
        )
    }

    fun View?.setMargin(structMargin: StructRect?) {
        structMargin ?: return
        setViewMarginDp(
            structMargin.left,
            structMargin.top,
            structMargin.right,
            structMargin.bottom
        )
    }


}