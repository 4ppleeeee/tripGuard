package com.tencent.news.core.page.model

interface IStructLoadStateWidget {
    var loading: StructWidget?
    var error: StructWidget?
    var empty: StructWidget?
}
