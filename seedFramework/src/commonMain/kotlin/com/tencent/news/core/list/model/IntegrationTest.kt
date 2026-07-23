@file:Suppress("PrivatePropertyName", "VariableNaming", "ConstructorParameterNaming")

package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import kotlinx.serialization.Serializable


// 集成测试 mock数据配置
@Serializable
class IntegrationTest : IKmmKeep, ITestDto {

    override var debugErrorInfo: String = ""

    override var desc: String = ""

    private var ad_file: String = ""
    override val adFile: String get() = ad_file

    private var content_file: String = ""
    override val contentFile: String get() = content_file

    private var forbid_tab2: Boolean = false
    override val forbidJumpTab2: Boolean get() = forbid_tab2

    private var no_freq_limit: Boolean = false
    override val noFreqLimit: Boolean get() = no_freq_limit

    private var ignore_all_freq_limit: Boolean = false
    override val ignoreAllFreqLimit: Boolean get() = ignore_all_freq_limit

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(debugErrorInfo)
        dest.writeString(desc)
        dest.writeString(ad_file)
        dest.writeString(content_file)
        dest.writeBoolean(forbid_tab2)
        dest.writeBoolean(no_freq_limit)
        dest.writeBoolean(ignore_all_freq_limit)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        debugErrorInfo = from.readString()
        desc = from.readString()
        ad_file = from.readString()
        content_file = from.readString()
        forbid_tab2 = from.readBoolean()
        no_freq_limit = from.readBoolean()
        ignore_all_freq_limit = from.readBoolean()
    }

}