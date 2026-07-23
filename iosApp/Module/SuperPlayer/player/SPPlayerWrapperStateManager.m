//
//  SPPlayerState.m
//  SPPlayer
//
//  Created by 郭力 on 2019/9/29.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperStateManager.h"


@interface SPPlayerWrapperStates ()
@property (atomic, assign) SPPlayerWrapperState curState; //当前状态
@property (atomic, assign) SPPlayerWrapperState preState; //先前状态
@end

@interface SPPlayerWrapperStateManager ()
@property (atomic, copy)   NSString *tag;                                           //wrapper层统一的日志tag
@property (atomic, assign) SPPlayerWrapperStage curStage;                          //wrapper层的状态阶段
@property (atomic, strong, nonnull) SPPlayerWrapperStates *mainState;              //播放器的状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *definitionSwitchState;  //无缝切换清晰度的状态状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *rDefinitionSwitchState; //重开播放器切换清晰度的状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *errorRetryState;        //错误重试的状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *livePlaybackState;      //直播回看的状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *startPipPlayState;      //启动画中画状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *stopPipPlayState;       //关闭画中画状态机
@property (atomic, strong, nonnull) SPPlayerWrapperStates *refreshPlayerState;     //刷新播放器状态机
@end

@implementation SPPlayerWrapperStates

- (instancetype)init{
    if (self = [super init]) {
        _curState = SPPlayerWrapperStateUnknown;
        _preState = SPPlayerWrapperStateUnknown;
    }
    return self;
}

@end

@implementation SPPlayerWrapperStateManager

- (instancetype)init {
  self = [super init];
  if (self) {
    _curStage = SPPlayerWrapperStageMain;
    _mainState = [[SPPlayerWrapperStates alloc] init];
    _definitionSwitchState = [[SPPlayerWrapperStates alloc] init];
    _rDefinitionSwitchState = [[SPPlayerWrapperStates alloc] init];
    _errorRetryState = [[SPPlayerWrapperStates alloc] init];
    _livePlaybackState = [[SPPlayerWrapperStates alloc] init];
    _startPipPlayState = [[SPPlayerWrapperStates alloc] init];
    _stopPipPlayState = [[SPPlayerWrapperStates alloc] init];
    _refreshPlayerState = [[SPPlayerWrapperStates alloc] init];
  }
    return self;
}

- (void)changeState:(SPPlayerWrapperState)state {
    switch (self.curStage) {
        case SPPlayerWrapperStageMain:
            self.mainState.preState = self.mainState.curState;
            self.mainState.curState = state;
            [self print:SPPlayerWrapperStageMain];
            break;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            self.rDefinitionSwitchState.preState = self.rDefinitionSwitchState.curState;
            self.rDefinitionSwitchState.curState = state;
            [self print:SPPlayerWrapperStageReOpenSwitchDefinition];
            break;
        case SPPlayerWrapperStageSwitchDefinition:
            self.definitionSwitchState.preState = self.definitionSwitchState.curState;
            self.definitionSwitchState.curState = state;
            [self print:SPPlayerWrapperStageSwitchDefinition];
            break;
        case SPPlayerWrapperStageErrorRetry:
            self.errorRetryState.preState = self.errorRetryState.curState;
            self.errorRetryState.curState = state;
            [self print:SPPlayerWrapperStageErrorRetry];
            break;
        case SPPlayerWrapperStageLiveBackPlay:
            self.livePlaybackState.preState = self.livePlaybackState.curState;
            self.livePlaybackState.curState = state;
            [self print:SPPlayerWrapperStageLiveBackPlay];
            break;
        case SPPlayerWrapperStageStartPipPlay:
            self.startPipPlayState.preState = self.startPipPlayState.curState;
            self.startPipPlayState.curState = state;
            [self print:SPPlayerWrapperStageStartPipPlay];
            break;
        case SPPlayerWrapperStageStopPipPlay:
            self.stopPipPlayState.preState = self.stopPipPlayState.curState;
            self.stopPipPlayState.curState = state;
            [self print:SPPlayerWrapperStageStopPipPlay];
            break;
        case SPPlayerWrapperStageRefreshPlayer:
            self.refreshPlayerState.preState = self.refreshPlayerState.curState;
            self.refreshPlayerState.curState = state;
            [self print:SPPlayerWrapperStageRefreshPlayer];
            break;
    }
}

- (void)changeStage:(SPPlayerWrapperStage)stage {
    self.curStage = stage;
//    [self print:SPPlayerWrapperStageMain];
}

- (SPPlayerWrapperState)currentState {
    SPPlayerWrapperState currentState = SPPlayerWrapperStateUnknown;
    switch (self.curStage) {
        case SPPlayerWrapperStageStartPipPlay:
            currentState = self.startPipPlayState.curState;
            break;
        default:
            currentState = self.mainState.curState;
            break;
    }
    return currentState;
}

- (SPPlayerWrapperState)previousState {
    return self.mainState.preState;
}

- (SPPlayerWrapperStage)currentStage {
    return self.curStage;
}

- (SPPlayerWrapperState)currentStateWithStage:(SPPlayerWrapperStage)stage {
    switch (stage) {
        case SPPlayerWrapperStageMain:
            return self.mainState.curState;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            return self.rDefinitionSwitchState.curState;
        case SPPlayerWrapperStageSwitchDefinition:
            return self.definitionSwitchState.curState;
        case SPPlayerWrapperStageErrorRetry:
            return self.errorRetryState.curState;
        case SPPlayerWrapperStageLiveBackPlay:
            return self.livePlaybackState.curState;
        case SPPlayerWrapperStageStartPipPlay:
            return self.startPipPlayState.curState;
        case SPPlayerWrapperStageStopPipPlay:
            return self.stopPipPlayState.curState;
        case SPPlayerWrapperStageRefreshPlayer:
            return self.refreshPlayerState.curState;
    }
}

- (SPPlayerWrapperState)previousStateWithStage:(SPPlayerWrapperStage)stage {
    switch (stage) {
        case SPPlayerWrapperStageMain:
            return self.mainState.preState;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            return self.rDefinitionSwitchState.preState;
        case SPPlayerWrapperStageSwitchDefinition:
            return self.definitionSwitchState.preState;
        case SPPlayerWrapperStageErrorRetry:
            return self.errorRetryState.preState;
        case SPPlayerWrapperStageLiveBackPlay:
            return self.livePlaybackState.preState;
        case SPPlayerWrapperStageStartPipPlay:
            return self.startPipPlayState.preState;
        case SPPlayerWrapperStageStopPipPlay:
            return self.stopPipPlayState.preState;
        case SPPlayerWrapperStageRefreshPlayer:
            return self.refreshPlayerState.preState;
    }
}

- (BOOL)isMainStage {
    return self.curStage == SPPlayerWrapperStageMain;
}

- (BOOL)isStage:(SPPlayerWrapperStage)stage {
    return self.curStage == stage;
}

- (BOOL)isState:(SPPlayerWrapperState)state {
    return self.mainState.curState == state;
}

- (BOOL)isStage:(SPPlayerWrapperStage)stage withState:(SPPlayerWrapperState)state {
    switch (stage) {
        case SPPlayerWrapperStageMain:
            return state == self.mainState.curState;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            return state == self.rDefinitionSwitchState.curState;
        case SPPlayerWrapperStageSwitchDefinition:
            return state == self.definitionSwitchState.curState;
        case SPPlayerWrapperStageErrorRetry:
            return state == self.errorRetryState.curState;
        case SPPlayerWrapperStageLiveBackPlay:
            return state == self.livePlaybackState.curState;
        case SPPlayerWrapperStageStartPipPlay:
            return state == self.startPipPlayState.curState;
        case SPPlayerWrapperStageStopPipPlay:
            return state == self.stopPipPlayState.curState;
        case SPPlayerWrapperStageRefreshPlayer:
            return state == self.refreshPlayerState.curState;
    }
}

- (void)restoreToMainStage {
    if ([self isStage:SPPlayerWrapperStageReOpenSwitchDefinition]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.rDefinitionSwitchState.curState = SPPlayerWrapperStateUnknown;
        self.rDefinitionSwitchState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
    if ([self isStage:SPPlayerWrapperStageSwitchDefinition]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.definitionSwitchState.curState = SPPlayerWrapperStateUnknown;
        self.definitionSwitchState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
    if ([self isStage:SPPlayerWrapperStageErrorRetry]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.errorRetryState.curState = SPPlayerWrapperStateUnknown;
        self.errorRetryState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
    if ([self isStage:SPPlayerWrapperStageLiveBackPlay]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.livePlaybackState.curState = SPPlayerWrapperStateUnknown;
        self.livePlaybackState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
    if ([self isStage:SPPlayerWrapperStageStartPipPlay]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.startPipPlayState.curState = SPPlayerWrapperStateUnknown;
        self.startPipPlayState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
     if ([self isStage:SPPlayerWrapperStageStopPipPlay]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.stopPipPlayState.curState = SPPlayerWrapperStateUnknown;
        self.stopPipPlayState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
    
    if ([self isStage:SPPlayerWrapperStageRefreshPlayer]) {
        self.curStage = SPPlayerWrapperStageMain;
        self.refreshPlayerState.curState = SPPlayerWrapperStateUnknown;
        self.refreshPlayerState.preState = SPPlayerWrapperStateUnknown;
        return;
    }
}

- (void) print:(SPPlayerWrapperStage)stageCause {
    SPLOGI(self.tag, @"wrapper state change ***********************************");
    switch (stageCause) {
        case SPPlayerWrapperStageMain:
            SPLOGI(self.tag, @"wrapper state change : cause : main state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            SPLOGI(self.tag, @"wrapper state change : cause : reopen switch definition");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageSwitchDefinition:
            SPLOGI(self.tag, @"wrapper state change : cause : seamless switch definition");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageErrorRetry:
            SPLOGI(self.tag, @"wrapper state change : cause : error retry state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageLiveBackPlay:
            SPLOGI(self.tag, @"wrapper state change : cause : live back state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageStartPipPlay:
            SPLOGI(self.tag, @"wrapper state change : cause : start pip state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageStopPipPlay:
            SPLOGI(self.tag, @"wrapper state change : cause : stop pip state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
        case SPPlayerWrapperStageRefreshPlayer:
            SPLOGI(self.tag, @"wrapper state change : cause : refresh player state change");
            SPLOGI(self.tag, @"wrapper state change : state : %@" , [self description]);
            break;
    }
    SPLOGI(self.tag, @"wrapper state change ***********************************");
}

#pragma mark - valid api calll
// #lizard forgives
- (BOOL)isValidCall:(SPPlayerWrapperAPI)api {
    switch (api) {
        case SPPlayerWrapperAPIOpen:
            return [self isValidCallForAPIOpen];
        case SPPlayerWrapperAPIPlay:
            return [self isValidCallForAPIPlay];
        case SPPlayerWrapperAPIPrepare:
            return [self isValidCallForAPIPrepare];
        case SPPlayerWrapperAPIPause:
            return [self isValidCallForAPIPause];
        case SPPlayerWrapperAPISeekTo:
            return [self isValidCallForAPISeekTo];
        case SPPlayerWrapperAPISeekLive:
            return [self isValidCallForAPISeekLive];
        case SPPlayerWrapperAPIStop:
            return [self isValidCallForAPIStop];
        case SPPlayerWrapperAPISetParam:
            return [self isValidCallForAPISetParam];
        case SPPlayerWrapperAPIGetRunTimeInfo:
            return [self isValidCallForAPIGetRunTimeInfo];
        case SPPlayerWrapperAPISwitchDefinition:
            return [self isValidCallAPISwitchDefinition];
        case SPPlayerWrapperAPIRefreshPlayer:
            return [self isValidCallForAPIRefreshPlayer];
        case SPPlayerWrapperAPICaptureImage:
            return [self isValidCallForAPICaptureImage];
        case SPPlayerWrapperAPIStartPip:
            return [self isValidCallForAPIStartPIP];
        case SPPlayerWrapperAPIStopPip:
            return [self isValidCallForAPIStopPip];
        case SPPlayerWrapperAPIPauseDownload:
            return [self isValidCallForAPIPauseDownload];
        case SPPlayerWrapperAPIResumeDonwload:
            return [self isValidCallForAPIResumeDonwload];
        case SPPlayerWrapperAPIRealTimeInfo:
            return [self isValidCallForAPIRealTimeInfo];
        case SPPlayerWrapperAPIGetRunTimeInfoFromStartPosition:
            return [self isValidCallForAPIGetRunTimeInfoFromStartPosition];
    }
}

- (BOOL)isValidCallForAPIOpen {
    return self.mainState.curState == SPPlayerWrapperStateUnknown ||
           self.mainState.curState == SPPlayerWrapperStateStopped ||
           self.mainState.curState == SPMediaPlayerStateError;
}

- (BOOL)isValidCallForAPIPlay {
    return self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPIPrepare {
    return  self.mainState.curState == SPPlayerWrapperStateCGIed ||
            self.mainState.curState == SPPlayerWrapperStateStopped;
}

- (BOOL)isValidCallForAPIPause {
    return  self.mainState.curState == SPPlayerWrapperStatePlaying    ||
            self.mainState.curState == SPPlayerWrapperStateUserPaused;
}

- (BOOL)isValidCallForAPISeekTo {
    return self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPISeekLive {
    return self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPIStop {
    return self.mainState.curState == SPPlayerWrapperStateCGIing     ||
           self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete   ||
           self.mainState.curState == SPPlayerWrapperStateError;
}

- (BOOL)isValidCallForAPISetParam {
    return self.mainState.curState == SPPlayerWrapperStateUnknown    ||
           self.mainState.curState == SPPlayerWrapperStateCGIing     ||
           self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPIGetRunTimeInfo {
    return (self.curStage == SPPlayerWrapperStageMain ||
            self.curStage == SPPlayerWrapperStageSwitchDefinition) &&
            (self.mainState.curState == SPPlayerWrapperStatePrepared   ||
             self.mainState.curState == SPPlayerWrapperStatePlaying    ||
             self.mainState.curState == SPPlayerWrapperStateUserPaused ||
             self.mainState.curState == SPPlayerWrapperStateStopped    ||
             self.mainState.curState == SPPlayerWrapperStateComplete);
}

- (BOOL)isValidCallForAPIGetRunTimeInfoFromStartPosition {
    return (self.curStage == SPPlayerWrapperStageMain) &&
            (self.mainState.curState == SPPlayerWrapperStateCGIing    ||
             self.mainState.curState == SPPlayerWrapperStateCGIed     ||
             self.mainState.curState == SPPlayerWrapperStatePreparing ||
             self.mainState.curState == SPPlayerWrapperStatePrepared);
}

- (BOOL)isValidCallAPISwitchDefinition {
     return self.mainState.curState == SPPlayerWrapperStateCGIed      ||
            self.mainState.curState == SPPlayerWrapperStatePreparing  ||
            self.mainState.curState == SPPlayerWrapperStatePrepared   ||
            self.mainState.curState == SPPlayerWrapperStatePlaying    ||
            self.mainState.curState == SPPlayerWrapperStateUserPaused ||
            self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPIRefreshPlayer {
    return self.mainState.curState == SPPlayerWrapperStateUnknown    ||
           self.mainState.curState == SPPlayerWrapperStateCGIing     ||
           self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete;
}

- (BOOL)isValidCallForAPICaptureImage {
    return self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused;
}

- (BOOL)isValidCallForAPIStartPIP {
    return self.curStage == SPPlayerWrapperStageMain && (
           self.mainState.curState == SPPlayerWrapperStatePrepared  ||
           self.mainState.curState == SPPlayerWrapperStatePlaying   ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused);
}

- (BOOL)isValidCallForAPIStopPip {
    return self.curStage == SPPlayerWrapperStageStartPipPlay ||
            ( self.curStage == SPPlayerWrapperStageMain &&
             ( self.mainState.curState == SPPlayerWrapperStatePlaying ||
               self.mainState.curState == SPPlayerWrapperStateUserPaused
             )
         );
}

- (BOOL)isValidCallForAPIPauseDownload {
    return self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete   ||
           self.mainState.curState == SPPlayerWrapperStateStopped    ||
           self.mainState.curState == SPPlayerWrapperStateError;
}

- (BOOL)isValidCallForAPIResumeDonwload {
    return self.mainState.curState == SPPlayerWrapperStateUnknown    ||
           self.mainState.curState == SPPlayerWrapperStateCGIing     ||
           self.mainState.curState == SPPlayerWrapperStateCGIed      ||
           self.mainState.curState == SPPlayerWrapperStatePreparing  ||
           self.mainState.curState == SPPlayerWrapperStatePrepared   ||
           self.mainState.curState == SPPlayerWrapperStatePlaying    ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused ||
           self.mainState.curState == SPPlayerWrapperStateComplete   ||
           self.mainState.curState == SPPlayerWrapperStateStopped    ||
           self.mainState.curState == SPPlayerWrapperStateError;
}

- (BOOL)isValidCallForAPIRealTimeInfo {
    return self.mainState.curState == SPPlayerWrapperCBOnPrepared ||
           self.mainState.curState == SPPlayerWrapperStatePlaying ||
           self.mainState.curState == SPPlayerWrapperStateUserPaused;
}

#pragma mark - valid call back
// #lizard forgives
- (BOOL)isValidCallback:(SPPlayerWrapperCB)callback {
    switch (callback) {
        case SPPlayerWrapperCBOnPrepared:
            return [self isValidCallbackForCBOnPrepared];
        case SPPlayerWrapperCBOnCompletion:
            return [self isValidCallbackForCBOnCompletion];
        case SPPlayerWrapperCBOnPlayerError:
            return [self isValidCallbackForCBOnPlayerError];
        case SPPlayerWrapperCBOnSeekComplete:
            return [self isValidCallbackForCBOnSeekComplete];
        case SPPlayerWrapperCBOnVideoSizeChange:
            return [self isValidCallbackForCBOnVideoSizeChange];
        case SPPlayerWrapperCBOnData:
            return [self isValidCallbackForCBOnData];
        case SPPlayerWrapperCBOnInfo:
            return [self isValidCallbackForCBOnInfo];
        case SPPlayerWrapperCBOnCGISuc:
            return [self isValidCallbackForCBOnCGISuc];
        case SPPlayerWrapperCBOnCGIError:
            return [self isValidCallbackForCBOnCGIError];
        case SPPlayerWrapperCBOnCGIUpdate:
            return [self isValidCallbackForCBOnCGIUpdate];
        case SPPlayerWrapperCBAirPlay:
            return [self isValidCallbackForCBAirPlay];
        case SPPlayerWrapperCBOnPip:
            return [self isValidCallbackForCBOnPip];
        case SPPlayerWrapperCBOnStateChange:
          return [self isValidCallbackForCBOnStateChange];
        default:
          break;
    }
}

- (BOOL)isValidCallbackForCBOnPrepared {
    return [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageReOpenSwitchDefinition withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageLiveBackPlay withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageStartPipPlay withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageRefreshPlayer withState:SPPlayerWrapperStatePreparing];
}

- (BOOL)isValidCallbackForCBOnCompletion {
    return [self isState:SPPlayerWrapperStatePrepared]   ||
           [self isState:SPPlayerWrapperStateUserPaused] ||
           [self isState:SPPlayerWrapperStatePlaying];
}

- (BOOL)isValidCallbackForCBOnPlayerError {
    return [self isState:SPPlayerWrapperStatePreparing]  ||
           [self isState:SPPlayerWrapperStatePrepared]   ||
           [self isState:SPPlayerWrapperStateUserPaused] ||
           [self isState:SPPlayerWrapperStatePlaying]    ||
           [self isStage:SPPlayerWrapperStageReOpenSwitchDefinition withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageReOpenSwitchDefinition withState:SPPlayerWrapperStatePrepared]  ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStatePreparing]   ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStatePrepared]    ||
           [self isStage:SPPlayerWrapperStageLiveBackPlay withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageLiveBackPlay withState:SPPlayerWrapperStatePrepared]  ||
           [self isStage:SPPlayerWrapperStageStartPipPlay withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageStartPipPlay withState:SPPlayerWrapperStatePrepared];
}

- (BOOL)isValidCallbackForCBOnSeekComplete {
    return [self isState:SPPlayerWrapperStatePrepared]   ||
           [self isState:SPPlayerWrapperStateUserPaused] ||
           [self isState:SPPlayerWrapperStatePlaying]    ||
           [self isState:SPPlayerWrapperStateComplete];
}

- (BOOL)isValidCallbackForCBOnVideoSizeChange {
    return [self isState:SPPlayerWrapperStatePreparing]  ||
           [self isState:SPPlayerWrapperStatePrepared]   ||
           [self isState:SPPlayerWrapperStateUserPaused] ||
           [self isState:SPPlayerWrapperStatePlaying];
}

- (BOOL)isValidCallbackForCBOnData {
    return [self isState:SPPlayerWrapperStateUserPaused] ||
           [self isState:SPPlayerWrapperStatePlaying];
}

- (BOOL)isValidCallbackForCBOnInfo {
    return [self isState:SPPlayerWrapperStateCGIed]      ||
           [self isState:SPPlayerWrapperStatePreparing]  ||
           [self isState:SPPlayerWrapperStatePrepared]   ||
           [self isState:SPPlayerWrapperStatePlaying]    ||
           [self isState:SPPlayerWrapperStateUserPaused];
}

- (BOOL)isValidCallbackForCBOnCGISuc {
    return [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStateCGIing]   ||
           [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStateCGIed]    ||
           [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStatePreparing] ||
           [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStatePrepared] ||
           [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStatePlaying]  ||
           [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStateUserPaused]  ||
           [self isStage:SPPlayerWrapperStageReOpenSwitchDefinition withState:SPPlayerWrapperStateCGIing] ||
           [self isStage:SPPlayerWrapperStageSwitchDefinition withState:SPPlayerWrapperStateCGIing] ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStateCGIing]       ||
           [self isStage:SPPlayerWrapperStageLiveBackPlay withState:SPPlayerWrapperStateCGIing]     ||
           [self isStage:SPPlayerWrapperStageStartPipPlay withState:SPPlayerWrapperStateCGIing]     ||
           [self isStage:SPPlayerWrapperStageRefreshPlayer withState:SPPlayerWrapperStateCGIing];
}

- (BOOL)isValidCallbackForCBOnCGIError {
    return [self isStage:SPPlayerWrapperStageMain withState:SPPlayerWrapperStateCGIing] ||
           [self isStage:SPPlayerWrapperStageReOpenSwitchDefinition withState:SPPlayerWrapperStateCGIing] ||
           [self isStage:SPPlayerWrapperStageSwitchDefinition withState:SPPlayerWrapperStateCGIing] ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStateCGIing]       ||
           [self isStage:SPPlayerWrapperStageErrorRetry withState:SPPlayerWrapperStateCGIing]       ||
           [self isStage:SPPlayerWrapperStageLiveBackPlay withState:SPPlayerWrapperStateCGIing]     ||
           [self isStage:SPPlayerWrapperStageStartPipPlay withState:SPPlayerWrapperStateCGIing]     ||
           [self isStage:SPPlayerWrapperStageRefreshPlayer withState:SPPlayerWrapperStateCGIing];
}

- (BOOL)isValidCallbackForCBOnCGIUpdate {
    return YES;
}

- (BOOL)isValidCallbackForCBAirPlay {
    return YES;
}

- (BOOL)isValidCallbackForCBOnPip {
    return YES;
}

- (BOOL)isValidCallbackForCBOnStateChange {
  return YES;
}

- (void)setLogTag:(NSString *)tag {
    self.tag = tag;
}
- (NSString *)description {
    NSMutableString *description = [[NSMutableString alloc] init];
    [description appendString:@"state"];
    [description appendString:@" [ "];
    [description appendFormat:@"main : cur = %@ pre = %@" ,
     [SPPlayerWrapperHelper stringValueForWrapperState:[self currentState]] ,
     [SPPlayerWrapperHelper stringValueForWrapperState:[self previousState]]
    ];
    
    if (self.curStage != SPPlayerWrapperStageMain) {
        [description appendString:@" | "];
        [description appendFormat:@" %@ : cur = %@ pre = %@" ,
         [SPPlayerWrapperHelper stringValueForWrapperStage:self.curStage],
         [SPPlayerWrapperHelper stringValueForWrapperState:[self currentStateWithStage:self.curStage]],
         [SPPlayerWrapperHelper stringValueForWrapperState:[self previousStateWithStage:self.curStage]]
        ];
    }
    
    [description appendString:@" ] "];
    return description;
}

@end
