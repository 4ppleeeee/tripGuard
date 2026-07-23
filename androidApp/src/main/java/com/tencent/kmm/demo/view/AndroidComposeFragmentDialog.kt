package com.tencent.kmm.demo.view

import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tencent.news.core.compose.AndroidComposePageDelegate
import com.tencent.news.core.compose.AndroidDialogModule
import com.tencent.news.core.compose.IComposePage
import com.tencent.news.core.compose.IComposePageDelegate
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.emptyPageArgs
import com.tencent.news.core.compose.scaffold.NewsComposeModule

class AndroidComposeFragmentDialog private constructor() : DialogFragment(), IComposePage {

    companion object {

        fun showDialog(
            activity: FragmentActivity,
            pageName: String,
            pageArgs: IComposePageArgs = emptyPageArgs()
        ) {
            AndroidComposeFragmentDialog().apply {
                arguments = Bundle().apply {
                    putString("pageName", pageName)
                    putSerializable("pageArgs", pageArgs)
                }
            }.showNow(activity.supportFragmentManager, "Test")
        }
    }

    override fun onStart() {
        super.onStart()
        updateWindowAttributes()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    private fun updateWindowAttributes() {
        val dialog = dialog ?: return
        val window = dialog.window ?: return
        val wlp = window.attributes
        // 全屏
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp.height = ViewGroup.LayoutParams.MATCH_PARENT
        // 底部
        wlp.gravity = Gravity.BOTTOM
        wlp.dimAmount = 0.0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            wlp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = wlp
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setWindowAnimations(/* resId = */ 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FrameLayout(inflater.context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compose.onCreate(
            view.context.applicationContext as Application,
            view as ViewGroup,
            arguments?.getSerializable("pageArgs") as IComposePageArgs,
            arguments?.getString("pageName") ?: "",
            mapOf(NewsComposeModule.Dialog.moduleName to AndroidDialogModule(this, this::callback))
        )
    }

    private fun callback(fragment: Fragment, method: String, params: Any?) {
        (fragment as? DialogFragment)?.dismissAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        compose.onResume()
    }

    override fun onPause() {
        super.onPause()
        compose.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compose.onDestroy()
    }

    override val compose: IComposePageDelegate = AndroidComposePageDelegate.create()
}
