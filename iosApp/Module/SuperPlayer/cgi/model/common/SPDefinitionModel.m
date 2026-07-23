/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPDefinitionModel.m
 Author      : Denzel
 Version     : 1.0
 Date        : 9/27/12
 Description :
 History     : 9/27/12 初始版本
 ***********************************************************/

#import "SPDefinitionModel.h"
#import "SPCGIDefines.h"
#import "SPUtils.h"

static NSString *const kSPDefinitionAuto  = @"auto";  //自动，由后台决定返回的清晰度（分段MP4或HLS
static NSString *const kSPDefinitionAudio = @"audio";
static NSString *const kSPDefinitionMSD   = @"msd";  //流畅（分段MP4或HLS）
static NSString *const kSPDefinitionSD    = @"sd";   //标清（分段MP4或HLS
static NSString *const kSPDefinitionMp4   = @"mp4";
static NSString *const kSPDefinitionHD    = @"hd";     //高清（分段MP4或HLS）
static NSString *const kSPDefinitionSHD   = @"shd";    //超清（分段MP4或HLS）
static NSString *const kSPDefinitionFHD   = @"fhd";    //全高清
static NSString *const kSPDefinitionHDR10 = @"hdr10";  // HDR10
static NSString *const KSPDefinitionDolby = @"dolby";

typedef NS_ENUM(NSInteger, SPDefinitionLevel) {
    SPDefinitionLevelAuto  = 0 << 0,
    SPDefinitionLevelAudio = 1 << 0,
    SPDefinitionLevelMSD   = 1 << 1,
    SPDefinitionLevelSD    = 1 << 2,
    SPDefinitionLevelMP4   = 1 << 3,  // MP4和HD的清晰度level是一样的
    SPDefinitionLevelHD    = 1 << 3,
    SPDefinitionLevelSHD   = 1 << 4,
    SPDefinitionLevelFHD   = 1 << 5,
    SPDefinitionLevelHDR10 = 1 << 6,
    SPDefinitionLevelDolby = 1 << 7,
};

@implementation SPDefinitionModel

+ (SPDefinitionModel *)definitionModelFromDict:(NSDictionary *)dict {
    SPDefinitionModel *fileInfo = [[SPDefinitionModel alloc] init];
    NSString *fileid             = [[dict objectForKey:@"id"] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    fileInfo.fileid              = fileid;
    fileInfo.filesl              = [[dict objectForKey:@"sl"] intValue];
    fileInfo.fileName            = [dict objectForKey:@"name"];
    fileInfo.fileBr              = [[dict objectForKey:@"br"] intValue];
    fileInfo.fullText            = [dict spStringForKeySafeModel:@"cname"];
    fileInfo.shortText           = [dict spStringForKeySafeModel:@"sname"];
    fileInfo.resolutionText      = [dict spStringForKeySafeModel:@"resolution"];
    fileInfo.processedFullText   = [SPDefinitionModel processFullText:fileInfo.fullText];
    fileInfo.fileLimit           = [[dict spNumberForKeySafeModel:@"lmt"] integerValue];
    fileInfo.videoFileSize       = [dict spInt64ValueForKeySafeModel:@"fs"];
    fileInfo.audio               = [[dict spNumberForKeySafeModel:@"audio"] intValue];
    fileInfo.video               = [[dict spNumberForKeySafeModel:@"video"] intValue];
    fileInfo.drm                 = [[dict spNumberForKeySafeModel:@"drm"] intValue];
    fileInfo.sr                  = [[dict spNumberForKeySafeModel:@"super"] boolValue];
    fileInfo.hdrEnhance          = [[dict spNumberForKeySafeModel:@"hdr10enh"] boolValue];

    fileInfo.isLive = NO;
    if (fileInfo.fileLimit != 0) {
        fileInfo.isVip = YES;
    }
    
    if ([fileInfo.fileName isEqualToString:@"mp4"]) {
        fileInfo.fileName = @"hd"; // mp4转hd
    }
    return fileInfo;
}

+ (NSString *)processFullText:(NSString *)fullText {
    if (fullText.length <= 0) {
        return @"";
    }
    NSArray *namesArray = [fullText componentsSeparatedByCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@";"]];

    NSString *processedFullText = @"";
    if ([namesArray isKindOfClass:[NSArray class]] && namesArray.count == 2) {
        NSString *clarityName = [namesArray objectAtIndex:0];
        NSString *clarityUnit = [namesArray objectAtIndex:1];
        if ([clarityUnit isKindOfClass:[NSString class]]) {
            clarityUnit = [clarityUnit stringByReplacingOccurrencesOfString:@"(" withString:@""];
            clarityUnit = [clarityUnit stringByReplacingOccurrencesOfString:@")" withString:@""];
        }
        processedFullText = [NSString stringWithFormatSafely:@"%@ %@", clarityName, clarityUnit];
    }
    if (!processedFullText.length) {
        return fullText;
    }
    return processedFullText;
}

+ (SPDefinitionModel *)definitionModelFromLiveDict:(NSDictionary *)dict {
    SPDefinitionModel *fileInfo = [[SPDefinitionModel alloc] init];
    fileInfo.fileid = [dict spStringForKeySafeModel:@"id"];
    fileInfo.fileName = [[dict objectForKey:@"fn"] safeString];
    fileInfo.isVip = [dict spBoolForKeySafeModel:@"vip"];
    fileInfo.fullText = [dict spStringForKeySafeModel:@"fnname"];
    fileInfo.shortText = [dict spStringForKeySafeModel:@"defnname"];
    fileInfo.resolutionText = [dict spStringForKeySafeModel:@"defnrate"];
    
    // 直播的processedFullText就等于fullText，因为直播返回的文案本来就是用空格隔开的，不用经过解析，保留processedFullText是为了兼顾老版本
    fileInfo.processedFullText = fileInfo.fullText;
    
    fileInfo.videoFileSize = [[dict spNumberForKeySafeModel:@"fs"] doubleValue];
    fileInfo.isLive = YES;
    return fileInfo;
}

+ (int)codeOfDefinitionName:(NSString *)def {
    int level = 0;
    if ([def isEqualToString:kSPDefinitionAuto]) {
        level = SPDefinitionLevelAuto;
    } else if ([def isEqualToString:kSPDefinitionAudio]) {
        level = SPDefinitionLevelAudio;
    } else if ([def isEqualToString:kSPDefinitionMSD]) {
        level = SPDefinitionLevelMSD;
    } else if ([def isEqualToString:kSPDefinitionSD]) {
        return SPDefinitionLevelSD;
    } else if ([def isEqualToString:kSPDefinitionMp4]) {
        return SPDefinitionLevelMP4;
    } else if ([def isEqualToString:kSPDefinitionHD]) {
        return SPDefinitionLevelHD;
    } else if ([def isEqualToString:kSPDefinitionSHD]) {
        return SPDefinitionLevelSHD;
    } else if ([def isEqualToString:kSPDefinitionFHD]) {
        return SPDefinitionLevelFHD;
    } else if ([def isEqualToString:kSPDefinitionHDR10]) {
        return SPDefinitionLevelHDR10;
    } else if ([def isEqualToString:KSPDefinitionDolby]) {
        return SPDefinitionLevelDolby;
    }

    return level;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"fileid:%@, fileName:%@, fileLocalName:%@,\
            processedFullText:%@, filesl:%ld,\
            fileBr:%ld, isVip:%d,fileLimit:%ld, videoFileSize:%lld,\
            isLive:%d, level:%d, audio:%d, video:%d,\
            sr:%d,\drm:%d",
            self.fileid, self.fileName, self.shortText,
            self.processedFullText, self.filesl, self.fileBr, self.isVip,
            self.fileLimit, self.videoFileSize, self.isLive, self.level,
            self.audio, self.video, self.sr, self.drm];
}

@end
