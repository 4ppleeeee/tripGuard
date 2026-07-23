/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPResourceLoader.m
 Author      : GHL
 Version     : 1.0
 Date        : 16/9/7
 Description :
 History     : 16/9/7 初始版本
 ***********************************************************/

#import "SPResourceLoader.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import "SPFairPlayManager.h"

#define DEFAULT_CONTENTTYPE @"video/mp4"

@interface SPResourceLoader ()

@property (nonatomic, strong) SPFairPlayManager *fairplayManager;

@end

@implementation SPResourceLoader

- (void)dealloc {
    [self cleanResource];
}

- (void)cleanResource {
    if (self.fairplayManager) {
        [self.fairplayManager cancelRequset];
    }
}

#pragma mark - AVAssetResourceLoaderDelegate

- (BOOL)resourceLoader:(AVAssetResourceLoader *)resourceLoader
    shouldWaitForLoadingOfRequestedResource:(AVAssetResourceLoadingRequest *)loadingRequest {
    if ([loadingRequest.request.URL.scheme isEqualToString:@"skd"]) {
        if (!self.fairplayManager) {
            self.fairplayManager = [[SPFairPlayManager alloc] initWithMediaPlayInfo:self.mediaPlayInfo];
        }

        if (![self.fairplayManager startRequestCKCWithRequest:loadingRequest]) {
            SPLOGE(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:request error");

            if ([self.delegate respondsToSelector:@selector(onFairplayRequestError)]) {
                [self.delegate onFairplayRequestError];
            }

            return NO;
        }

        return YES;
    }
    return NO;
}

- (void)resourceLoader:(AVAssetResourceLoader *)resourceLoader didCancelLoadingRequest:(AVAssetResourceLoadingRequest *)loadingRequest {
    SPLOGS(SP_PLAYER_LOG_FILTER, @"%@", NSStringFromSelector(_cmd));

    if ([loadingRequest.request.URL.scheme isEqualToString:@"skd"]) {
        [self.fairplayManager cancelRequset];
        return;
    }
}

@end
