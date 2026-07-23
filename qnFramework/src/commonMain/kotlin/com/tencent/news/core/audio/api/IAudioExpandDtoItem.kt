package com.tencent.news.core.audio.api

import com.tencent.news.core.audio.model.RadioScene
import com.tencent.news.core.audio.model.RadioSceneType
import com.tencent.news.core.audio.model.RadioSubSceneType

/**
 * 音频URL播放队列扩展字段
 *
 * 说明：
 *   1.播放顺序规则:
 *     高优先级队列 > 正文音频 > 低优先级队列
 *     正文音频数据扩展出如下高优先级以及低优先级的播放队列，保证优先播放高优先级的队列音频，再去播放正文音频，最后播放低优先级队列音频
 *
 *   2.播放逻辑说明：
 *     完整音频播放 = 高优先级队列 + 正文音频 + 低优先级队列
 *     高优先级队列以及低优先级队列可以理解为正文音频的分片数据，被当做正文音频的分片数据数据，外部从虚拟播放器获取播放的音频数据，
 *     始终获取的是正文音频的数据。同时虚拟播放器内部会等到所有的音频数据都被消费完毕后，才会向外部回调Finish事件
 *
 *  3. 使用场景：
 *     仅针对URL场景播放生效，TTS场景不生效
 */
interface IAudioExpandDtoItem {

    // 音频播放场景
    var audioScene: RadioScene

    // 播放队列: 高优先级队列
    var highPrioritySubPlayUrlList: List<IAudioExpandFeedItem>?

    // 播放队列: 低优先级队列
    var lowPrioritySubPlayUrlList: List<IAudioExpandFeedItem>?
}


interface IAudioExpandFeedItem {

    // 播放的音频id
    var audioId: String

    // 播放的音频url
    var url: String

    // 播放的音频时长
    var duration: Float
}