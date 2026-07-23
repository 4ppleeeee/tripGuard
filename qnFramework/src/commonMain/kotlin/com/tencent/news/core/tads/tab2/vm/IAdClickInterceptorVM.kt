package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.tads.click.AdClickRequest

/**
 * 用于在vm层hook广告点击，某些情况下插入点击拦截器不容易实现，比如hook最终跳转h5，openWebPage的调用处有很多，没法使用拦截器拦截
 */
interface IAdClickInterceptorVM {
    var hookJumpH5Action: ((request: AdClickRequest, url: String) -> Boolean)?
}