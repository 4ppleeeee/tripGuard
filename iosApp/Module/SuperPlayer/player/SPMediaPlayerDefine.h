/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPMediaPlayerDefine.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/21
 Description :
 History     : 17/2/21 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, SPVideoPlayerMsg) {
    SPVideoPlayerMsgUnknown = 0,

    SPVideoPlayerMsgError,             // error
    SPVideoPlayerMsgPreparing,         // preparing to open file
    SPVideoPlayerMsgOpenFileComplete,  // open file complete
    SPVideoPlayerMsgReadyToStart,      // ready to start, now you can call start function
    SPVideoPlayerMsgStarted,           // start
    SPVideoPlayerMsgPause,             // pause
                                        //    SPVideoPlayerMsgStartBuffering,                    // start buffering
                                        //    SPVideoPlayerMsgEndBuffering,                      // end buffering
                                        //    SPVideoPlayerMsgSeekComplete,                      // seek complete
    SPVideoPlayerMsgPlaybackComplete,  // playback complete
                                        //    SPVideoPlayerMsgNoMoreDataReading,                 // no more data
                                        //    SPVideoPlayerMsgResolutionDidChange,               // video resolution did change
    SPVideoPlayerMsgStopped,           // playback was stopped

    SPVideoPlayerMsgSkipLotsOfFramesInOneUnitOfTime,  // skip a lot of frames in one unit of time
    SPVideoPlayerMsgAudioDecoderModeDidChange,        // 音频解码器类型改变
    SPVideoPlayerMsgVideoDecoderModeDidChange,        // video decoder mode did change
    SPVideoPlayerMsgPlayerCreateStart,                // create player start
    SPVideoPlayerMsgPlayerCreateEnd,                  // create player end
    SPVideoPlayerMsgCreateFirstVideoFrameDecoder,     // start to create first video frame decoder,
                                                       // maybe notify more than one time before first video frame rendered
    SPVideoPlayerMsgFirstFrameRendered,               // first frame rendered
};
