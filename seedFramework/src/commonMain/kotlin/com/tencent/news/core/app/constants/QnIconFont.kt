package com.tencent.news.core.app.constants


// qnView 组件用到的IconFont，在线预览：
// https://remote-build.woa.com/legend/fileupload/iconfont_new/demo.html
enum class QnIconFont(override val code: String) : IIconFont {

    BACKR_REGULAR("ws_back_regular"),   // 返回键

    // 分享弹窗：
    SHARE_REGULAR("ws_share_regular"),  // 分享
    SCREENSHOT("xwcrop"),               // 截图分享
    DOWNLOAD("ws_download_regular"),    // 下载
    COPY_LINK("ws_link_regular"),       // 复制链接

    // 确认弹窗：
    XW_DUIGOU("ws_mark_regular"),       // ✔️对钩
    XW_GOU_24("ws_gouxuan_face"),       // 实心-选中
    XW_DAN_XUAN("ws_nogouxuan"),        // 空心-未选中

}