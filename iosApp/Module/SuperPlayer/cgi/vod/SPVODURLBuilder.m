//
//  SPVODURLBuilder.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/10.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVODURLBuilder.h"

@implementation SPVODURLBuilder

+ (SPSection *)buildHLSURLWithGetVInfoData:(SPGetVInfoData *)getvinfoData
                                    sdtFrom:(NSString *)sdtFrom
                                   freeFlow:(BOOL)freeFlow {
    if (freeFlow ||  // 免流不需要拼接
        (SPMediaDLTypeHLSM3U8 == getvinfoData.vodPlayInfo.dltype)) {  // mM3U8直出不需要拼接地址，直接使用
        return [self buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                            duration:getvinfoData.vodPlayInfo.duration
                                            fileSize:getvinfoData.vodPlayInfo.fileSize
                                             fileMD5:getvinfoData.vodPlayInfo.extraInfo.fMD5
                                               keyID:getvinfoData.vodPlayInfo.extraInfo.keyID];
    } else {
        return [self buildHLSURLWithUIInfoArray:getvinfoData.uiInfoArray
                                       duration:getvinfoData.vodPlayInfo.duration
                                        sdtFrom:sdtFrom];
    }
}

+ (SPSection *)buildWholeMP4URLWithGetVInfoData:(SPGetVInfoData *)getvinfoData
                                         sdtFrom:(NSString *)sdtFrom
                                        freeFlow:(BOOL)freeFlow {
    if (freeFlow) {
        return [self buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                            duration:getvinfoData.vodPlayInfo.duration
                                            fileSize:getvinfoData.vodPlayInfo.fileSize
                                             fileMD5:getvinfoData.vodPlayInfo.extraInfo.fMD5
                                               keyID:getvinfoData.vodPlayInfo.extraInfo.keyID];
    } else {
        return [self buildWholeMP4URLWithGetVInfoData:getvinfoData sdtFrom:sdtFrom];
    }
}

/**
 *  直接从vl.vi.ui节点获取地址，不拼接任何参数，免流情况下和中台之前的老的离线播放，都是直接从ui节点获取地址，不需要拼接
 *  @param uiInfoArray ui节点信息
 *  @param duration 视频时长，如果是分片，则是分片的时长
 *  @param fileSize 视频文件大小，如果是分片，则是分片的大小
 *  @param fileMD5 视频文件MD5，如果是分片，则是分片文件的MD5，TODO:调试的时候注意一下整片MP4和HLS情况下的MD5，hemanli
 *  @return SPSection实例
 */
+ (SPSection *)buildURLDirectlyFromUIInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray
                                       duration:(NSTimeInterval)duration
                                       fileSize:(int64_t)fileSize
                                        fileMD5:(NSString *)fileMD5
                                          keyID:(nonnull NSString *)keyID {
    SPSection *section = [[SPSection alloc] init];
    NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] init];
    NSMutableArray<NSString *> *vtList = [[NSMutableArray alloc] init];
    for (int i = 0; i < uiInfoArray.count; i++) {
        SPVODUIInfo *uiInfo = [uiInfoArray objectAtIndex:i];
        if (i == 0) {
            section.url = uiInfo.urlStr;
            section.index = 0;
        }
        
        [urlList addObject:uiInfo.urlStr];
        if (uiInfo.vt) {
            [vtList addObject:uiInfo.vt];
        }
    }
    
    section.duration = duration;
    section.clipSize = fileSize;
    section.clipMD5 = fileMD5;
    section.keyID = keyID;
    section.urlList = urlList;
    section.vtList = vtList;
    return section;
}

+ (SPSection *)buildHLSURLWithUIInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray
                                  duration:(NSTimeInterval)duration
                                   sdtFrom:(NSString *)sdtFrom {
    SPSection *section = [[SPSection alloc] init];
    NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] init];
    NSMutableArray<NSString *> *vtList = [[NSMutableArray alloc] init];
    for (int i = 0; i < uiInfoArray.count; i++) {
        SPVODUIInfo *uiInfo = [uiInfoArray objectAtIndex:i];
        NSString *urlStr = [NSString stringWithFormat:@"%@%@&sdtfrom=%@",
                                     uiInfo.urlStr, uiInfo.pt, sdtFrom];
        
        if (i == 0) {
            section.url = urlStr;
            section.index = 0;
        }
        
        [urlList addObject:urlStr];
        if (uiInfo.vt) {
            [vtList addObject:uiInfo.vt];
        }
    }
    
    section.duration = duration;
    section.urlList = urlList;
    section.vtList = vtList;
    return section;
}

+ (SPSection *)buildWholeMP4URLWithGetVInfoData:(SPGetVInfoData *)getvinfoData
                                         sdtFrom:(NSString *)sdtFrom {
    NSArray<SPVODUIInfo *> *uiInfoArray = getvinfoData.uiInfoArray;
    SPSection *section = [[SPSection alloc] init];
    NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] init];
    NSMutableArray<NSString *> *vtList = [[NSMutableArray alloc] init];
    for (int i = 0; i < uiInfoArray.count; i++) {
        SPVODUIInfo *uiInfo = [uiInfoArray objectAtIndex:i];
        //TODO:check br和fmt
        NSMutableString *urlStr = [NSMutableString stringWithFormat:@"%@%@", uiInfo.urlStr, getvinfoData.fileName];
        [urlStr appendFormat:@"?vkey=%@&br=%d&fmt=%@&sdtfrom=%@",
         getvinfoData.fvKey,
         getvinfoData.vodPlayInfo.rate,
         getvinfoData.vodPlayInfo.currentDefinition.fileName,
         sdtFrom];
        
        if (i == 0) {
            section.url = urlStr;
            section.index = 0;
        }
        
        [urlList addObject:urlStr];
        if (uiInfo.vt) {
            [vtList addObject:uiInfo.vt];
        }
    }
    
    section.duration = getvinfoData.vodPlayInfo.duration;
    section.urlList = urlList;
    section.vtList = vtList;
    return section;
}

+ (SPSection *)buildMP4ClipURLWith:(SPClipInfo *)clipInfo
                        uiInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray
                           fileName:(NSString *)fileName
                               vkey:(NSString *)vkey
                               rate:(int)rate
                               defn:(NSString *)defn
                            sdtFrom:(NSString *)sdtFrom {
    
    SPSection *section = [[SPSection alloc] init];
    NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] init];
    NSMutableArray<NSString *> *vtList = [[NSMutableArray alloc] init];
    for (int i = 0; i < uiInfoArray.count; i++) {
        SPVODUIInfo *uiInfo = [uiInfoArray objectAtIndex:i];
        //TODO:check br和fmt
        NSRange range = [fileName rangeOfString:@"." options:NSBackwardsSearch];
        NSString *newFileName = nil;
        if (range.location != NSNotFound) {
            newFileName = [NSString stringWithFormat:@"%@%d%@",
                           [fileName substringToIndex:range.location + 1],
                           clipInfo.index,
                           [fileName substringFromIndex:range.location]];
        }
        
        NSMutableString *urlStr = [NSMutableString stringWithFormat:@"%@%@?vkey=%@", uiInfo.urlStr, newFileName, vkey];
        [urlStr appendFormat:@"&br=%d&fmt=%@&sdtfrom=%@", rate, defn, sdtFrom];
        
        if (i == 0) {
            section.url = urlStr;
            section.index = 0;
        }
        
        [urlList addObject:urlStr];
        if (uiInfo.vt) {
            [vtList addObject:uiInfo.vt];
        }
    }
    
    
    section.urlList = urlList;
    section.vtList = vtList;
    section.duration = clipInfo.clipDuration;
    section.clipSize = clipInfo.clipSize;
    section.clipMD5 = clipInfo.md5;
    section.keyID = clipInfo.keyID;
    return section;
}
@end
