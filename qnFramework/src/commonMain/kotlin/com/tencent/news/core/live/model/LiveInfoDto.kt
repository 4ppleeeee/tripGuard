package com.tencent.news.core.live.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.safeRead

/**
 * LiveInfo 字段访问器
 * 用于解决循环引用问题
 */
class LiveInfoDto(private val liveInfo: KmmLiveInfo) : IKmmLiveInfo, IKmmKeep, IKmmParcelable {

    // 接口实现，通过 by 委托访问 liveInfo 的原始字段
    override var liveStatus: Int by liveInfo::live_status
    override var streamId: String? by liveInfo::stream_id
    override var roomId: String? by liveInfo::room_id
    override var startTime: Long by liveInfo::start_time
    override var endTime: Long by liveInfo::end_time
    override var screenType: Int by liveInfo::screenType
    override var onlineTotal: Long by liveInfo::online_total
    override var desc: String? by liveInfo::desc
    override var upNum: Long by liveInfo::up_num
    override var mid: String? by liveInfo::mid
    override var isOrderLive: String? by liveInfo::is_orderLive
    override var direction: Int by liveInfo::hv_direction
    override var vid: String? by liveInfo::stream_id
    override var pid: String? by liveInfo::program_id
    override var raceInfo: RoseRaceInfo? by liveInfo::raceInfo
    override var liveSource: Int by liveInfo::live_source
    override var yspLivingUrl: String? by liveInfo::ysp_living_url

    override fun isSubscribe(): Boolean = "1" == isOrderLive

    override fun setIsSubscribe(isSubscribe: Boolean) {
        isOrderLive = if (isSubscribe) "1" else "0"
    }

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeInt(liveStatus)
        dest.writeString(roomId)
        dest.writeLong(startTime)
        dest.writeLong(endTime)
        dest.writeInt(screenType)
        dest.writeLong(onlineTotal)
        dest.writeString(desc)
        dest.writeLong(upNum)
        dest.writeString(mid)
        dest.writeString(isOrderLive)
        dest.writeInt(direction)
        dest.writeString(vid)
        dest.writeString(pid)
        dest.writeSerializable(raceInfo)
        dest.writeInt(liveSource)
        dest.writeString(yspLivingUrl)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        liveStatus = from.readInt()
        roomId = from.readString()
        startTime = from.readLong()
        endTime = from.readLong()
        screenType = from.readInt()
        onlineTotal = from.readLong()
        desc = from.readString()
        upNum = from.readLong()
        mid = from.readString()
        isOrderLive = from.readString()
        direction = from.readInt()
        vid = from.readString()
        pid = from.readString()
        raceInfo = from.safeRead { it.readSerializable() }
        liveSource = from.readInt()
        yspLivingUrl = from.readString()
    }
}

/**
 * 直播信息跨平台接口
 * 用于在 KMM 和宿主平台间共享直播数据
 */
interface IKmmLiveInfo : IKmmKeep, IKmmParcelable, ICmsModelDoc {

    val liveStatus: Int

    val streamId: String?

    /**
     * 房间ID
     */
    val roomId: String?

    /**
     * 开始时间
     */
    val startTime: Long

    /**
     * 结束时间
     */
    val endTime: Long

    /**
     * 屏幕类型
     * 0: 横屏, 1: 竖屏
     */
    val screenType: Int

    /**
     * 在线总人数
     */
    val onlineTotal: Long

    /**
     * 直播描述
     */
    val desc: String?

    /**
     * 点赞数
     */
    val upNum: Long

    /**
     * 主播ID
     */
    val mid: String?

    /**
     * 是否预约直播
     * "1": 已预约, "0": 未预约
     */
    val isOrderLive: String?

    /**
     * 方向
     */
    val direction: Int

    /**
     * 流ID
     */
    val vid: String?

    /**
     * 节目ID
     */
    val pid: String?

    /**
     * 赛事信息
     */
    val raceInfo: RoseRaceInfo?

    /**
     * 直播来源
     * 2: 央视频
     */
    val liveSource: Int

    /**
     * 央视频直播URL
     */
    val yspLivingUrl: String?

    /**
     * 是否已订阅
     */
    fun isSubscribe(): Boolean

    /**
     * 设置订阅状态
     */
    fun setIsSubscribe(isSubscribe: Boolean)
}
