package com.tencent.news.core.app

import com.tencent.news.qncore.ohos.tns_ad_des_iv
import com.tencent.news.qncore.ohos.tns_ad_des_key
import com.tencent.news.qncore.ohos.tns_http_sign_secret
import com.tencent.news.qncore.ohos.tns_http_sign_url_secret
import com.tencent.news.qncore.ohos.tns_mob_des_iv
import com.tencent.news.qncore.ohos.tns_mob_des_key
import kotlinx.cinterop.toKString

//
// 密钥常量见https://git.woa.com/QQNews_CrossPlatform/QQNewsSecrets
//

object AdSecurityConstants {
    val DES_KEY = tns_ad_des_key()?.toKString().orEmpty()
    val DES_IV = tns_ad_des_iv()?.toKString().orEmpty()

    val MOB_DES_KEY = tns_mob_des_key()?.toKString().orEmpty()
    val MOB_DES_IV = tns_mob_des_iv()?.toKString().orEmpty()
}

object HttpSignSecurityConstants {
    val SECRET = tns_http_sign_secret()?.toKString().orEmpty()
    val URL_SECRET = tns_http_sign_url_secret()?.toKString().orEmpty()
}

object AppId {
    const val QQ_LOGIN = ""

    /**
     * 微信开放平台 AppID，由业务壳按自身包名覆盖。
     */
    const val WX_RELEASE_APP_ID = ""
    const val WX_RDM_APP_ID = ""
}
