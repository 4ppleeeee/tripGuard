package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmDtoParcelable
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.parcel.safeRead
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 直播信息 KMM 实现类
 * 跨平台直播数据模型
 */
@Serializable
class KmmLiveInfo : IKmmKeep, IKmmDtoParcelable {

    // 原始字段定义，与 LiveInfo 保持一致（服务端定义，不能修改）
    internal var live_status: Int = 0
    internal var room_id: String? = null
    internal var start_time: Long = 0L
    internal var end_time: Long = 0L
    internal var screenType: Int = 0
    internal var online_total: Long = 0L
    internal var desc: String? = null
    internal var up_num: Long = 0L
    internal var mid: String? = null
    internal var is_orderLive: String? = null
    internal var hv_direction: Int = 0
    internal var stream_id: String? = null
    internal var program_id: String? = null
    internal var raceInfo: RoseRaceInfo? = null
    internal var live_source: Int = 0
    internal var ysp_living_url: String? = null

    // 包装一层，屏蔽序列化问题（循环引用）
    @Transient
    @kotlin.jvm.Transient
    private var _dtoHolder: TransientLiveInfoDtoHolder? = null
    private val dtoHolder: TransientLiveInfoDtoHolder
        get() = _dtoHolder ?: TransientLiveInfoDtoHolder(info = this).apply { _dtoHolder = this }

    // 对外暴露的 getter/setter 方法，通过 dto 操作
    fun getLive_status(): Int = dtoHolder.dto.liveStatus
    fun setLive_status(value: Int) {
        dtoHolder.dto.liveStatus = value
    }

    fun getStreamId(): String? = dtoHolder.dto.streamId

    fun getRoomId(): String? = dtoHolder.dto.roomId
    fun setRoomId(value: String?) {
        dtoHolder.dto.roomId = value
    }

    fun getStartTime(): Long = dtoHolder.dto.startTime
    fun setStartTime(value: Long) {
        dtoHolder.dto.startTime = value
    }

    fun getEndTime(): Long = dtoHolder.dto.endTime
    fun setEndTime(value: Long) {
        dtoHolder.dto.endTime = value
    }

    fun getScreenType(): Int = dtoHolder.dto.screenType
    fun setScreenType(value: Int) {
        dtoHolder.dto.screenType = value
    }

    fun getOnline_total(): Long = dtoHolder.dto.onlineTotal
    fun setOnline_total(value: Long) {
        dtoHolder.dto.onlineTotal = value
    }

    fun getDesc(): String? = dtoHolder.dto.desc
    fun setDesc(value: String?) {
        dtoHolder.dto.desc = value
    }

    fun getUpNum(): Long = dtoHolder.dto.upNum
    fun setUpNum(value: Long) {
        dtoHolder.dto.upNum = value
    }

    fun getMid(): String? = dtoHolder.dto.mid
    fun setMid(value: String?) {
        dtoHolder.dto.mid = value
    }

    fun getIsOrderLive(): String? = dtoHolder.dto.isOrderLive
    fun setIsOrderLive(value: String?) {
        dtoHolder.dto.isOrderLive = value
    }

    fun getDirection(): Int = dtoHolder.dto.direction
    fun setDirection(value: Int) {
        dtoHolder.dto.direction = value
    }

    fun getVid(): String? = dtoHolder.dto.vid
    fun setVid(value: String?) {
        dtoHolder.dto.vid = value
    }

    fun getPid(): String? = dtoHolder.dto.pid
    fun setPid(value: String?) {
        dtoHolder.dto.pid = value
    }

    fun getRaceInfo(): RoseRaceInfo? = dtoHolder.dto.raceInfo
    fun setRaceInfo(value: RoseRaceInfo?) {
        dtoHolder.dto.raceInfo = value
    }

    fun getLiveSource(): Int = dtoHolder.dto.liveSource
    fun setLiveSource(value: Int) {
        dtoHolder.dto.liveSource = value
    }

    fun getYspLivingUrl(): String? = dtoHolder.dto.yspLivingUrl
    fun setYspLivingUrl(value: String?) {
        dtoHolder.dto.yspLivingUrl = value
    }

    fun isSubscribe(): Boolean = dtoHolder.dto.isSubscribe()
    fun setIsSubscribe(isSubscribe: Boolean) {
        dtoHolder.dto.setIsSubscribe(isSubscribe)
    }

    override fun getParcelDtoList(): List<IKmmParcelable?> = listOf(raceInfo)

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeInt(live_status)
        dest.writeString(room_id)
        dest.writeLong(start_time)
        dest.writeLong(end_time)
        dest.writeInt(screenType)
        dest.writeLong(online_total)
        dest.writeString(desc)
        dest.writeLong(up_num)
        dest.writeString(mid)
        dest.writeString(is_orderLive)
        dest.writeInt(hv_direction)
        dest.writeString(stream_id)
        dest.writeString(program_id)
        dest.writeSerializable(raceInfo)
        dest.writeInt(live_source)
        dest.writeString(ysp_living_url)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        live_status = from.readInt()
        room_id = from.readString()
        start_time = from.readLong()
        end_time = from.readLong()
        screenType = from.readInt()
        online_total = from.readLong()
        desc = from.readString()
        up_num = from.readLong()
        mid = from.readString()
        is_orderLive = from.readString()
        hv_direction = from.readInt()
        stream_id = from.readString()
        program_id = from.readString()
        raceInfo = from.safeRead { it.readSerializable() }
        live_source = from.readInt()
        ysp_living_url = from.readString()
    }
}

private class TransientLiveInfoDtoHolder(info: KmmLiveInfo) {
    val dto by lazy { LiveInfoDto(info) }
}
