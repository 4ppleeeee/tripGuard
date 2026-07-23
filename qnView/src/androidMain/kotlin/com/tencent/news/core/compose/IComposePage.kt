package com.tencent.news.core.compose

import com.tencent.news.core.app.IKmmContext

interface IComposePage : IKmmContext {
    val compose: IComposePageDelegate
}