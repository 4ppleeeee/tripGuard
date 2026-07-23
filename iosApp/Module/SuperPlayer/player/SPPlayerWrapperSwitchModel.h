//
//  SPPlayerWrapperSwitchModel.h
//  SPPlayer
//
//  Created by 郭力 on 2019/10/8.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayingContext.h"

NS_ASSUME_NONNULL_BEGIN
static long const KTVWrapperSwitchBaseOpaqu       = 100000;

typedef NS_ENUM(NSUInteger, SPSwitchType) {
    SPSwitchTypeDefinition     = 0, //切换清晰度类型
};

typedef NS_ENUM(NSUInteger, SPSwitchRetCode) {
    SPSwitchRetCodeOK               = 0, //通用返回码：成功
    SPSwitchRetCodeDuplicateTask    = 1, //通用返回码：添加失败，重复任务
    SPSwitchRetCodeNoneExistentTask = 2, //通用错误码：任务不存在
    SPSwitchRetCodeNotLastestTask   = 3, //通用错误码：不是最后一个任务
};

@interface SPSwitchRet : NSObject
@property (nonatomic, assign) long taskId;
@property (nonatomic, assign) SPSwitchRetCode   code;
@property (nonatomic, assign) SPSwitchType      type;
@property (nonatomic, strong) SPPlayingContext *info;
@end

@protocol ISPPlayerWrapperSwitchModel <NSObject>
@required
- (SPSwitchRet*)driveAddOneNewTaskWithType:(SPSwitchType)type requestInfo:(SPPlayingContext*)requestInfo;
- (SPSwitchRet*)driveTaskWhenVideoInfoSucessWithType:(SPSwitchType)type taskId:(long)taskId;
- (SPSwitchRet*)driveTaskWhenVideoInfoFailedWithType:(SPSwitchType)type taskId:(long)taskId;
- (SPSwitchRet*)driveTaskWhenPlayerSucessWithType:(SPSwitchType)type taskId:(long)taskId;
- (SPSwitchRet*)driveTaskWhenPlayerFailedWithType:(SPSwitchType)type taskId:(long)taskId;
- (SPSwitchRet*)findTaskByTaskId:(long)taskId;
- (SPSwitchRet*)clearWithType:(SPSwitchType)type;
@end


@interface SPPlayerWrapperSwitchModel : NSObject <ISPPlayerWrapperSwitchModel>
@end

NS_ASSUME_NONNULL_END
