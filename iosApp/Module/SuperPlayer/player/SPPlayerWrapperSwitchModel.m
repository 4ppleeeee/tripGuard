//
//  SPPlayerWrapperSwitchModel.m
//  SPPlayer
//
//  Created by 郭力 on 2019/10/8.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperSwitchModel.h"
#import "SPVcSystemInfo.h"

typedef NS_ENUM(NSUInteger, SPSwitchTaskState) {
    SPSwitchTaskStateVideoInfo = 0,
    SPSwitchTaskStateSwitching = 1,
    SPSwitchTaskStateComplete  = 2,
};

@interface SPSwitchTask : NSObject
@property (nonatomic, assign) long taskId;
@property (nonatomic, assign) long long taskTime;
@property (nonatomic, assign) SPSwitchType type;
@property (nonatomic, assign) SPSwitchTaskState state;
@property (nonatomic, strong) SPPlayingContext *info;
@end

@protocol ISPInternalSwitchModel <NSObject>
@required
- (SPSwitchRet*)driveAddOneNewTask:(SPPlayingContext*)requestInfo idBase:(long)idBase;
- (SPSwitchRet*)driveTaskWhenVideoInfoSucess:(long)taskId;
- (SPSwitchRet*)driveTaskWhenVideoInfoFailed:(long)taskId;
- (SPSwitchRet*)driveTaskWhenPlayerSucess:(long)taskId;
- (SPSwitchRet*)driveTaskWhenPlayerFailed:(long)taskId;
- (SPSwitchTask*)findTaskByTaskId:(long)taskId;
- (SPSwitchTask*)findTaskByRequestInfo:(SPPlayingContext*)requestInfo;
- (BOOL)isLatestTask:(SPSwitchTask*)task;
- (void)clean;
@end

@interface SPDefinitionSwitchModel : NSObject <ISPInternalSwitchModel>
@property (nonatomic, readonly, assign) SPSwitchType type;
@property (nonatomic, strong) NSMutableArray<SPSwitchTask*> *tasks;
@end

@interface SPPlayerWrapperSwitchModel ()
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, id<ISPInternalSwitchModel>> *models;
@property (nonatomic, assign) long idBase;
@end

@implementation SPSwitchTask
@end

@implementation SPSwitchRet
@end

#pragma mark - switch models

@implementation SPPlayerWrapperSwitchModel

- (instancetype)init {
    if (self = [super init]) {
        _idBase = KTVWrapperSwitchBaseOpaqu;
        _models = [[NSMutableDictionary alloc] init];
        [_models setObject:[[SPDefinitionSwitchModel alloc]init] forKey:@(SPSwitchTypeDefinition)];
    }
    return self;
}

- (long)generateBaseId {
    return _idBase++;
}

- (nonnull SPSwitchRet *)driveAddOneNewTaskWithType:(SPSwitchType)type requestInfo:(nonnull SPPlayingContext *)requestInfo {
    return [[self.models objectForKey:@(type)] driveAddOneNewTask:requestInfo idBase:[self generateBaseId]];
}

- (nonnull SPSwitchRet *)driveTaskWhenVideoInfoSucessWithType:(SPSwitchType)type taskId:(long)taskId {
    return [[self.models objectForKey:@(type)] driveTaskWhenVideoInfoSucess:taskId];
}

- (nonnull SPSwitchRet *)driveTaskWhenVideoInfoFailedWithType:(SPSwitchType)type taskId:(long)taskId {
    return [[self.models objectForKey:@(type)] driveTaskWhenVideoInfoFailed:taskId];
}

- (nonnull SPSwitchRet *)driveTaskWhenPlayerSucessWithType:(SPSwitchType)type taskId:(long)taskId {
    return [[self.models objectForKey:@(type)] driveTaskWhenPlayerSucess:taskId];
}

- (nonnull SPSwitchRet *)driveTaskWhenPlayerFailedWithType:(SPSwitchType)type taskId:(long)taskId {
    return [[self.models objectForKey:@(type)] driveTaskWhenPlayerFailed:taskId];
}

- (nonnull SPSwitchRet *)clearWithType:(SPSwitchType)type {
    [[self.models objectForKey:@(type)] clean];
    return [[SPSwitchRet alloc] init];
}

- (nonnull SPSwitchRet *)findTaskByTaskId:(long)taskId {
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPDefinitionSwitchModel *definitionModel = [self.models objectForKey:@(SPSwitchTypeDefinition)];
    
    for (SPSwitchTask *task in definitionModel.tasks) {
        if (taskId == task.taskId) {
            ret.code = SPSwitchRetCodeOK;
            ret.info = task.info;
            ret.type = task.type;
            ret.taskId = task.taskId;
            return ret;
        }
    }
        
    ret.taskId = -1;
    ret.code = SPSwitchRetCodeNoneExistentTask;
    return ret;
}

@end

#pragma mark - definition switch model

@implementation SPDefinitionSwitchModel

- (instancetype)init {
    if (self = [super init]) {
        _type = SPSwitchTypeDefinition;
        _tasks = [[NSMutableArray alloc] init];
    }
    return self;
}

- (nonnull SPSwitchRet *)driveAddOneNewTask:(nonnull SPPlayingContext *)requestInfo idBase:(long)idBase{
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPSwitchTask *dTask = [self findTaskByRequestInfo:requestInfo];
    
    if (dTask != nil && dTask.state == SPSwitchTaskStateSwitching && [self isLatestTask:dTask]) {
        ret.taskId = dTask.taskId;
        ret.info = dTask.info;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeDuplicateTask;
        dTask.taskTime = [[SPVcSystemInfo sharedInstance] currentTime];
        return ret;
    }
    
    if (dTask != nil && dTask.type == SPSwitchTaskStateVideoInfo && [self isLatestTask:dTask]) {
        ret.taskId = dTask.taskId;
        ret.info = dTask.info;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeDuplicateTask;
        dTask.taskTime = [[SPVcSystemInfo sharedInstance] currentTime];
        return ret;
    }
    
    if (dTask != nil) {
    }
    
    SPSwitchTask *task = [[SPSwitchTask alloc] init];
    task.type = self.type;
    task.taskId = idBase;
    task.state = SPSwitchTaskStateVideoInfo;
    task.info = [requestInfo copy];
    task.taskTime = [[SPVcSystemInfo sharedInstance] currentTime];
    [self.tasks addObject:task];
    
    ret.taskId = task.taskId;
    ret.type = self.type;
    ret.info = task.info;
    ret.code = SPSwitchRetCodeOK;
    
    return ret;
}

- (nonnull SPSwitchRet *)driveTaskWhenVideoInfoSucess:(long)taskId {
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPSwitchTask *task = [self findTaskByTaskId:taskId];
    
    if (task == nil) {
        ret.taskId = -1;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNoneExistentTask;
        return ret;
    }
    
    if (![self isLatestTask:task]) {
        ret.taskId = task.taskId;
        ret.info = task.info;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNotLastestTask;
        task.state = SPSwitchTaskStateComplete;
        return ret;
    }
    
    ret.taskId = taskId;
    ret.info = task.info;
    ret.type = self.type;
    ret.code = SPSwitchRetCodeOK;
    task.state = SPSwitchTaskStateSwitching;
    return ret;
}

- (nonnull SPSwitchRet *)driveTaskWhenVideoInfoFailed:(long)taskId {
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPSwitchTask *task = [self findTaskByTaskId:taskId];
    
    if (task == nil) {
        ret.taskId = -1;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNoneExistentTask;
        return ret;
    }
    
    if (![self isLatestTask:task]) {
        ret.taskId = task.taskId;
        ret.info = task.info;
        ret.code = SPSwitchRetCodeNotLastestTask;
        ret.type = self.type;
        task.state = SPSwitchTaskStateComplete;
        return ret;
    }
    
    ret.taskId = task.taskId;
    ret.info = task.info;
    ret.type = self.type;
    ret.code = SPSwitchRetCodeOK;
    task.state = SPSwitchTaskStateComplete;
    
    [self clean];
    return ret;
}


- (nonnull SPSwitchRet *)driveTaskWhenPlayerSucess:(long)taskId {
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPSwitchTask *task = [self findTaskByTaskId:taskId];
    
    if (task == nil) {
        ret.taskId = -1;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNoneExistentTask;
        return ret;
    }
    
    if (![self isLatestTask:task]) {
        ret.taskId = task.taskId;
        ret.info = task.info;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNotLastestTask;
        task.state = SPSwitchTaskStateComplete;
        return ret;
    }
    
    ret.taskId = task.taskId;
    ret.info = task.info;
    ret.type = self.type;
    ret.code = SPSwitchRetCodeOK;
    task.state = SPSwitchTaskStateComplete;
    
    [self clean];
    return ret;
}

- (nonnull SPSwitchRet *)driveTaskWhenPlayerFailed:(long)taskId {
    SPSwitchRet *ret = [[SPSwitchRet alloc] init];
    SPSwitchTask *task = [self findTaskByTaskId:taskId];
    
    if (task == nil) {
        ret.taskId = -1;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNoneExistentTask;
        return ret;
    }
    
    if (![self isLatestTask:task]) {
        ret.taskId = task.taskId;
        ret.info = task.info;
        ret.type = self.type;
        ret.code = SPSwitchRetCodeNotLastestTask;
        return ret;
    }
    
    ret.taskId = task.taskId;
    ret.info = task.info;
    ret.type = self.type;
    ret.code = SPSwitchRetCodeOK;
    task.state = SPSwitchTaskStateComplete;
    
    [self clean];
    return ret;
}

- (SPSwitchTask *)findTaskByRequestInfo:(SPPlayingContext *)requestInfo {
    for (SPSwitchTask *task in self.tasks) {
        if ([self isDuplicateTask:task withRequestInfo:requestInfo]) {
            return task;
        }
    }
    return nil;
}

- (BOOL)isDuplicateTask:(SPSwitchTask *)task withRequestInfo:(SPPlayingContext *)requestInfo {
    return (task.type == self.type) &&
           ([task.info.requiredDefinition isEqual:requestInfo.requiredDefinition]) &&
    (task.info.enableHEVC == requestInfo.enableHEVC) &&
    (task.info.enableFairPlay == requestInfo.enableFairPlay);
}

- (BOOL)isLatestTask:(SPSwitchTask *)task {
    BOOL latest = YES;
    for (SPSwitchTask *_task in self.tasks) {
        if (task == _task) {
            continue;
        }
        if (task.type != _task.type) {
            continue;
        }
        if (task.taskTime < _task.taskTime) {
            latest = false;
            break;
        }
    }
    
    return latest;
}

- (SPSwitchTask *)findTaskByTaskId:(long)taskId {
    for (SPSwitchTask *task in self.tasks) {
        if (task.taskId == taskId) {
            return task;
        }
    }
    
    return nil;
}

- (void)clean {
    [self.tasks removeAllObjects];
}

@end
