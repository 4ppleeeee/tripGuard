/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKRawWaterMarkInfo.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/6
 Description :
 History     : 17/3/6 初始版本
 ***********************************************************/

#import "TVKRawWaterMarkInfo.h"

@interface TVKRawWaterMarkBlockInfo ()
/**
 遮标水印位置
 */
@property (nonatomic, assign) CGRect blockPosition;
/**
 是否遮挡logo
 */
@property (nonatomic, assign) BOOL isShow;
@end

@implementation TVKRawWaterMarkBlockInfo : NSObject
- (instancetype)initWithDic:(NSDictionary *)dict {
    self = [self init];
    if (self) {
        NSInteger positionX = [[dict spNumberForKeySafeModel:@"x"] integerValue];
        NSInteger positionY = [[dict spNumberForKeySafeModel:@"y"] integerValue];
        NSInteger positionW = [[dict spNumberForKeySafeModel:@"w"] integerValue];
        NSInteger positionH = [[dict spNumberForKeySafeModel:@"h"] integerValue];

        self.blockPosition = CGRectMake(positionX, positionY, positionW, positionH);
        self.isShow = [dict spBoolForKeySafeModel:@"show"];
    }
    return self;
}
@end

@implementation TVKRawWaterMarkInfo

- (id)initWithDic:(NSDictionary *)dict {
    self = [self init];
    if (self) {
        NSInteger positionX = [[dict spNumberForKeySafeModel:@"x"] integerValue];
        NSInteger positionY = [[dict spNumberForKeySafeModel:@"y"] integerValue];
        NSInteger positionW = [[dict spNumberForKeySafeModel:@"w"] integerValue];
        NSInteger positionH = [[dict spNumberForKeySafeModel:@"h"] integerValue];

        self.position = CGRectMake(positionX, positionY, positionW, positionH);
        self.md5      = [dict spStringForKeySafeModel:@"md5"];
        self.url      = [dict spStringForKeySafeModel:@"url"];
    }

    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"water mark, position=(%f, %f, %f, %f), url=%@",
            self.position.origin.x, self.position.origin.y, self.position.size.width, self.position.size.height, self.url];
}

@end

@implementation TVKVODWaterMarkInfo

- (id)initWithDic:(NSDictionary *)dict {
    self = [super initWithDic:dict];
    if (self) {
        self.httpsUrl    = [dict spStringForKeySafeModel:@"surl"];
        self.waterMarkId = [[dict spNumberForKeySafeModel:@"id"] integerValue];
        self.alpha       = [[dict spNumberForKeySafeModel:@"a"] integerValue];
    }
    return self;
}

+ (TVKVODWaterMarkInfo *)vodWaterMarkInfoWithDict:(NSDictionary *)dict {
    if (dict.count <= 0) {
        return nil;
    }

    TVKVODWaterMarkInfo *vodWaterMarkInfo = [[TVKVODWaterMarkInfo alloc] initWithDic:dict];
    return vodWaterMarkInfo;
}

+ (NSArray<__kindof TVKVODWaterMarkInfo *> *)vodWaterMarkInfoArrayWithArray:(NSArray *)array {
    if (array.count <= 0) {
        return nil;
    }

    NSMutableArray<__kindof TVKVODWaterMarkInfo *> *vodWaterMarkInfos = [[NSMutableArray alloc] init];
    for (NSDictionary *waterInfoDic in array) {
        if (![waterInfoDic isKindOfClass:[NSDictionary class]]) {
            continue;
        }
        if (waterInfoDic.count <= 0) {
            continue;
        }

        TVKVODWaterMarkInfo *waterMarkInfo = [TVKVODWaterMarkInfo vodWaterMarkInfoWithDict:waterInfoDic];
        if (waterMarkInfo) {
            [vodWaterMarkInfos addObject:waterMarkInfo];
        }
    }

    return vodWaterMarkInfos;
}

@end

@implementation TVKLiveWaterMarkInfo

- (id)initWithDic:(NSDictionary *)dict {
    self = [super initWithDic:dict];
    if (self) {
        self.isShow = [[dict spNumberForKeySafeModel:@"show"] boolValue];
    }
    return self;
}
+ (TVKLiveWaterMarkInfo *)liveWaterMarkInfoWithDict:(NSDictionary *)dict {
    if (dict.count <= 0) {
        return nil;
    }

    TVKLiveWaterMarkInfo *liveWaterMarkInfo = [[TVKLiveWaterMarkInfo alloc] initWithDic:dict];
    return liveWaterMarkInfo;
}

@end

@implementation TVKActionWaterMarkScene

@end

@implementation TVKVODActionWaterMarkScene

+ (TVKVODActionWaterMarkScene *)actionWaterMarkSceneWithDict:(NSDictionary *)dict {
    TVKVODActionWaterMarkScene *actionWaterMarkInfo = [[TVKVODActionWaterMarkScene alloc] init];
    actionWaterMarkInfo.inTime                      = [[dict spNumberForKeySafeModel:@"in"] intValue];
    actionWaterMarkInfo.outTime                     = [[dict spNumberForKeySafeModel:@"out"] intValue];
    actionWaterMarkInfo.start                       = [[dict spNumberForKeySafeModel:@"start"] intValue];
    actionWaterMarkInfo.end                         = [[dict spNumberForKeySafeModel:@"end"] intValue];
    NSArray *waterInfoArray                         = [dict spArrayForKeySafeModel:@"wi"];
    actionWaterMarkInfo.waterMarkInfos              = [TVKVODWaterMarkInfo vodWaterMarkInfoArrayWithArray:waterInfoArray];
    return actionWaterMarkInfo;
}

@end

@implementation TVKActionWaterMarkModel

+ (TVKActionWaterMarkModel *)actionWaterMarkModelWithDict:(NSDictionary *)dict {
    if (dict.count <= 0) {
        return nil;
    }

    TVKActionWaterMarkModel *actionWaterMarkModel = [[TVKActionWaterMarkModel alloc] init];
    actionWaterMarkModel.duration                 = [[dict spNumberForKeySafeModel:@"duration"] intValue];
    actionWaterMarkModel.start                    = [[dict spNumberForKeySafeModel:@"start"] intValue];
    actionWaterMarkModel.rw                       = [[dict spNumberForKeySafeModel:@"rw"] intValue];
    actionWaterMarkModel.repeat                   = [[dict spNumberForKeySafeModel:@"repeat"] intValue];
    actionWaterMarkModel.runMode                  = [[dict spNumberForKeySafeModel:@"runmod"] intValue];
    NSArray *sceneArray                           = [dict spArrayForKeySafeModel:@"scenes"];
    NSMutableArray *actionSceneArray              = [[NSMutableArray alloc] initWithCapacity:sceneArray.count];
    for (NSDictionary *sceneDict in sceneArray) {
        if (![sceneDict isKindOfClass:[NSDictionary class]]) {
            continue;
        }

        TVKVODActionWaterMarkScene *actionScene = [TVKVODActionWaterMarkScene actionWaterMarkSceneWithDict:sceneDict];
        [actionSceneArray addObject:actionScene];
    }

    actionWaterMarkModel.actionWaterMarkScenes = actionSceneArray;
    return actionWaterMarkModel;
}

@end

@implementation TVKWaterMarkModel

@end
