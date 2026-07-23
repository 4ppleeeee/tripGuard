/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPDefinitionModel.h
 Author      : Denzel
 Version     : 1.0
 Date        : 9/27/12
 Description :
 History     : 9/27/12 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@interface SPDefinitionModel : NSObject

@property (atomic, copy) NSString *fileid;            // 通常所说的FormatId
@property (nonatomic, copy) NSString *fileName;             //清晰度名字，sd、hd、shd、fhd...
@property (nonatomic, copy) NSString *fullText;             //清晰度显示完整文案，例：蓝光;(1080P VIP尊享)，投传给外部使用，播放器不理解
@property (nonatomic, copy) NSString *shortText;            //清晰显示短文案，标清、高清、超清、蓝光...
@property (nonatomic, copy) NSString *resolutionText;       //分辨率文本，比如：270P、480P、720P、1080P、HDR

//@deprecated use fullText instead。清晰度显示完整文案，由fullText解析而来，去掉了分号和括号，分号替换成空格，但这步解析不应该放在播放器内部，所以此字段不再推荐使用，请使用fullText
@property (nonatomic, copy) NSString *processedFullText;

@property (nonatomic, assign) NSInteger filesl;  //当前清晰度是否被选中，即要要播放的清晰度

@property (nonatomic, assign) NSInteger fileBr;  //码率

@property (nonatomic, assign) BOOL isVip;            //是否是付费清晰度
@property (nonatomic, assign) NSInteger fileLimit;   //清晰度权限标记 4.1.1
@property (nonatomic, assign) int64_t videoFileSize;  //对应文件大小
@property (nonatomic, assign) BOOL isLive;           //是否是直播清晰度
@property (nonatomic, assign) int level;             //该清晰度对应的level，仅用于排序

@property (nonatomic, assign) int audio;  //音频编码，1:AAC，2:Dolby Surround，3:Dolby Atmos
@property (nonatomic, assign) int video;  //视频编码，1:H264, 2:H265, 3:HDR10, 4:DolbyVision

@property (nonatomic, assign) int drm;  // 0:非加密视频 1:drm加密视频 2:数字太和drm 3:数字太和drm 8:hls加密视频 4:fairplay加密，(注意，这是getvinfo返回

@property (nonatomic, assign) BOOL sr;          // 是否可以做超分运算
@property (nonatomic, assign) BOOL hdrEnhance;  // 是否需要应用SDR增强为HDR

/**
 * 从后台返回的点播数据中解析清晰度信息
 * @param dict 从后台返回数据中解析出的清晰度字段
 * @return 一个SPDefinitionModel实例
 */
+ (SPDefinitionModel *)definitionModelFromDict:(NSDictionary *)dict;

/**
 * 从后台返回的直播数据中解析清晰度信息
 * @param dict 从后台返回数据中解析出的清晰度字段
 * @return 一个SPDefinitionModel实例
 */
+ (SPDefinitionModel *)definitionModelFromLiveDict:(NSDictionary *)dict;

+ (int)codeOfDefinitionName:(NSString *)def;

+ (NSString *)processFullText:(NSString *)fullText;

@end
