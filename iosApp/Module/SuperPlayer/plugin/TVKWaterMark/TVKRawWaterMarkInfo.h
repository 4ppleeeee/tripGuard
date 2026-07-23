/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKRawWaterMarkInfo.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/6
 Description :
 History     : 17/3/6 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

// 遮标水印：当前水印的原始位置，有些时候需要遮挡，这里承载需要遮挡的区域信息
@interface TVKRawWaterMarkBlockInfo : NSObject
/**
 遮标水印位置
 */
@property (nonatomic, assign, readonly) CGRect blockPosition;
/**
 是否遮挡logo
 */
@property (nonatomic, assign, readonly) BOOL isShow;
/**
 初始化方法
 */
- (instancetype)initWithDic:(NSDictionary *)dict;
@end

// 水印信息
@interface TVKRawWaterMarkInfo : NSObject

@property (nonatomic, assign) CGRect position;

@property (nonatomic, copy) NSString *md5;

@property (nonatomic, copy) NSString *url;

- (id)initWithDic:(NSDictionary *)dict;

@end

// 点播
@interface TVKVODWaterMarkInfo : TVKRawWaterMarkInfo

@property (nonatomic, copy) NSString *httpsUrl;
@property (nonatomic, assign) NSInteger waterMarkId;
@property (nonatomic, assign) NSInteger alpha;

+ (TVKVODWaterMarkInfo *)vodWaterMarkInfoWithDict:(NSDictionary *)dict;

+ (NSArray<__kindof TVKVODWaterMarkInfo *> *)vodWaterMarkInfoArrayWithArray:(NSArray *)array;

@end

// 直播
@interface TVKLiveWaterMarkInfo : TVKRawWaterMarkInfo

@property (nonatomic, assign) BOOL isShow;

+ (TVKLiveWaterMarkInfo *)liveWaterMarkInfoWithDict:(NSDictionary *)dict;

@end

// 动态水印信息，基类
@interface TVKActionWaterMarkScene : NSObject

@property (nonatomic, assign) int inTime;

@property (nonatomic, assign) int outTime;

@property (nonatomic, assign) int start;  // 这里的start是指repeat次数的起始次数

@property (nonatomic, assign) int end;  // 这里的end是指repeat次数的结束次数

@property (nonatomic, strong) NSArray<__kindof TVKRawWaterMarkInfo *> *waterMarkInfos;

@end

// 目前仅有点播有动态水印
@interface TVKVODActionWaterMarkScene : TVKActionWaterMarkScene

+ (TVKVODActionWaterMarkScene *)actionWaterMarkSceneWithDict:(NSDictionary *)dict;

@end

typedef NS_ENUM(NSUInteger, TVKActionWaterMarkRunMode) {
    TVKActionWaterMarkRunModeDefault                = 0,
    TVKActionWaterMarkRunModeRelativeToPlayPosition = 1,
    TVKActionWaterMarkRunModeRelativeToPlayTime     = 2,
};

@interface TVKActionWaterMarkModel : NSObject

@property (nonatomic, assign) int duration;

@property (nonatomic, assign) int start;

@property (nonatomic, assign) int rw;  // 用来计算水印位置，r = (min(视频宽，视频高)/rw)，水印实际位置 = 后台返回水印位置 * r

@property (nonatomic, assign) int repeat;

@property (nonatomic, assign) int runMode;  // 见TVKActionWaterMarkRunMode的定义

@property (nonatomic, strong) NSArray<__kindof TVKActionWaterMarkScene *> *actionWaterMarkScenes;

+ (TVKActionWaterMarkModel *)actionWaterMarkModelWithDict:(NSDictionary *)dict;

@end

@interface TVKWaterMarkModel : NSObject

@property (nonatomic, copy) NSString *actionUrl;

@property (nonatomic, strong) TVKActionWaterMarkModel *actionWaterMarkModel;

@property (nonatomic, strong) NSArray<__kindof TVKRawWaterMarkInfo *> *waterInfos;

@property (nonatomic, strong) NSArray<__kindof TVKRawWaterMarkBlockInfo *> *waterBlockInfos;

@end
