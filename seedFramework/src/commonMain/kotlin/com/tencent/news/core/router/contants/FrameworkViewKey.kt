package com.tencent.news.core.router.contants

object FrameworkViewKey {

    object Debug {
        const val DEMO_PAGE = "/page/demo"                      // 业务体验demo
        const val DEMO_CATEGORY_PAGE = "/page/demo/category"    // demo二级子分类页面
    }

    object Dialog {
        const val NATIVE_BRIDGE = "/dialog/native_bridge"
    }

    object Setting {
        const val DEVELOPER = "page/developer"      // 开发者调试设置页
    }

    object Channel {
        const val ITEM_CELL = "/channel/item_cell"  // 通用cell（item结构）
        const val STRUCT = "/channel/struct"        // 通用频道
    }
}
