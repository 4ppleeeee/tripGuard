//
//  SPVODInfoParser.m
//  SPPlayer
//
//  Created by liyukuan on 2019/9/26.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVODInfoParser.h"
#import "NSDictionary+SPXMLDictionary.h"
#import "SPCGIErrorModel.h"
#import "SPVODURLBuilder.h"

//水印
#import "SPMediaPlayInfo+waterMark.h"
#import "TVKRawWaterMarkInfo.h"

@interface SPVODInfoParser ()

+ (SPGetVInfoData *)parseSuccessData:(NSDictionary *)root;

+ (SPGetVInfoData *)parseFailedData:(NSDictionary *)root;

@end

@implementation SPVODInfoParser

+ (SPGetVInfoData *)parseGetVInfoXMLString:(NSString *)xmlString {
    NSDictionary *root = [NSDictionary spDictionaryWithXMLString:xmlString];
    NSString *code = [root[@"s"] safeString];
    if ([code isEqualToString:@"o"]) {
        SPGetVInfoData *getvinfoData = [SPVODInfoParser parseSuccessData:root];
        getvinfoData.vodPlayInfo.extraInfo.xmlString = xmlString;  // 仅离线下载时给下载组件用
        return getvinfoData;
    } else {
        return [SPVODInfoParser parseFailedData:root];
    }
}

+ (SPGetVInfoData *)parseSuccessData:(NSDictionary *)root {
    SPGetVInfoData *getvinfoData = [[SPGetVInfoData alloc] init];
    SPVODPlayInfo *vodPlayInfo = [[SPVODPlayInfo alloc] init];
    vodPlayInfo.extraInfo = [[SPVODExtraInfo alloc] init];
    
    getvinfoData.vodPlayInfo = vodPlayInfo;
    
    int dlType = [[root spNumberForKeySafeModel:@"dltype"] intValue];
    SPCGIErrorModel *errorModel = [[SPCGIErrorModel alloc] init];
    errorModel.em = 0;
    
    vodPlayInfo.exem = [[root spNumberForKeySafeModel:@"exem"] intValue];
    vodPlayInfo.fp2p = [[root spNumberForKeySafeModel:@"fp2p"] intValue];
    vodPlayInfo.vodPreViewTime = [root spFloatValueForKeySafeModel:@"preview"];
    vodPlayInfo.vodPreviewStart = [root spFloatValueForKeySafeModel:@"startpreview"];
    vodPlayInfo.extraInfo.tstId = [root spStringForKeySafeModel:@"tstid"];
    vodPlayInfo.extraInfo.ip = [root spStringForKeySafeModel:@"ip"];
    vodPlayInfo.extraInfo.tm = [root spInt64ValueForKeySafeModel:@"tm"];
    
    NSDictionary *vlNote =[root spDictionaryForKeySafeModel:@"vl"];
    
    id viNode = [vlNote objectForKey:@"vi"];
    NSDictionary *viDict = nil;
    if ([viNode isKindOfClass:[NSArray class]]) {
        viDict = [viNode firstObject];
    } else if ([viNode isKindOfClass:[NSDictionary class]]) {
        viDict = (NSDictionary *)viNode;
    } else {
        // 不太可能是其他情况
    }

    // 解析vi节点下信息
    [self parseVINote:viDict getvinfoData:getvinfoData];
    
    // 解析清晰度列表
    [self parseDefinitionListWithRoot:root vodPlayInfo:vodPlayInfo];
    
    // 解析分片信息
    [self parseClipInfoWithViDict:viDict
                     getvinfoData:getvinfoData
                      vodPlayInfo:vodPlayInfo
                           dlType:dlType];
    
    // ui节点
    NSDictionary *ulDict = [viDict spDictionaryForKeySafeModel:@"ul"];
    NSArray *uiArray = [self getArrayNode:ulDict key:@"ui"];
    getvinfoData.uiInfoArray = [self parseUIInfo:uiArray];
    
    if (SPMediaDLTypeHLSM3U8 == dlType) {
        vodPlayInfo.m3u8 = [ulDict spStringForKeySafeModel:@"m3u8"];
        if (vodPlayInfo.m3u8.length == 0) {
            dlType = SPMediaDLTypeHLS;
        }
    }
    
    vodPlayInfo.dltype = dlType;
    
    // 水印
    NSDictionary *wlDict = [viDict spDictionaryForKeySafeModel:@"wl"];
    vodPlayInfo.waterMarkModel = [self parseWaterMarkInfoDic:wlDict];
    
    // 遮标水印
    NSDictionary *llDict = [viDict spDictionaryForKeySafeModel:@"ll"];
    [self parseWaterMarkBlockInfoWithDic:llDict toWaterMarkModel:vodPlayInfo.waterMarkModel];
    
    // DRM的解析要等清晰度列表解析完
    [self parseDRMInfo:viDict vodPlayInfo:vodPlayInfo];
        
    return getvinfoData;
}

+ (void)parseClipInfoWithViDict:(NSDictionary *)viDict
                   getvinfoData:(SPGetVInfoData *)getvinfoData
                    vodPlayInfo:(SPVODPlayInfo *)vodPlayInfo
                         dlType:(SPMediaDLType) dlType {
    NSDictionary *clDict = [viDict spDictionaryForKeySafeModel:@"cl"];
    vodPlayInfo.clipCount = [[clDict spNumberForKeySafeModel:@"fc"] intValue];
    if (SPMediaDLTypeHttp == dlType) {
        if (vodPlayInfo.clipCount > 0) { //分片MP4
            vodPlayInfo.mediaType = SPMediaFormatMultiMp4;
            NSArray *ciArray = [self getArrayNode:clDict key:@"ci"];
            getvinfoData.clipInfoArray = [self parseClipInfo:ciArray];
        } else { // 整片MP4
            vodPlayInfo.mediaType = SPMediaFormatOneMp4;
        }
    } else if (SPMediaDLTypeHLS == dlType || SPMediaDLTypeHLSM3U8 == dlType) {
        vodPlayInfo.mediaType = SPMediaFormatHLS;
    }
}

+ (SPGetVInfoData *)parseFailedData:(NSDictionary *)root {
    SPGetVInfoData *getvinfoData = [[SPGetVInfoData alloc] init];
    getvinfoData.cgiErrorModel = [self parseErrorInfo:root];
    return getvinfoData;
}

+ (SPCGIErrorModel *)parseErrorInfo:(NSDictionary *)root {
    SPCGIErrorModel *errorModel = [[SPCGIErrorModel alloc] init];
    errorModel.em = [[root spNumberForKeySafeModel:@"em"] intValue];
    errorModel.errMsg = [root spStringForKeySafeModel:@"msg"];
    errorModel.exem = [[root spNumberForKeySafeModel:@"exem"] intValue];
    errorModel.exInfo = [root spStringForKeySafeModel:@"exinfo"];
    if (errorModel.em == 85 && errorModel.exem == -3) {
        errorModel.curSeverTime = [[root spNumberForKeySafeModel:@"curTime"] intValue];
        errorModel.randFlag = [root spStringForKeySafeModel:@"rand"];
    }
    errorModel.needRetry = [root spBoolForKeySafeModel:@"retry"];  // 应该只有gevinfo会返回这个字段，getvbkey没有
    return errorModel;
}

+ (void)parseVINote:(NSDictionary *)viDict getvinfoData:(SPGetVInfoData *)getvinfoData {
    SPVODPlayInfo *vodPlayInfo = getvinfoData.vodPlayInfo;
    vodPlayInfo.vid = [viDict spStringForKeySafeModel:@"vid"];
    vodPlayInfo.isHevc = [viDict spBoolForKeySafeModel:@"hevc"];
    vodPlayInfo.rate = [[viDict spNumberForKeySafeModel:@"br"] intValue];
    vodPlayInfo.chargeState = [[viDict spNumberForKeySafeModel:@"ch"] intValue]; //付费状态
    vodPlayInfo.videoState = [[viDict spNumberForKeySafeModel:@"st"] intValue];
    vodPlayInfo.link = [viDict spStringForKeySafeModel:@"lnk"];
    vodPlayInfo.videoType = [[viDict spNumberForKeySafeModel:@"videotype"] intValue];
    vodPlayInfo.extraInfo.type = [[viDict spNumberForKeySafeModel:@"type"] intValue];
    vodPlayInfo.startPosition = [[viDict spNumberForKeySafeModel:@"head"] intValue];
    vodPlayInfo.skipEndPosition = [[viDict spNumberForKeySafeModel:@"tail"] intValue];
    vodPlayInfo.aspectRatio = [viDict spFloatValueForKeySafeModel:@"wh"];
    vodPlayInfo.vr = [[viDict spNumberForKeySafeModel:@"vr"] intValue];
    vodPlayInfo.mediaState = [[viDict spNumberForKeySafeModel:@"mst"] intValue];
    float vw = [viDict spFloatValueForKeySafeModel:@"vw"];
    float vh = [viDict spFloatValueForKeySafeModel:@"vh"];
    vodPlayInfo.videoSize = CGSizeMake(vw, vh);
    vodPlayInfo.duration = [[viDict spNumberForKeySafeModel:@"td"] intValue];
    // 视频文件大小，注意：多个分片的情况下，返回的是总的大小，不是第一个分片的大小
    vodPlayInfo.fileSize = [[viDict spNumberForKeySafeModel:@"fs"] intValue];
    vodPlayInfo.fVideo = [viDict spBoolForKeySafeModel:@"fvideo"];
    vodPlayInfo.sshot = [[viDict spNumberForKeySafeModel:@"sshot"] intValue];
    vodPlayInfo.mshot = [[viDict spNumberForKeySafeModel:@"mshot"] intValue];
    
    vodPlayInfo.extraInfo.ct = [[viDict spNumberForKeySafeModel:@"ct"] intValue];
    vodPlayInfo.extraInfo.keyID = [viDict spStringForKeySafeModel:@"keyid"];
    vodPlayInfo.extraInfo.base = [viDict spStringForKeySafeModel:@"base"];
    // 视频文件MD5，如果是多个分片，则返回的是第一个分片的
    vodPlayInfo.extraInfo.fMD5 = [viDict spStringForKeySafeModel:@"fmd5"];
    vodPlayInfo.extraInfo.enc = [[viDict spNumberForKeySafeModel:@"enc"] intValue];
        
    getvinfoData.fileName = [viDict spStringForKeySafeModel:@"fn"];
    getvinfoData.fvKey = [viDict spStringForKeySafeModel:@"fvkey"];
    getvinfoData.fsha = [viDict spStringForKeySafeModel:@"fsha"];
}

+ (void)parseDefinitionListWithRoot:(NSDictionary *)root vodPlayInfo:(SPVODPlayInfo *)vodPlayInfo {
    NSArray *fiArray = [self getArrayNode:[root objectForKey:@"fl"] key:@"fi"];
    if (fiArray.count == 0) {
        return;
    }
    
    NSMutableArray<SPDefinitionModel *> *defnList = [[NSMutableArray alloc] initWithCapacity:fiArray.count];
    SPDefinitionModel *currentDefnModel = nil;
    for (NSDictionary *fiDict in fiArray) {
        SPDefinitionModel *defnModel = [SPDefinitionModel definitionModelFromDict:fiDict];
        [defnList addObject:defnModel];
        if (defnModel.filesl == 1) {
            currentDefnModel = defnModel;
        }
    }
    
    vodPlayInfo.defnModelList = defnList;
    vodPlayInfo.currentDefinition = currentDefnModel;
}

+ (NSArray<SPVODUIInfo *> *)parseUIInfo:(NSArray *)uiArray {
    if (uiArray.count == 0) {
        return nil;
    }
    
    NSMutableArray<SPVODUIInfo *> *uiInfoArray = [[NSMutableArray alloc] initWithCapacity:uiArray.count];
    for (NSDictionary *uiDict in uiArray) {
        SPVODUIInfo *uiInfo = [[SPVODUIInfo alloc] init];
        uiInfo.urlStr = [uiDict spStringForKeySafeModel:@"url"];
        uiInfo.vt = [uiDict spStringForKeySafeModel:@"vt"];
        NSDictionary *hlsDict = [uiDict spDictionaryForKeySafeModel:@"hls"];
        uiInfo.pt = [hlsDict spStringForKeySafeModel:@"pt"];
        
        //免流相关参数，只有免流情况下getvinfo返回这三个字段，请求getvbkey时用到。(HSL请求不需要)
        uiInfo.spip = [uiDict spStringForKeySafeModel:@"spip"];
        uiInfo.spport = [uiDict spStringForKeySafeModel:@"spport"];
        uiInfo.path = [uiDict spStringForKeySafeModel:@"path"];
        [uiInfoArray addObject:uiInfo];
    }
    
    return uiInfoArray;
}

+ (void)parseDRMInfo:(NSDictionary *)viDict vodPlayInfo:(SPVODPlayInfo *)vodPlayInfo {
    NSString *ckc = [viDict[@"ckc"] safeString];
    vodPlayInfo.drmModel = [SPDrmModel drmModelByParseCKCField:ckc drm:vodPlayInfo.currentDefinition.drm];
}

+ (NSArray<SPClipInfo *> *)parseClipInfo:(NSArray *)ciArray {
    if (ciArray.count == 0) {
        return nil;
    }
    
    NSMutableArray<SPClipInfo *> *clipInfoArray = [[NSMutableArray alloc] initWithCapacity:ciArray.count];
    for (NSDictionary *ciDict in ciArray) {
        SPClipInfo *clipInfo = [[SPClipInfo alloc] init];
        clipInfo.index = [[ciDict spNumberForKeySafeModel:@"idx"] intValue];
        clipInfo.clipDuration = [ciDict spFloatValueForKeySafeModel:@"cd"];
        clipInfo.clipSize = [[ciDict spNumberForKeySafeModel:@"cs"] longLongValue];
        clipInfo.md5 = [ciDict spStringForKeySafeModel:@"cmd5"];
        clipInfo.keyID = [ciDict spStringForKey:@"keyid"];
        [clipInfoArray addObject:clipInfo];
    }
    
    return clipInfoArray;
}

+ (TVKWaterMarkModel *)parseWaterMarkInfoDic:(NSDictionary *)wlDiction
{
    if (wlDiction.count == 0) {
        return nil;
    }
    
    TVKWaterMarkModel *waterMarkModel = [[TVKWaterMarkModel alloc] init];
    NSString *actionUrl = [wlDiction spStringForKeySafeModel:@"action"];
    if (actionUrl.length == 0) { // 无动态水印
        NSArray *wiNode = [self getArrayNode:wlDiction key:@"wi"];
        waterMarkModel.waterInfos = [TVKVODWaterMarkInfo vodWaterMarkInfoArrayWithArray:wiNode];
    } else {
        waterMarkModel.actionUrl = actionUrl;
    }
    
    return waterMarkModel;
}

+ (void)parseWaterMarkBlockInfoWithDic:(NSDictionary *)llDic toWaterMarkModel:(TVKWaterMarkModel *)waterMarkModel {
    if (llDic.count == 0) {
        return;
    }
    NSArray *liNode = [self getArrayNode:llDic key:@"li"];
    NSMutableArray<TVKRawWaterMarkBlockInfo *> *blockInfoArray = [[NSMutableArray alloc] init];
    [liNode enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *blockInfoDic = (NSDictionary *)obj;
        TVKRawWaterMarkBlockInfo *blockInfo = [[TVKRawWaterMarkBlockInfo alloc] initWithDic:blockInfoDic];
        [blockInfoArray addObject:blockInfo];
    }];
    waterMarkModel.waterBlockInfos = [blockInfoArray copy];
}

+ (SPGetVBKeyData *)parseGetVBKeyString:(NSString *)xmlString freeFlow:(BOOL)freeFlow {
    NSDictionary *root = [NSDictionary spDictionaryWithXMLString:xmlString];
    NSString *code = [root[@"s"] safeString];
    if ([code isEqualToString:@"o"]) {
        return [self parseVBKeySuccessData:root freeFLow:freeFlow];
    } else {
        return [self parseVBKeyFailedData:root];
    }
}

+ (SPGetVBKeyData *)parseVBKeySuccessData:(NSDictionary *)root freeFLow:(BOOL)freeFlow {
    NSDictionary *vlNode =[root spDictionaryForKeySafeModel:@"vl"];
    NSDictionary *viDict = nil;
    id viNode = [vlNode objectForKey:@"vi"];
    if ([viNode isKindOfClass:[NSArray class]]) {
        viDict = [viNode firstObject];
    } else if ([viNode isKindOfClass:[NSDictionary class]]) {
        viDict = (NSDictionary *)viNode;
    } else {
        // 不太可能是其他情况
    }

    NSDictionary *clDict = [viDict spDictionaryForKeySafeModel:@"cl"];
    NSArray *ciArray = [self getArrayNode:clDict key:@"ci"];
    
    SPGetVBKeyData *vkeyData = [[SPGetVBKeyData alloc] init];
    vkeyData.fileName = [viDict spStringForKeySafeModel:@"filename"];
    
    int maxClipIndex = 0;
    NSMutableDictionary<NSNumber *, SPClipInfo *> *clipInfoDict = [[NSMutableDictionary alloc] initWithCapacity:ciArray.count];
    for (NSDictionary *ciDict in ciArray) {
        SPClipInfo *clipInfo = [[SPClipInfo alloc] init];
        clipInfo.index = [[ciDict spNumberForKeySafeModel:@"idx"] intValue];
        clipInfo.vkey = [ciDict spStringForKeySafeModel:@"key"];
        clipInfo.keyID = [ciDict spStringForKeySafeModel:@"keyid"];
        clipInfo.sha = [ciDict spStringForKeySafeModel:@"sha"];
        if (maxClipIndex < clipInfo.index) {
            maxClipIndex = clipInfo.index;
        }
        
        if (freeFlow) {
            NSArray *uiArray = [[ciDict objectForKey:@"ul"] objectForKey:@"ui"];
            NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] init];
            for (NSDictionary *uiDict in uiArray) {
                NSString *urlStr = [uiDict objectForKey:@"url"];
                [urlList addObject:urlStr];
            }
            
            clipInfo.urlList = urlList;
        }
        
        [clipInfoDict setObject:clipInfo forKey:@(clipInfo.index)];
    }
    
    vkeyData.clipInfoDict = clipInfoDict;
    vkeyData.maxClipIndex = maxClipIndex;
    
    return vkeyData;
}

+ (SPGetVBKeyData *)parseVBKeyFailedData:(NSDictionary *)root {
    SPGetVBKeyData *vbkeyData = [[SPGetVBKeyData alloc] init];
    vbkeyData.cgiErrorModel = [self parseErrorInfo:root];
    return vbkeyData;
}

+ (NSArray *)getArrayNode:(NSDictionary *)parent key:(NSString *)key {
    if (!parent) {
        return nil;
    }
    
    //发现本来为数组的节点，xml转NSDictionary的时候，没有转成array，而是单个元素
    id node = [parent objectForKey:key];
    if ([node isKindOfClass:[NSArray class]]) {
        return node;
    } else if (node != nil){
        return @[node];
    }
    
    return nil;
}

/* parse offline begin */
+ (SPGetVInfoData *)parseOfflineGetVInfoXMLString:(NSString *)xmlString {
    SPGetVInfoData *getvinfoData = [self parseGetVInfoXMLString:xmlString];
    if (SPMediaDLTypeHLS == getvinfoData.vodPlayInfo.dltype ||
        SPMediaDLTypeHLSM3U8 == getvinfoData.vodPlayInfo.dltype) {
        [self buildOfflineHLSURLWithGetVInfoData:getvinfoData];
    } else {
        [self buildOfflineMP4URLWithGetVInfoData:getvinfoData];
    }
    
    return getvinfoData;
}

+ (void)buildOfflineHLSURLWithGetVInfoData:(SPGetVInfoData *)getvinfoData {
    SPSection *section = [SPVODURLBuilder buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                                                   duration:getvinfoData.vodPlayInfo.duration
                                                                   fileSize:getvinfoData.vodPlayInfo.fileSize
                                                                    fileMD5:getvinfoData.vodPlayInfo.extraInfo.fMD5
                                                                      keyID:getvinfoData.vodPlayInfo.extraInfo.keyID];
    getvinfoData.vodPlayInfo.sectionArray = @[section];
}

+ (void)buildOfflineMP4URLWithGetVInfoData:(SPGetVInfoData *)getvinfoData {
    if (getvinfoData.vodPlayInfo.clipCount == 0) {
        [self buildOfflineWholeMP4WithGetVInfoData:getvinfoData];
    } else {
        [self buildOfflineMultiMP4WithGetVInfoData:getvinfoData];
    }
}

+ (void)buildOfflineWholeMP4WithGetVInfoData:(SPGetVInfoData *)getvinfoData {
    SPSection *section = [SPVODURLBuilder buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                                                   duration:getvinfoData.vodPlayInfo.duration
                                                                   fileSize:getvinfoData.vodPlayInfo.fileSize
                                                                    fileMD5:getvinfoData.vodPlayInfo.extraInfo.fMD5
                                                                      keyID:getvinfoData.vodPlayInfo.extraInfo.keyID];
    getvinfoData.vodPlayInfo.sectionArray = @[section];
}

+ (void)buildOfflineMultiMP4WithGetVInfoData:(SPGetVInfoData *)getvinfoData {
    NSArray<SPClipInfo *> *clipInfoArray = getvinfoData.clipInfoArray;
    NSMutableArray<SPSection *> *sectionArray = [[NSMutableArray alloc] init];
    for (SPClipInfo *clipInfo in clipInfoArray) {
        SPSection *section = [SPVODURLBuilder buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                                                       duration:clipInfo.clipDuration
                                                                       fileSize:clipInfo.clipSize
                                                                        fileMD5:clipInfo.md5
                                                                          keyID:clipInfo.keyID];
        [sectionArray addObject:section];
    }
    
    getvinfoData.vodPlayInfo.sectionArray = sectionArray;
}

/* parse offline end */
@end
