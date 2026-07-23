//
//  SPPlayerState.h
//  SPPlayer
//
//  Created by 郭力 on 2019/9/29.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayerWrapperDefine.h"
#import "SPPlayerWrapperHelper.h"

NS_ASSUME_NONNULL_BEGIN

//状态机组合，添加上prevoius状态，方便一些逻辑
@interface SPPlayerWrapperStates : NSObject
@end


@interface SPPlayerWrapperStateManager : NSObject

- (void)changeState:(SPPlayerWrapperState)state;

- (void)changeStage:(SPPlayerWrapperStage)stage;

- (SPPlayerWrapperState)currentState;

- (SPPlayerWrapperState)previousState;

- (SPPlayerWrapperStage)currentStage;

- (SPPlayerWrapperState)currentStateWithStage:(SPPlayerWrapperStage)stage;

- (SPPlayerWrapperState)previousStateWithStage:(SPPlayerWrapperStage)stage;

- (BOOL) isMainStage;

- (BOOL) isStage:(SPPlayerWrapperStage)stage;

- (BOOL) isState:(SPPlayerWrapperState)state;

- (BOOL) isStage:(SPPlayerWrapperStage)stage withState:(SPPlayerWrapperState)state;

- (void) restoreToMainStage;

- (BOOL) isValidCall:(SPPlayerWrapperAPI)api;

- (BOOL) isValidCallback:(SPPlayerWrapperCB)callback;

- (void) setLogTag:(NSString *)tag;
@end

NS_ASSUME_NONNULL_END
