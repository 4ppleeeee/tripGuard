//
//  SPPlayerMediaSource.m
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerMediaSource.h"

@implementation SPPlayerMediaSource

- (instancetype)initWithMediaInfo:(SPMediaInfo *)mediaInfo headers:(NSMutableDictionary *)headers {
    if (self = [super init]) {
        self.type = SPMediaSourceTypeVid;
        self.mediaInfo = mediaInfo;
        self.headers = headers;
    }

    return self;
}

- (instancetype)initWithMediainfo:(SPMediaInfo *)mediaInfo {
    if (self = [super init]) {
        self.type = SPMediaSourceTypeVid;
        self.mediaInfo = mediaInfo;
        self.headers = [NSMutableDictionary dictionary];
    }
    return self;
}

- (instancetype)initWithUrl:(NSString *)url headers:(NSMutableDictionary *)headers {
    if (self = [super init]) {
        self.type = SPMediaSourceTypeUrl;
        self.url = url;
        self.headers = headers;
    }
    return self;
}

- (instancetype)initWithUrl:(NSString *)url headers:(NSMutableDictionary *)headers serverTime:(int64_t)serverTime {
    if (self = [super init]) {
        self.type = SPMediaSourceTypeUrl;
        self.url = url;
        self.headers = headers;
        self.serverTime = serverTime;
    }
    return self;
}

///lowryhe 不知道怎么处理，先注释
//- (instancetype)initWithComposition:(ITPMultiMediaAsset *)composition headers:(NSMutableDictionary *)headers {
//    if (self = [super init]) {
//        self.type = SPMediaSourceTypeAsset;
//        self.composition = composition;
//        self.headers = headers;
//    }
//    return self;
//}

- (void)setUrl:(NSString *)url {
    self.url = url;
    self.type = SPMediaSourceTypeUrl;
}

//- (void)setComposition:(ITPMultiMediaAsset *)composition {
//    self.type = SPMediaSourceTypeAsset;
//    self.composition = composition;
//}

- (void)setMediaInfo:(SPMediaInfo *)mediaInfo {
    self.type = SPMediaSourceTypeVid;
    self.mediaInfo = mediaInfo;
}

- (BOOL)isValid {
    Boolean valid = (self.type == SPMediaSourceTypeVid && self.mediaInfo != nil);
    valid = valid || (self.type == SPMediaSourceTypeUrl && self.url != nil);
    valid = valid || (self.type == SPMediaSourceTypeAsset);
    return valid;
}

@end
