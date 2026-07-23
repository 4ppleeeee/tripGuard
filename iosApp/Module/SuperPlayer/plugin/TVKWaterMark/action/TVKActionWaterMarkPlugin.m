/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkPlugin.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import "TVKActionWaterMarkPlugin.h"
#import "TVKRawWaterMarkInfo.h"
#import "SPNetWorkManager.h"
#import "TVKJSONResponse.h"
#import "TVKActionWaterMarkSceneGroup.h"
//#import "TVKMediaPlayerInfoEventSender.h"
//#import "TVKMediaPlayer.h"

@interface TVKActionWaterMarkPlugin ()

@property (nonatomic, strong) TVKActionWaterMarkModel *actionWaterMarkModel;
@property (nonatomic, strong) TVKActionWaterMarkSceneGroup *actionWaterMarkSceneGroup;
/** 动态水印json 是否下载完成 */
@property (nonatomic) BOOL waterMarkInfoPrepared;
/** 是否start */
@property (nonatomic) BOOL waterMarkStarted;
@property (nonatomic) NSTimeInterval startTime;
@property (nonatomic, assign) CGSize videoSize;
@property (nonatomic, assign) SPVideoStretchMode stretchMode;
@property (nonatomic, strong) TVKWaterMarkCGIInfo *waterMarkCGIinfo;
@property (nonatomic, assign) NSTimeInterval playerPosition;
@end

@implementation TVKActionWaterMarkPlugin

- (instancetype)initWithContext:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo extraInfo:(TVKWaterMarkExtraInfo *)extraInfo {
    if (self = [super init]) {
        self.pluginId = TVKMediaPlayerPluginViewIdWaterMark;
        _stretchMode = extraInfo.stretchMode;
        _videoSize = extraInfo.videoSize;
        _playerPosition = extraInfo.position;
        _waterMarkCGIinfo = waterMarkCGIinfo;
        _waterMarkStarted = NO;
        _waterMarkInfoPrepared = NO;
    }
    return self;
}

- (void)load {
    [self requestActionWaterMark];
}

- (void)unLoad {
    self.waterMarkInfoPrepared = NO;
    self.waterMarkStarted = NO;
    self.actionWaterMarkModel = nil;
    [self.actionWaterMarkSceneGroup destroy];  // 先释放资源
    self.actionWaterMarkSceneGroup = nil;
}

- (void)onStretchModeChanged:(SPVideoStretchMode)stretchMode {
    if (self.waterMarkInfoPrepared == NO || self.actionWaterMarkSceneGroup == nil || self.waterMarkStarted == NO) {
        SPLOGW(SP_WATER_MARK_LOG_FILTER, @"onStretchModeChanged waterMarkInfoPrepared is NO");
        return;
    }
    SPLOGI(SP_WATER_MARK_LOG_FILTER, @"onStretchModeChanged stretchMode=(%d)", stretchMode);
    [self.actionWaterMarkSceneGroup setStretchMode:stretchMode];
    [self.actionWaterMarkSceneGroup requestLayout];
}

- (void)onViewSizeChanged:(CGSize)videoViewSize {
    if (self.waterMarkInfoPrepared == NO || self.actionWaterMarkSceneGroup == nil || self.waterMarkStarted == NO) {
        SPLOGW(SP_WATER_MARK_LOG_FILTER, @"onViewSizeChanged waterMarkInfoPrepared is NO");
        return;
    }
    SPLOGI(SP_WATER_MARK_LOG_FILTER, @"changed videoViewSize=(%f,%f)", videoViewSize.width, videoViewSize.height);
    [self.actionWaterMarkSceneGroup onVideoViewSizeChanged:videoViewSize];
    [self.actionWaterMarkSceneGroup requestLayout];
}

- (void)onPlayerPositionUpdated:(NSTimeInterval)playerPosition {
    self.playerPosition = playerPosition;
    if (self.waterMarkInfoPrepared == NO || self.actionWaterMarkSceneGroup == nil || self.waterMarkStarted == NO) {
        SPLOGW(SP_WATER_MARK_LOG_FILTER, @"onPlayerPositionUpdated waterMarkInfoPrepared is NO");
        return;
    }
    if (_actionWaterMarkModel.runMode == TVKActionWaterMarkRunModeRelativeToPlayTime) {
        NSTimeInterval curTime = [[NSDate date] timeIntervalSince1970];
        [_actionWaterMarkSceneGroup setRelativeTime:(curTime - _startTime)];
    } else {
        [_actionWaterMarkSceneGroup setPlayPosition:playerPosition];
    }
}

- (void)onPlayStart:(NSTimeInterval)startTime {
    SPLOGW(SP_WATER_MARK_LOG_FILTER, @"onPlayStart startTime=%lf", startTime);
    self.startTime = startTime;
    self.waterMarkStarted = YES;
    if (self.waterMarkInfoPrepared == YES) {
        [self show];
    }
}

- (void)requestActionWaterMark {
    NSString *actionUrl = self.waterMarkCGIinfo.waterMarkModel.actionUrl;
    TVKActionWaterMarkModel *actionWaterMarkModel = self.waterMarkCGIinfo.waterMarkModel.actionWaterMarkModel;
    if (actionWaterMarkModel != nil) {
        self.actionWaterMarkModel = actionWaterMarkModel;
        [self initActionWaterMark];
        self.waterMarkInfoPrepared = YES;
        if (self.waterMarkStarted == YES) {
            [self show];
        }
        return;
    }
    if (actionUrl.length == 0) {
        return;
    }
    [[SPNetWorkManager shareInstance] getRequest:actionUrl
                                   requestHeaders:nil
                                completionHandler:^(NSData *__nullable responseData, NSError *__nullable error) {
                                    TVKJSONResponse *response = [[TVKJSONResponse alloc] init];
                                    [response processResponseData:responseData];
                                    NSString *jsonString = [[NSString alloc] initWithData:response.responseData encoding:NSUTF8StringEncoding];
                                    jsonString = [TVKJSONResponse filterJSON:jsonString];
                                    id dict = [TVKJSONResponse parseJSON:jsonString];
                                    if ([dict isKindOfClass:[NSDictionary class]]) {
                                        self.actionWaterMarkModel = [TVKActionWaterMarkModel actionWaterMarkModelWithDict:dict];
                                    }
                                    /** 动态水印json 下载完成后先渲染一次,因为水印创建是在videosizechange开始创建*/
                                    [self initActionWaterMark];
                                    self.waterMarkInfoPrepared = YES;
                                    if (self.waterMarkStarted == YES) {
                                        [self show];
                                    }
                                }];
}

- (void)initActionWaterMark {
    UIView *parent = [self.delegate onGetContainerWithId:self.pluginId];
    CGSize videoSize = self.waterMarkCGIinfo.videoSize;
    if (videoSize.width == 0 || videoSize.height == 0) {
        videoSize = self.videoSize;
    }
    _actionWaterMarkSceneGroup = [[TVKActionWaterMarkSceneGroup alloc] initWithActionWaterMarkModel:_actionWaterMarkModel
                                                                                          container:parent videoSize:videoSize];
    SPVideoStretchMode stretchMode = self.stretchMode;
    [_actionWaterMarkSceneGroup setStretchMode:stretchMode];  //先设置一些stetchmode，水印计算位置需要
}

- (void)show {
    [self.actionWaterMarkSceneGroup setStretchMode:self.stretchMode];
    [self.actionWaterMarkSceneGroup onVideoSizeChange:self.videoSize];
    NSTimeInterval curPosition = self.playerPosition;
    if (_actionWaterMarkModel.runMode == TVKActionWaterMarkRunModeRelativeToPlayTime) {
        NSTimeInterval curTime = [[NSDate date] timeIntervalSince1970];
        SPLOGD(@"TVKPlayFlow-Watermark", @"curTime=%lf, st=%lf", curTime, self.startTime);
        [_actionWaterMarkSceneGroup setRelativeTime:(curTime - self.startTime)];
    } else {
        SPLOGD(@"TVKPlayFlow-Watermark", @"curPosition=%lf", curPosition);
        [_actionWaterMarkSceneGroup setPlayPosition:curPosition];
    }
}
@end
